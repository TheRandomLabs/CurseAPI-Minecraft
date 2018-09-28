package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.cursemeta.CurseMetaException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.util.Documents;
import com.therandomlabs.curseapi.util.URLs;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.ThreadUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public class VersionChange implements Comparable<VersionChange>, Serializable {
	private static final long serialVersionUID = 1789316477374597287L;

	public static final String ARCHIVED_FILE = "[Archived file]";
	transient boolean preloaded;
	transient boolean valid = true;
	private final MCVersion mcVersion;
	private final Mod oldMod;
	private final Mod newMod;
	private transient CurseProject project;
	private transient CurseFile oldFile;
	private transient CurseFile newFile;
	private transient CurseFileList files;

	VersionChange(MCVersion mcVersion, Mod oldMod, Mod newMod) {
		this.mcVersion = mcVersion;
		this.oldMod = oldMod;
		this.newMod = newMod;
	}

	@Override
	public int hashCode() {
		return mcVersion.hashCode() * oldMod.hashCode() * newMod.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof VersionChange)) {
			return false;
		}

		final VersionChange versionChange = (VersionChange) object;

		return versionChange.mcVersion.equals(mcVersion) && versionChange.oldMod.equals(oldMod) &&
				versionChange.newMod.equals(newMod);
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

	public Mod getOldMod() {
		return oldMod;
	}

	public CurseFile getOldFile() throws CurseException {
		if(oldFile == null) {
			oldFile = getFile(oldMod.fileID, false);
		}

		return oldFile;
	}

	public Mod getOlderMod() {
		return isDowngrade() ? newMod : oldMod;
	}

	public CurseFile getOlderFile() throws CurseException {
		return isDowngrade() ? getNewFile() : getOldFile();
	}

	public String getOldFileName() throws CurseException {
		return isOldFileUnknown() ? ARCHIVED_FILE : getOldFile().name();
	}

	public boolean isOldFileUnknown() throws CurseException {
		final CurseFile oldFile = getOldFile();
		return oldFile.isNull() || oldFile.id() != oldMod.fileID;
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

	public Mod getNewerMod() {
		return isDowngrade() ? oldMod : newMod;
	}

	public CurseFile getNewerFile() throws CurseException {
		return isDowngrade() ? getOldFile() : getNewFile();
	}

	public String getNewFileName() throws CurseException {
		return isNewFileUnknown() ? ARCHIVED_FILE : getNewFile().name();
	}

	public boolean isNewFileUnknown() throws CurseException {
		final CurseFile newFile = getNewFile();
		return newFile.isNull() || newFile.id() != newMod.fileID;
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

		if(oldFile.gameVersion() == newFile.gameVersion()) {
			files.filterGameVersions(oldFile.gameVersion());
		} else {
			files.filterGameVersionGroups(mcVersion.getGroup());
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

	public String getModTitle() throws CurseException {
		return getProject() == null ? CurseProject.UNKNOWN_TITLE : getProject().title();
	}

	public CurseProject getProject() throws CurseException {
		if(project == null && valid) {
			project = CurseProject.fromID(newMod.projectID, true);
		}

		return project;
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

			return Collections.singletonMap(
					"Could not retrieve changelog",
					ex.getClass().getName() + ": " + ex.getMessage()
			);
		}
	}

	public Map<String, String> getChangelogs() throws CurseException, IOException {
		return getChangelogs(false);
	}

	public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
		final CurseFile oldFile = getOlderFile();
		final CurseFile newFile = getNewerFile();

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
			String changelog = null;

			for(ModSpecificHandler handler : ManifestComparer.handlers) {
				changelog = handler.getChangelog(file);
			}

			if(changelog == null) {
				changelog = file.changelog(true);
			}

			if(file.hasChangelog()) {
				if(urls) {
					changelogs.put(file.name(), ManifestComparer.getCurseForgeURL(file));
				} else {
					for(ModSpecificHandler handler : ManifestComparer.handlers) {
						changelog = handler.modifyChangelog(
								newMod.projectID, oldFile, newFile, changelog
						);
					}

					changelogs.put(file.name(), changelog);
				}
			} else {
				changelogs.put(file.name(), ManifestComparer.NO_CHANGELOG_PROVIDED);
			}
		}

		return changelogs;
	}

	void preload() throws CurseException {
		if(preloaded) {
			return;
		}

		final CurseProject project = getProject();
		MCEventHandling.forEach(handler -> handler.downloadingModFileData(project));
		files = CurseFile.getFilesBetween(
				newMod.projectID, getOlderMod().fileID, getNewerMod().fileID
		);
		valid = !files.isEmpty();

		if(valid) {
			MCEventHandling.forEach(handler -> handler.downloadedModFileData(project));
		} else {
			MCEventHandling.forEach(handler -> handler.noFilesFound(project));
		}
	}

	List<String> getURLsToPreload() throws CurseException {
		if(preloaded) {
			return ImmutableList.empty();
		}

		for(ModSpecificHandler handler : ManifestComparer.handlers) {
			final CurseFile newFile = getNewerFile();

			if(handler.shouldPreloadOnlyNewFile(getProject())) {
				return new ImmutableList<>(ManifestComparer.getChangelogURLString(newFile));
			}

			final List<String> urls =
					handler.getURLsToPreload(getProject().id(), getOlderFile(), newFile);

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

	private CurseFile getFile(int fileID, boolean preferOlder) throws CurseException {
		try {
			return CurseFile.getFile(newMod.projectID, fileID);
		} catch(CurseMetaException ignored) {}

		if(files == null) {
			preload();
		}

		return files.fileClosestToID(fileID, preferOlder);
	}

	public static Map<VersionChange, Map<String, String>> getChangelogs(
			List<VersionChange> versionChanges, boolean urls, boolean quietly)
			throws CurseException, IOException {
		final Set<String> preloadURLsSet = ConcurrentHashMap.newKeySet();

		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), versionChanges.size(), index -> {
			final VersionChange versionChange = versionChanges.get(index);
			versionChange.preload();

			if(!versionChange.valid) {
				return;
			}

			preloadURLsSet.addAll(versionChange.getURLsToPreload());
		});

		final TRLList<VersionChange> sorted = new TRLList<>(versionChanges);
		sorted.removeIf(versionChange -> !versionChange.valid);
		sorted.sort();

		final List<String> preloadURLs = new TRLList<>(preloadURLsSet);

		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), preloadURLs.size(), index -> {
			final URL url = URLs.of(preloadURLs.get(index));

			MCEventHandling.forEach(handler -> handler.downloadingChangelogData(url));

			CurseAPI.doWithRetries(() -> Documents.get(url));

			MCEventHandling.forEach(handler -> handler.downloadedChangelogData(url));
		});

		final Map<VersionChange, Map<String, String>> changelogs =
				new LinkedHashMap<>(sorted.size());

		for(VersionChange versionChange : sorted) {
			if(quietly) {
				changelogs.put(versionChange, versionChange.getChangelogsQuietly(urls));
			} else {
				changelogs.put(versionChange, versionChange.getChangelogs(urls));
			}
		}

		return changelogs;
	}
}
