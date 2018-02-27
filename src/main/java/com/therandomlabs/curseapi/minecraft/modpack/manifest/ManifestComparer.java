package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseFile;
import com.therandomlabs.curseapi.CurseFileList;
import com.therandomlabs.curseapi.CurseProject;
import com.therandomlabs.curseapi.MemberType;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.StringUtils;

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

		public TRLList<Mod> getUnchanged() {
			return unchanged;
		}

		public TRLList<VersionChange> getUpdated() {
			return updated;
		}

		public TRLList<VersionChange> getDowngraded() {
			return downgraded;
		}

		public TRLList<Mod> getRemoved() {
			return removed;
		}

		public TRLList<Mod> getAdded() {
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

	public static class VersionChange implements Serializable {
		private static final long serialVersionUID = 1789316477374597287L;

		private CurseProject project;

		private final String mcVersion;

		private final Mod oldMod;
		private CurseFile oldFile;

		private final Mod newMod;
		private CurseFile newFile;

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
				oldFile = getProject().closestFileToID(oldMod.fileID, false);
			}
			return oldFile;
		}

		public String getOldModName() throws CurseException {
			if(getOldFile().equals(getNewFile())) {
				return "[Archived file]";
			}
			return getOldFile().name();
		}

		public Mod getNewMod() {
			return newMod;
		}

		public CurseFile getNewFile() throws CurseException {
			if(newFile == null) {
				newFile = getProject().closestFileToID(newMod.fileID, true);
			}
			return newFile;
		}

		public String getNewModName() throws CurseException {
			return getNewFile().name();
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

		public Map<String, String> getChangelogs() throws CurseException, IOException {
			return getChangelogs(false);
		}

		public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
			final CurseProject project = getProject();
			final int id = project.id();
			final CurseFile oldFile = isDowngrade() ? getNewFile() : getOldFile();
			final CurseFile newFile = isDowngrade() ? getOldFile() : getNewFile();

			final CurseFileList files = getChangelogFiles();
			final Map<String, String> changelogs = new HashMap<>(files.size());

			if(id == BIOMES_O_PLENTY_ID) {
				final Map.Entry<String, String> entry = getBoPChangelog(oldFile, newFile, urls);
				changelogs.put(entry.getKey(), entry.getValue());
				return changelogs;
			}

			if(id == ACTUALLY_ADDITIONS_ID) {
				final Map.Entry<String, String> entry = getAAChangelog(oldFile, newFile, urls);
				changelogs.put(entry.getKey(), entry.getValue());
				return changelogs;
			}

			final String owner = project.members(MemberType.OWNER).get(0).username;

			if(owner.equals("TeamCoFH")) {
				final Map.Entry<String, String> entry = getCoFHChangelog(oldFile, newFile, urls);
				changelogs.put(entry.getKey(), entry.getValue());
				return changelogs;
			}

			if(owner.equals("bre2l") || owner.equals("zmaster587")) {
				final Map.Entry<String, String> entry =
						getChangelogFromOldAndNew(oldFile, newFile, urls);
				changelogs.put(entry.getKey(), entry.getValue());
				return changelogs;
			}

			for(CurseFile file : files) {
				if(file.changelogProvided()) {
					changelogs.put(file.name(), file.changelog());
				} else {
					changelogs.put(file.name(), NO_CHANGELOG_PROVIDED);
				}
			}

			return changelogs;
		}
	}

	private static final String NO_CHANGELOG_PROVIDED = "No changelog provided.";
	private static final String VIEW_CHANGELOG_AT = "View changelog at";
	private static final String RETRIEVED_FROM = "Retrieved from";

	private static final int SERVEROBSERVER_ID = 279375;
	private static final int BIOMES_O_PLENTY_ID = 220318;
	private static final int ACTUALLY_ADDITIONS_ID = 228404;
	private static final String ACTUALLY_ADDITIONS_CHANGELOG = "https://raw." +
			"githubusercontent.com/Ellpeck/ActuallyAdditions/master/update/changelog.md";

	private ManifestComparer() {}

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
			throw new CurseException(ex);
		}

		return url;
	}

	static Map.Entry<String, String> getCoFHChangelog(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException, IOException {
		final String changelogURL = getCoFHURL(newFile);

		if(url) {
			return new SimpleEntry<>(VIEW_CHANGELOG_AT, changelogURL);
		}

		String oldVersion = oldFile.name().split("-")[2];
		int lengthToRemove = ArrayUtils.last(oldVersion.split("\\.")).length() + 1;
		oldVersion = oldVersion.substring(0, oldVersion.length() - lengthToRemove);

		String newVersion = newFile.name().split("-")[2];
		lengthToRemove = ArrayUtils.last(newVersion.split("\\.")).length() + 1;
		newVersion = newVersion.substring(0, newVersion.length() - lengthToRemove);

		final String[] lines = StringUtils.splitNewline(DocumentUtils.read(changelogURL));
		final StringBuilder parsed = new StringBuilder();

		boolean checkVersion = false;
		boolean changelogStarted = false;

		for(String line : lines) {
			if(checkVersion) {
				checkVersion = false;

				line = line.trim();
				if(line.isEmpty()) {
					continue;
				}

				if(changelogStarted) {
					if(StringUtils.removeLastChar(line).equals(oldVersion)) {
						break;
					}
				} else if(StringUtils.removeLastChar(line).equals(newVersion)) {
					changelogStarted = true;
				}
			}

			if(line.startsWith("======") || line.startsWith("------")) {
				checkVersion = true;
				continue;
			}

			if(changelogStarted) {
				parsed.append(line).append(System.lineSeparator());
			}
		}

		return new SimpleEntry<>(RETRIEVED_FROM + ": " + url, parsed.toString());
	}

	static Map.Entry<String, String> getBoPChangelog(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException {
		if(url) {
			return new SimpleEntry<>(VIEW_CHANGELOG_AT, newFile.urlString());
		}

		String[] split = oldFile.name().split("-");
		final String oldVersion = split[1] + '-' + split[2];

		split = newFile.name().split("-");
		final String newVersion = split[1] + '-' + split[2];

		final String[] lines = StringUtils.splitNewline(newFile.changelog());
		final StringBuilder parsed = new StringBuilder();

		boolean changelogStarted = false;

		for(String line : lines) {
			if(line.equals("Changelog:")) {
				continue;
			}

			if(line.startsWith("Build ")) {
				final String version = StringUtils.removeLastChar(line.split(" ")[1]);
				if(!changelogStarted) {
					if(version.equals(newVersion)) {
						changelogStarted = true;
					}
				} else if(version.equals(oldVersion)) {
					break;
				}
			}

			if(changelogStarted) {
				parsed.append(line).append(System.lineSeparator());
			}
		}

		return new SimpleEntry<>(RETRIEVED_FROM + " " + newFile.name() + "'s changelog",
				parsed.toString());
	}

	static Map.Entry<String, String> getAAChangelog(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException, IOException {
		if(url) {
			return new SimpleEntry<>(VIEW_CHANGELOG_AT, ACTUALLY_ADDITIONS_CHANGELOG);
		}

		String[] split = oldFile.name().split("-");
		String oldVersion = split[1] + '-' + split[2];
		oldVersion = StringUtils.removeLastChars(oldVersion, 4);

		split = newFile.name().split("-");
		String newVersion = split[1] + '-' + split[2];
		newVersion = StringUtils.removeLastChars(newVersion, 4);

		final String[] lines =
				StringUtils.splitNewline(DocumentUtils.read(ACTUALLY_ADDITIONS_CHANGELOG));
		final StringBuilder parsed = new StringBuilder();

		boolean changelogStarted = false;

		for(String line : lines) {
			if(line.startsWith("# ")) {
				final String version = line.split(" ")[1];
				if(!changelogStarted) {
					if(version.equals(newVersion)) {
						changelogStarted = true;
					}
				} else if(version.equals(oldVersion)) {
					break;
				}
			}

			if(changelogStarted) {
				parsed.append(line).append(System.lineSeparator());
			}
		}

		return new SimpleEntry<>(RETRIEVED_FROM + ": " + ACTUALLY_ADDITIONS_CHANGELOG,
				parsed.toString());
	}

	static Map.Entry<String, String> getChangelogFromOldAndNew(CurseFile oldFile, CurseFile newFile,
			boolean url) throws CurseException {
		if(url) {
			return new SimpleEntry<>(VIEW_CHANGELOG_AT, newFile.urlString());
		}

		String changelog = newFile.changelog().replace(oldFile.changelog(), "");
		while(changelog.endsWith("\r") || changelog.endsWith("\n")) {
			changelog = StringUtils.removeLastChar(changelog);
		}

		return new SimpleEntry<>(RETRIEVED_FROM + " " + newFile.name() + "'s changelog", changelog);
	}
}
