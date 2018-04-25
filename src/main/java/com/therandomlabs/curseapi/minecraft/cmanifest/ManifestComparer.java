package com.therandomlabs.curseapi.minecraft.cmanifest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.cursemeta.CurseMeta;
import com.therandomlabs.curseapi.cursemeta.CurseMetaException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.ImmutableMap;
import com.therandomlabs.utils.collection.ImmutableSet;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.concurrent.ThreadUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class ManifestComparer {
	public static final String NO_CHANGELOG_PROVIDED = "No changelog provided.";
	public static final String VIEW_CHANGELOG_AT = "View changelog at";
	public static final String UNKNOWN_TITLE = "Unknown Title";

	private static final Set<ModSpecificHandler> handlers = new HashSet<>(2);

	private ManifestComparer() {}

	public static class Results implements Serializable {
		private static final long serialVersionUID = 3470798086960813569L;

		private final ExtendedCurseManifest oldManifest;
		private final ExtendedCurseManifest newManifest;
		private final TRLList<Mod> unchanged;
		private final TRLList<VersionChange> updated;
		private final TRLList<VersionChange> downgraded;
		private final TRLList<Mod> removed;
		private final TRLList<Mod> added;

		private boolean unchangedLoaded;
		private boolean removedLoaded;
		private boolean addedLoaded;

		Results(ExtendedCurseManifest oldManifest, ExtendedCurseManifest newManifest,
				TRLList<Mod> unchanged, TRLList<VersionChange> updated,
				TRLList<VersionChange> downgraded, TRLList<Mod> removed, TRLList<Mod> added) {
			this.oldManifest = oldManifest;
			this.newManifest = newManifest;
			this.unchanged = unchanged;
			this.updated = updated;
			this.downgraded = downgraded;
			this.removed = removed;
			this.added = added;
		}

		public ExtendedCurseManifest getOldManifest() {
			return oldManifest;
		}

		public ExtendedCurseManifest getNewManifest() {
			return newManifest;
		}

		public TRLList<Mod> getUnchanged() throws CurseException {
			if(!unchangedLoaded) {
				load(unchanged);
				unchangedLoaded = true;
			}

			return unchanged;
		}

		private void load(TRLList<Mod> mods) throws CurseException {
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), mods.size(),
					index -> mods.get(index).title());
			mods.sort();
		}

		public TRLList<VersionChange> getUpdated() {
			return updated;
		}

		public Map<VersionChange, Map<String, String>> getUpdatedChangelogs(boolean urls)
				throws CurseException, IOException {
			return VersionChange.getChangelogs(updated, urls, false);
		}

		public Map<VersionChange, Map<String, String>> getUpdatedChangelogsQuietly(boolean urls)
				throws CurseException, IOException {
			return VersionChange.getChangelogs(updated, urls, true);
		}

		public TRLList<VersionChange> getDowngraded() {
			return downgraded;
		}

		public Map<VersionChange, Map<String, String>> getDowngradedChangelogs(boolean urls)
				throws CurseException, IOException {
			return VersionChange.getChangelogs(downgraded, urls, false);
		}

		public Map<VersionChange, Map<String, String>> getDowngradedChangelogsQuietly(
				boolean urls) throws CurseException, IOException {
			return VersionChange.getChangelogs(downgraded, urls, true);
		}

		public TRLList<Mod> getRemoved() throws CurseException {
			if(!removedLoaded) {
				load(removed);
				removedLoaded = true;
			}

			return removed;
		}

		public TRLList<Mod> getAdded() throws CurseException {
			if(!addedLoaded) {
				load(added);
				addedLoaded = true;
			}

			return added;
		}

		public boolean hasForgeVersionChanged() {
			return !getOldForgeVersion().equals(getNewForgeVersion());
		}

		public String getOldForgeVersion() {
			return oldManifest.minecraft.getForgeVersion();
		}

		public String getNewForgeVersion() {
			return newManifest.minecraft.getForgeVersion();
		}
	}

	public static class VersionChange implements Comparable<VersionChange>, Serializable {
		public static final String ARCHIVED_FILE = "[Archived file]";

		private static final long serialVersionUID = 1789316477374597287L;

		private final String mcVersion;
		private final Mod oldMod;
		private final Mod newMod;

		private transient CurseProject project;
		private transient CurseFile oldFile;
		private transient CurseFile newFile;
		private transient CurseFileList files;

		transient boolean hasNoProject;
		transient boolean preloaded;

		VersionChange(String mcVersion, Mod oldMod, Mod newMod) {
			this.mcVersion = mcVersion;
			this.oldMod = oldMod;
			this.newMod = newMod;
		}

		public static Map<VersionChange, Map<String, String>> getChangelogs(
				TRLList<VersionChange> versionChanges, boolean urls, boolean quietly)
				throws CurseException, IOException {
			final Set<String> toPreload = ConcurrentHashMap.newKeySet();

			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), versionChanges.size(),
					index -> {
				final VersionChange versionChange = versionChanges.get(index);
				versionChange.preload();
				toPreload.addAll(versionChange.getURLsToPreload());
			});

			versionChanges.removeIf(Objects::isNull);
			versionChanges.sort();

			final List<String> list = new TRLList<>(toPreload);

			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), list.size(), index -> {
				try {
					DocumentUtils.get(list.get(index));
				} catch(CurseException ex) {
					//FileNotFoundException occurs if a file is archived
					if(!(ex.getCause() instanceof FileNotFoundException)) {
						throw ex;
					}
				}
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

			for(ModSpecificHandler handler : handlers) {
				handler.filterFileList(newMod.projectID, files, oldFile, newFile);
			}

			return files;
		}

		@Override
		public int compareTo(VersionChange versionChange) {
			try {
				return getModTitle().compareTo(versionChange.getModTitle());
			} catch(CurseException ex) {
				ThrowableHandling.handleUnexpected(ex);
			}
			return 0;
		}

		public String getModTitle() throws CurseException {
			return getProject() == null ? UNKNOWN_TITLE : getProject().title();
		}

		public CurseProject getProject() throws CurseException {
			if(project == null && !hasNoProject) {
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

			files = CurseFile.filesFromProjectID(newMod.projectID);
			files.between(getOlderFile().id(), getNewerFile().id());
		}

		List<String> getURLsToPreload() throws CurseException {
			if(preloaded) {
				return ImmutableList.empty();
			}

			for(ModSpecificHandler handler : handlers) {
				final List<String> urls = handler.getURLsToPreload(newMod.projectID, getOlderFile(),
						getNewerFile());
				if(urls != null) {
					return urls;
				}

				if(handler.shouldPreloadOnlyNewFile(newMod.projectID, getProject())) {
					return new ImmutableList<>(getChangelogURLString(newFile));
				}
			}

			final CurseFileList files = getChangelogFiles();
			final List<String> urls = new TRLList<>(files.size());

			for(CurseFile file : files) {
				urls.add(getChangelogURLString(file));
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
			final CurseFile oldFile = isDowngrade() ? getNewFile() : getOldFile();
			final CurseFile newFile = isDowngrade() ? getOldFile() : getNewFile();

			for(ModSpecificHandler handler : handlers) {
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
						changelogs.put(file.name(), getChangelogURLString(file));
					} else {
						String changelog = file.changelog();

						for(ModSpecificHandler handler : handlers) {
							changelog = handler.modifyChangelog(newMod.projectID, oldFile, newFile,
									changelog);
						}

						changelogs.put(file.name(), changelog);
					}
				} else {
					changelogs.put(file.name(), NO_CHANGELOG_PROVIDED);
				}
			}

			return changelogs;
		}
	}

	public static class ForgeVersionChange extends VersionChange {
		private static final long serialVersionUID = -4645680856319627592L;

		private final String oldVersion;
		private final String newVersion;
		private final boolean isDowngrade;

		ForgeVersionChange(String mcVersion, String oldVersion, String newVersion,
				boolean isDowngrade) {
			super(mcVersion, null, null);
			this.oldVersion = oldVersion;
			this.newVersion = newVersion;
			this.isDowngrade = isDowngrade;
		}

		@Override
		public Mod getOldMod() {
			return null;
		}

		@Override
		public CurseFile getOldFile() {
			return null;
		}

		@Override
		public String getOldFileName() {
			return oldVersion;
		}

		@Override
		public Mod getNewMod() {
			return null;
		}

		@Override
		public CurseFile getNewFile() {
			return null;
		}

		@Override
		public String getNewFileName() {
			return newVersion;
		}

		@Override
		public boolean isDowngrade() {
			return isDowngrade;
		}

		@Override
		public String getModTitle() {
			return MinecraftForge.TITLE;
		}

		@Override
		public CurseProject getProject() {
			return null;
		}

		@Override
		void preload() {}

		@Override
		List<String> getURLsToPreload() {
			return ImmutableList.empty();
		}

		@Override
		public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
			if(urls) {
				final String newerVersion = isDowngrade() ? oldVersion : newVersion;
				final URL changelogURL = MinecraftForge.getChangelogURL(newerVersion);
				return new ImmutableMap<>(
						new ImmutableSet<>(VIEW_CHANGELOG_AT),
						new ImmutableList<>(changelogURL.toString())
				);
			}
			return MinecraftForge.getChangelog(oldVersion, newVersion);
		}
	}

	public interface ModSpecificHandler {
		boolean shouldPreloadOnlyNewFile(int projectID, CurseProject project);

		List<String> getURLsToPreload(int projectID, CurseFile oldFile, CurseFile newFile)
				throws CurseException;

		void filterFileList(int projectID, CurseFileList files, CurseFile oldFile,
				CurseFile newFile) throws CurseException;

		Map<String, String> getChangelogs(int projectID, CurseFile oldFile, CurseFile newFile,
				boolean urls) throws CurseException, IOException;

		String modifyChangelog(int projectID, CurseFile oldFile, CurseFile newFile,
				String changelog) throws CurseException;
	}

	public static void registerModSpecificHandler(ModSpecificHandler handler) {
		handlers.add(handler);
	}

	public static void removeModSpecificHandler(ModSpecificHandler handler) {
		handlers.remove(handler);
	}

	public static Results compare(ExtendedCurseManifest oldManifest,
			ExtendedCurseManifest newManifest) throws CurseException, IOException {
		oldManifest.both();
		newManifest.both();

		final TRLList<Mod> unchanged = new TRLList<>();
		final TRLList<VersionChange> updated = new TRLList<>();
		final TRLList<VersionChange> downgraded = new TRLList<>();
		final TRLList<Mod> removed = new TRLList<>();
		final TRLList<Mod> added = new TRLList<>();

		final String mcVersion = newManifest.minecraft.version.toString();

		for(Mod oldMod : oldManifest.files) {
			boolean found = false;

			for(Mod newMod : newManifest.files) {
				if(oldMod.projectID == newMod.projectID) {
					found = true;

					if(oldMod.fileID == newMod.fileID) {
						unchanged.add(newMod);
						break;
					}

					if(newMod.fileID > oldMod.fileID) {
						updated.add(new VersionChange(mcVersion, oldMod, newMod));
						break;
					}

					downgraded.add(new VersionChange(mcVersion, newMod, oldMod));
					break;
				}
			}

			if(!found) {
				removed.add(oldMod);
			}
		}

		for(Mod newMod : newManifest.files) {
			boolean found = false;

			for(Mod oldMod : oldManifest.files) {
				if(oldMod.projectID == newMod.projectID) {
					found = true;
					break;
				}
			}

			if(!found) {
				added.add(newMod);
			}
		}

		final String oldForge = oldManifest.minecraft.getForgeVersion();
		final String newForge = newManifest.minecraft.getForgeVersion();

		final int compare = MinecraftForge.compare(oldForge, newForge);

		if(compare < 0) {
			updated.add(new ForgeVersionChange(mcVersion, oldForge, newForge, false));
		} else if(compare > 0) {
			downgraded.add(new ForgeVersionChange(mcVersion, newForge, oldForge, true));
		}

		return new Results(oldManifest, newManifest, unchanged, updated, downgraded, removed,
				added);
	}

	public static URL getChangelogURL(CurseFile file) throws CurseException {
		if(!CurseAPI.isAvoidingCurseMeta() || file.url() == null) {
			return CurseMeta.getChangelogURL(file.projectID(), file.id());
		}

		return file.url();
	}

	public static String getChangelogURLString(CurseFile file) throws CurseException {
		return getChangelogURL(file).toString();
	}
}
