package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.ImmutableMap;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.concurrent.ThreadUtils;
import com.therandomlabs.utils.io.IOConstants;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class ManifestComparer {
	public static class Results implements Serializable {
		private static final long serialVersionUID = 3470798086960813569L;

		private final ExtendedCurseManifest oldManifest;
		private final ExtendedCurseManifest newManifest;
		private final TRLList<Mod> unchanged;
		private final TRLList<VersionChange> updated;
		private final TRLList<VersionChange> downgraded;
		private final TRLList<Mod> removed;
		private final TRLList<Mod> added;

		private boolean unchangedPreloaded;
		private boolean removedPreloaded;
		private boolean addedPreloaded;

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
			if(!unchangedPreloaded) {
				ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), unchanged.size(), index -> {
					unchanged.get(index).title();
				});
				unchanged.sort();
			}

			return unchanged;
		}

		public TRLList<VersionChange> getUpdated() {
			return updated;
		}

		public Map<VersionChange, Map<String, String>> getUpdatedChangelogs(boolean urls)
				throws CurseException, IOException {
			return VersionChange.getChangelogs(updated, urls);
		}

		public TRLList<VersionChange> getDowngraded() {
			return downgraded;
		}

		public Map<VersionChange, Map<String, String>> getDowngradedChangelogs(boolean urls)
				throws CurseException, IOException {
			return VersionChange.getChangelogs(downgraded, urls);
		}

		public TRLList<Mod> getRemoved() throws CurseException {
			if(!removedPreloaded) {
				ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), removed.size(), index -> {
					removed.get(index).title();
				});
				removed.sort();
			}

			return removed;
		}

		public TRLList<Mod> getAdded() throws CurseException {
			if(!addedPreloaded) {
				ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), added.size(), index -> {
					added.get(index).title();
				});
				added.sort();
			}

			return added;
		}

		public String getOldForgeVersion() {
			return oldManifest.minecraft.getForgeVersion();
		}

		public String getNewForgeVersion() {
			return newManifest.minecraft.getForgeVersion();
		}

		public boolean hasForgeVersionChanged() {
			return !getOldForgeVersion().equals(getNewForgeVersion());
		}
	}

	public static class VersionChange implements Comparable<VersionChange>, Serializable {
		private static final long serialVersionUID = 1789316477374597287L;

		public static final String ARCHIVED_FILE = "[Archived file]";

		private transient CurseProject project;

		private final String mcVersion;

		private final Mod oldMod;
		private transient CurseFile oldFile;

		private final Mod newMod;
		private transient CurseFile newFile;

		transient boolean preloaded;

		VersionChange(String mcVersion, Mod oldMod, Mod newMod) {
			this.mcVersion = mcVersion;
			this.oldMod = oldMod;
			this.newMod = newMod;
		}

		public CurseProject getProject() throws CurseException {
			if(project == null) {
				project = CurseProject.fromID(newMod.projectID);
			}
			return project;
		}

		public String getModTitle() throws CurseException {
			return getProject().title();
		}

		public Mod getOldMod() {
			return oldMod;
		}

		public CurseFile getOldFile() throws CurseException {
			if(oldFile == null) {
				oldFile = getProject().fileClosestToID(oldMod.fileID, false);
			}
			return oldFile;
		}

		public String getOldFileName() throws CurseException {
			return isOldFileArchived() ? ARCHIVED_FILE : getOldFile().name();
		}

		public boolean isOldFileArchived() throws CurseException {
			return getOldFile().id() != oldMod.fileID;
		}

		public Mod getNewMod() {
			return newMod;
		}

		public CurseFile getNewFile() throws CurseException {
			if(newFile == null) {
				newFile = getProject().fileClosestToID(newMod.fileID, true);
			}
			return newFile;
		}

		public String getNewFileName() throws CurseException {
			return isNewFileArchived() ? ARCHIVED_FILE : getNewFile().name();
		}

		public boolean isNewFileArchived() throws CurseException {
			return getNewFile().id() != newMod.fileID;
		}

		public boolean isDowngrade() {
			return oldMod.fileID > newMod.fileID;
		}

		public CurseFileList getChangelogFiles() throws CurseException {
			final CurseProject project = getProject();
			final CurseFile oldFile = isDowngrade() ? getNewFile() : getOldFile();
			final CurseFile newFile = isDowngrade() ? getOldFile() : getNewFile();
			final CurseFileList files = project.files();

			if(oldFile.minecraftVersion() == newFile.minecraftVersion()) {
				files.filterVersions(oldFile.minecraftVersion());
			} else {
				files.filterMCVersionGroup(mcVersion);
			}

			files.between(oldFile, newFile);

			if(oldFile == newFile) {
				files.add(newFile);
			}

			filterChangelogFiles(project, oldFile, newFile, files);

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

		private static boolean needsCurseFiles(CurseProject project) {
			final int id = project.id();
			final String owner = project.owner().username();
			return id != BIOMES_O_PLENTY_ID && id != ACTUALLY_ADDITIONS_ID &&
					!owner.equals("TeamCoFH") && !owner.equals("bre21") &&
					!owner.equals("zmaster587");
		}

		void preload() throws CurseException {
			if(preloaded) {
				return;
			}

			final CurseProject project = getProject();
			if(needsCurseFiles(project)) {
				project.files();
			}
		}

		List<String> getURLsToPreload() throws CurseException {
			if(preloaded) {
				return ImmutableList.empty();
			}

			final CurseProject project = getProject();
			final int id = project.id();

			final CurseFile oldFile = isDowngrade() ? getNewFile() : getOldFile();
			final CurseFile newFile = isDowngrade() ? getOldFile() : getNewFile();

			if(id == BIOMES_O_PLENTY_ID) {
				return new ImmutableList<>(newFile.urlString());
			}

			if(id == ACTUALLY_ADDITIONS_ID) {
				return new ImmutableList<>(ACTUALLY_ADDITIONS_CHANGELOG);
			}

			final String owner = project.owner().username();

			if(owner.equals("TeamCoFH")) {
				final String url = getCoFHURL(newFile);
				if(url != null) {
					return new ImmutableList<>(url);
				}
			}

			if(owner.equals("bre2l") || owner.equals("zmaster587")) {
				return new ImmutableList<>(newFile.urlString(), oldFile.urlString());
			}

			return getChangelogFiles().stream().map(file -> file.urlString()).
					collect(Collectors.toList());
		}

		public Map<String, String> getChangelogs() throws CurseException, IOException {
			return getChangelogs(false);
		}

		public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
			final CurseProject project = getProject();
			final int id = project.id();
			final CurseFile oldFile = isDowngrade() ? getNewFile() : getOldFile();
			final CurseFile newFile = isDowngrade() ? getOldFile() : getNewFile();

			final CurseFileList files = getChangelogFiles();
			final Map<String, String> changelogs = new LinkedHashMap<>(files.size());

			if(id == BIOMES_O_PLENTY_ID) {
				return getBoPChangelog(oldFile, newFile, urls);
			}

			if(id == ACTUALLY_ADDITIONS_ID) {
				return getAAChangelog(oldFile, newFile, urls);
			}

			final String owner = project.owner().username();

			if(owner.equals("TeamCoFH")) {
				final Map<String, String> changelog = getCoFHChangelog(oldFile, newFile, urls);
				if(changelog != null) {
					return changelog;
				}
			}

			if(owner.equals("zmaster587") || newFile.uploader().equals("mezz")) {
				final String changelog = getChangelogByComparison(oldFile, newFile, urls);
				if(urls) {
					changelogs.put(VIEW_CHANGELOG_AT, changelog);
				} else {
					changelogs.put("Retrieved from " + getOldFileName() + " and " +
							getNewFileName() + "'s changelogs", changelog);
				}
				return changelogs;
			}

			if(owner.equals("bre2el")) {
				return getBre2elChangelog(oldFile, newFile, urls);
			}

			final boolean isMcJty = owner.equals("McJty");

			for(CurseFile file : files) {
				if(file.changelogProvided()) {
					if(urls) {
						changelogs.put(file.name(), file.urlString());
					} else {
						String changelog = file.changelog();

						if(isMcJty) {
							//McJty's changelogs' first two lines are not needed
							final String[] lines = StringUtils.splitNewline(changelog);
							changelog = ArrayUtils.join(ArrayUtils.subArray(lines, 2), NEWLINE);
						}

						changelogs.put(file.name(), changelog);
					}
				} else {
					changelogs.put(file.name(), NO_CHANGELOG_PROVIDED);
				}
			}

			return changelogs;
		}

		public static Map<VersionChange, Map<String, String>> getChangelogs(
				TRLList<VersionChange> versionChanges, boolean urls)
				throws CurseException, IOException {
			final Set<String> toPreload = ConcurrentHashMap.newKeySet();

			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(),
					versionChanges.size(), index -> {
				final VersionChange versionChange = versionChanges.get(index);
				versionChange.preload();
				toPreload.addAll(versionChange.getURLsToPreload());
			});

			versionChanges.sort();

			final List<String> list = new TRLList<>(toPreload);

			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), list.size(), index -> {
				DocumentUtils.get(list.get(index));
			});

			final Map<VersionChange, Map<String, String>> changelogs =
					new LinkedHashMap<>(versionChanges.size());

			for(VersionChange versionChange : versionChanges) {
				changelogs.put(versionChange, versionChange.getChangelogs(urls));
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
		public CurseProject getProject() {
			return null;
		}

		@Override
		public String getModTitle() {
			return MinecraftForge.TITLE;
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
		public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
			if(urls) {
				return new ImmutableMap<>(
						new String[] {"View changelog at"},
						new String[] {MinecraftForge.getChangelogURL(
								isDowngrade() ? oldVersion : newVersion
						).toString()}
				);
			}
			return MinecraftForge.getChangelog(oldVersion, newVersion);
		}

		@Override
		void preload() {}

		@Override
		List<String> getURLsToPreload() {
			return ImmutableList.empty();
		}
	}

	static final String NEWLINE = IOConstants.LINE_SEPARATOR;

	private static final String NO_CHANGELOG_PROVIDED = "No changelog provided.";
	private static final String VIEW_CHANGELOG_AT = "View changelog at";

	private static final int SERVEROBSERVER_ID = 279375;
	private static final int BIOMES_O_PLENTY_ID = 220318;
	private static final int ACTUALLY_ADDITIONS_ID = 228404;
	private static final String ACTUALLY_ADDITIONS_CHANGELOG = "https://raw." +
			"githubusercontent.com/Ellpeck/ActuallyAdditions/master/update/changelog.md";

	private ManifestComparer() {}

	public static Results compare(ExtendedCurseManifest oldManifest,
			ExtendedCurseManifest newManifest) throws CurseException, IOException {
		oldManifest.both();
		oldManifest.moveAlternativeModsToFiles();
		newManifest.both();
		newManifest.moveAlternativeModsToFiles();

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

	static void filterChangelogFiles(CurseProject project, CurseFile oldFile, CurseFile newFile,
			CurseFileList files) {
		//ServerObserver Universal
		if(project.id() == SERVEROBSERVER_ID) {
			if(!oldFile.name().endsWith(" Universal") &&
					!newFile.name().endsWith(" Universal")) {
				files.removeIf(file -> file.name().endsWith(" Universal"));
			} else if(oldFile.name().endsWith(" Universal") &&
					newFile.name().endsWith(" Universal")) {
				files.removeIf(file -> !file.name().endsWith(" Universal"));
			}
		}
	}

	static String getCoFHURL(CurseFile file) throws CurseException {
		String url = file.changelog().trim();
		url = url.split("]")[0].substring(1);
		url = url.replace("/blob", "");
		url = url.replace("github", "raw.githubusercontent");

		try {
			new URL(url);
		} catch(MalformedURLException ex) {
			return null;
		}

		return url;
	}

	static Map<String, String> getCoFHChangelog(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException, IOException {
		final String changelogURL = getCoFHURL(newFile);

		if(changelogURL == null) {
			return null;
		}

		final Map<String, String> changelog = new LinkedHashMap<>();

		if(url) {
			changelog.put(VIEW_CHANGELOG_AT, changelogURL);
			return changelog;
		}

		String oldVersion = oldFile.name().split("-")[2];
		int lengthToRemove = ArrayUtils.last(oldVersion.split("\\.")).length() + 1;
		oldVersion = oldVersion.substring(0, oldVersion.length() - lengthToRemove);

		String newVersion = newFile.name().split("-")[2];
		lengthToRemove = ArrayUtils.last(newVersion.split("\\.")).length() + 1;
		newVersion = newVersion.substring(0, newVersion.length() - lengthToRemove);

		final String[] lines =
				ArrayUtils.subArray(StringUtils.splitNewline(DocumentUtils.read(changelogURL)), 4);
		final StringBuilder entry = new StringBuilder();
		String version = null;

		boolean checkVersion = true;
		boolean changelogStarted = false;

		for(String line : lines) {
			if(checkVersion) {
				checkVersion = false;

				if(line.isEmpty()) {
					continue;
				}

				if(changelogStarted) {
					String entryString = entry.toString();
					entryString = StringUtils.removeLastChars(entryString,
							NEWLINE.length());
					changelog.put(version, entryString);
					entry.setLength(0);
				}

				version = StringUtils.removeLastChar(line);

				if(changelogStarted) {
					if(version.equals(oldVersion)) {
						break;
					}
				} else if(version.equals(newVersion)) {
					changelogStarted = true;
				}

				continue;
			}

			if(line.startsWith("------")) {
				checkVersion = true;
				continue;
			}

			entry.append(line).append(NEWLINE);
		}

		return changelog;
	}

	static Map<String, String> getBoPChangelog(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException {
		final Map<String, String> changelog = new LinkedHashMap<>();

		if(url) {
			changelog.put(VIEW_CHANGELOG_AT, newFile.urlString());
			return changelog;
		}

		final String[] split = oldFile.name().split("-");
		final String oldVersion = split[1] + '-' + split[2];

		final String[] lines = StringUtils.splitNewline(newFile.changelog());

		final StringBuilder entry = new StringBuilder();
		String version = null;

		for(int i = 1; i < lines.length; i++) {
			final String line = lines[i];
			if(line.startsWith("Build ")) {
				version = StringUtils.removeLastChar(line.split(" ")[1]);
				continue;
			}

			if(version.equals(oldVersion)) {
				break;
			}

			if(line.isEmpty()) {
				changelog.put(version, entry.toString());
				entry.setLength(0);
				version = null;
			}

			if(version != null) {
				entry.append(line.substring(1)).append(NEWLINE);
			}
		}

		return changelog;
	}

	static Map<String, String> getAAChangelog(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException, IOException {
		final Map<String, String> changelog = new LinkedHashMap<>();

		if(url) {
			changelog.put(VIEW_CHANGELOG_AT, ACTUALLY_ADDITIONS_CHANGELOG);
			return changelog;
		}

		String[] split = oldFile.name().split("-");
		String oldVersion = split[1] + '-' + split[2];
		oldVersion = StringUtils.removeLastChars(oldVersion, 4);

		split = newFile.name().split("-");
		String newVersion = split[1] + '-' + split[2];
		newVersion = StringUtils.removeLastChars(newVersion, 4);

		final String[] lines =
				StringUtils.splitNewline(DocumentUtils.read(ACTUALLY_ADDITIONS_CHANGELOG));
		final StringBuilder entry = new StringBuilder();
		String version = null;

		boolean changelogStarted = false;

		for(String line : lines) {
			if(line.startsWith("# ")) {
				if(changelogStarted) {
					changelog.put(version, entry.toString());
					entry.setLength(0);
				}

				version = line.split(" ")[1];
				if(!changelogStarted) {
					if(version.equals(newVersion)) {
						changelogStarted = true;
					}
				} else if(version.equals(oldVersion)) {
					break;
				}

				continue;
			}

			if(line.isEmpty()) {
				continue;
			}

			entry.append(line).append(NEWLINE);
		}

		return changelog;
	}

	static Map<String, String> getBre2elChangelog(CurseFile oldFile, CurseFile newFile, boolean url)
			throws CurseException {
		final Map<String, String> changelog = new LinkedHashMap<>();

		if(url) {
			changelog.put(VIEW_CHANGELOG_AT, newFile.urlString());
			return changelog;
		}

		final String oldVersion = StringUtils.removeLastChars(oldFile.name().split("-")[2], 4);

		final String[] lines = StringUtils.splitNewline(newFile.changelog());
		final StringBuilder entry = new StringBuilder();
		String version = null;

		for(String line : lines) {
			if(line.startsWith("v")) {
				if(version != null) {
					changelog.put(version, entry.toString());
					entry.setLength(0);
				}

				version = line.substring(1);

				if(version.equals(oldVersion)) {
					break;
				}

				continue;
			}

			if(line.isEmpty()) {
				continue;
			}

			entry.append(line).append(NEWLINE);
		}

		return changelog;
	}

	static String getChangelogByComparison(CurseFile oldFile, CurseFile newFile, boolean url)
			throws CurseException {
		if(url) {
			return newFile.urlString();
		}

		String changelog = newFile.changelog().replace(oldFile.changelog(), "");
		while(changelog.endsWith("\r") || changelog.endsWith("\n")) {
			changelog = StringUtils.removeLastChar(changelog);
		}

		return changelog;
	}
}
