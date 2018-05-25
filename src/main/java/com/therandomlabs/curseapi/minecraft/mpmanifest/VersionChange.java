package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.cursemeta.CurseMetaException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.curseapi.util.URLUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.ThreadUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public class VersionChange implements Comparable<VersionChange>, Serializable {
	private static final long serialVersionUID = 1789316477374597287L;

	public static final String ARCHIVED_FILE = "[Archived file]";

	private final String mcVersion;
	private final Mod oldMod;
	private final Mod newMod;

	private transient CurseProject project;
	private transient CurseFile oldFile;
	private transient CurseFile newFile;
	private transient CurseFileList files;

	transient boolean hasNoProject;
	transient boolean preloaded;
	transient boolean valid = true;

	VersionChange(String mcVersion, Mod oldMod, Mod newMod) {
		this.mcVersion = mcVersion;
		this.oldMod = oldMod;
		this.newMod = newMod;
	}

	public Mod getOldMod() {
		return oldMod;
	}

	public CurseFile getOldFile() throws CurseException {
		if(oldFile == null) {
			oldFile = getFile(oldMod.fileID, false);
		}

		return oldFile;
	}

	public CurseFile getOlderFile() throws CurseException {
		return isDowngrade() ? getNewFile() : getOldFile();
	}

	public String getOldFileName() throws CurseException {
		return isOldFileUnknown() ? ARCHIVED_FILE : getOldFile().name();
	}

	public boolean isOldFileUnknown() throws CurseException {
		return getOldFile().id() != oldMod.fileID;
	}

	public Mod getNewMod() {
		return newMod;
	}

	public CurseFile getNewFile() throws CurseException {
		if(newFile == null) {
			newFile = getFile(newMod.fileID, true);
		}

		return newFile;
	}

	private CurseFile getFile(int fileID, boolean preferOlder) throws CurseException {
		try {
			return CurseFile.fromID(newMod.projectID, fileID);
		} catch(CurseMetaException ignored) { }

		if(files == null) {
			preload();
		}

		return files.fileClosestToID(fileID, preferOlder);
	}

	public CurseFile getNewerFile() throws CurseException {
		return isDowngrade() ? getOldFile() : getNewFile();
	}

	public String getNewFileName() throws CurseException {
		return isNewFileUnknown() ? ARCHIVED_FILE : getNewFile().name();
	}

	public boolean isNewFileUnknown() throws CurseException {
		return getNewFile().id() != newMod.fileID;
	}

	public boolean isDowngrade() {
		return oldMod.fileID > newMod.fileID;
	}

	public CurseFileList getChangelogFiles() throws CurseException {
		if(files == null) {
			preload();
		}

		final CurseFile oldFile = getOlderFile();
		final CurseFile newFile = getNewerFile();

		if(oldFile.minecraftVersion() == newFile.minecraftVersion()) {
			files.filterVersions(oldFile.minecraftVersion());
		} else {
			files.filterMCVersionGroup(mcVersion);
		}

		files.between(oldFile, newFile);

		if(oldFile == newFile) {
			files.add(newFile);
		}

		for(ModSpecificHandler handler : ManifestComparer.handlers) {
			handler.filterFileList(newMod.projectID, files, oldFile, newFile);
		}

		return files;
	}

	@Override
	public int compareTo(VersionChange versionChange) {
		try {
			return getModTitle().compareTo(versionChange.getModTitle());
		} catch(CurseException ex) {
			ThrowableHandling.handle(ex);
		}
		return 0;
	}

	public String getModTitle() throws CurseException {
		return getProject() == null ? ManifestComparer.UNKNOWN_TITLE : getProject().title();
	}

	public CurseProject getProject() throws CurseException {
		if(project == null && !hasNoProject && valid) {
			try {
				project = CurseProject.fromID(newMod.projectID);
			} catch(InvalidProjectIDException ex) {
				hasNoProject = true;
			}
		}
		return project;
	}

	void preload() throws CurseException {
		if(preloaded) {
			return;
		}

		if(CurseAPI.isAvoidingCurseMeta()) {
			try {
				files = getProject().filesBetween(getOlderFile().id(), getNewerFile().id());
				valid = !files.isEmpty();
			} catch(InvalidProjectIDException ex) {
				valid = false;
			}
		} else {
			try {
				files = CurseFile.filesFromProjectID(newMod.projectID);
				valid = !files.isEmpty();
			} catch(InvalidProjectIDException | CurseMetaException ex) {
				valid = false;
			}

			if(valid) {
				files.between(getOlderFile().id(), getNewerFile().id());
			}
		}

		if(!valid) {
			MCEventHandling.forEach(handler -> handler.noFilesFound(newMod.projectID));
		}
	}

	List<String> getURLsToPreload() throws CurseException {
		if(preloaded) {
			return ImmutableList.empty();
		}

		for(ModSpecificHandler handler : ManifestComparer.handlers) {
			if(handler.shouldPreloadOnlyNewFile(getProject())) {
				return new ImmutableList<>(ManifestComparer.getChangelogURLString(newFile));
			}

			final List<String> urls = handler.getURLsToPreload(getProject().id(), getOlderFile(),
					getNewerFile());
			if(urls != null) {
				return urls;
			}
		}

		final CurseFileList files = getChangelogFiles();
		final List<String> urls = new TRLList<>(files.size());

		for(CurseFile file : files) {
			urls.add(ManifestComparer.getChangelogURLString(file));
		}

		return urls;
	}

	public Map<String, String> getChangelogsQuietly() {
		return getChangelogsQuietly(false);
	}

	public Map<String, String> getChangelogsQuietly(boolean urls) {
		try {
			return getChangelogs(urls);
		} catch(CurseException | IOException | NumberFormatException |
				IndexOutOfBoundsException | NullPointerException ex) {
			ThrowableHandling.handleWithoutExit(ex);
			return Collections.singletonMap("Could not retrieve changelog",
					ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	public Map<String, String> getChangelogs() throws CurseException, IOException {
		return getChangelogs(false);
	}

	public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
		final CurseFile oldFile = getOlderFile();

		for(ModSpecificHandler handler : ManifestComparer.handlers) {
			final Map<String, String> changelogs =
					handler.getChangelogs(newMod.projectID, oldFile, newFile, urls);
			if(changelogs != null) {
				return changelogs;
			}
		}

		CurseFileList files = getChangelogFiles();
		final Map<String, String> changelogs = new LinkedHashMap<>(files.size());

		for(CurseFile file : files) {
			if(file.hasChangelog()) {
				if(urls) {
					changelogs.put(file.name(), ManifestComparer.getCurseForgeURL(file));
				} else {
					String changelog = file.changelog();

					for(ModSpecificHandler handler : ManifestComparer.handlers) {
						changelog = handler.modifyChangelog(newMod.projectID, oldFile, newFile,
								changelog);
					}

					changelogs.put(file.name(), changelog);
				}
			} else {
				changelogs.put(file.name(), ManifestComparer.NO_CHANGELOG_PROVIDED);
			}
		}

		return changelogs;
	}

	public static Map<VersionChange, Map<String, String>> getChangelogs(
			TRLList<VersionChange> versionChanges, boolean urls, boolean quietly)
			throws CurseException, IOException {
		final Set<String> toPreload = ConcurrentHashMap.newKeySet();

		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), versionChanges.size(),
				index -> {
			final VersionChange versionChange = versionChanges.get(index);
			versionChange.preload();

			if(!versionChange.valid) {
				versionChanges.set(index, null);
			}

			toPreload.addAll(versionChange.getURLsToPreload());
		});

		versionChanges.removeIf(Objects::isNull);
		versionChanges.sort();

		final List<String> list = new TRLList<>(toPreload);

		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), list.size(), index -> {
			final URL url = URLUtils.url(list.get(index));
			MCEventHandling.forEach(handler -> handler.downloadingChangelogData(url));
			DocumentUtils.get(url);
			MCEventHandling.forEach(handler -> handler.downloadedChangelogData(url));
		});

		final Map<VersionChange, Map<String, String>> changelogs =
				new LinkedHashMap<>(versionChanges.size());

		for(VersionChange versionChange : versionChanges) {
			if(quietly) {
				changelogs.put(versionChange, versionChange.getChangelogsQuietly(urls));
			} else {
				changelogs.put(versionChange, versionChange.getChangelogs(urls));
			}
		}

		return changelogs;
	}
}
