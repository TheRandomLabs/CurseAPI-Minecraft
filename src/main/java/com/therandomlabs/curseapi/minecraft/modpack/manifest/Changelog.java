package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseFile;
import com.therandomlabs.curseapi.CurseFileList;
import com.therandomlabs.curseapi.CurseProject;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.concurrent.ThreadUtils;
import com.therandomlabs.utils.io.IOConstants;
import com.therandomlabs.utils.misc.StringUtils;

public class Changelog {
	public static class UpdateInfo implements Cloneable {
		private CurseProject project;

		private final String mcVersion;

		private final Mod oldMod;
		private CurseFile oldModFile;

		private final Mod newMod;
		private CurseFile newModFile;

		UpdateInfo(String mcVersion, Mod oldMod, Mod newMod) {
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

		public CurseFile getOldModFile() throws CurseException {
			if(oldModFile == null) {
				oldModFile = getProject().fileFromID(oldMod.fileID);
			}
			return oldModFile;
		}

		public String getOldModName() throws CurseException {
			return getOldModFile().name();
		}

		public Mod getNewMod() {
			return newMod;
		}

		public CurseFile getNewModFile() throws CurseException {
			if(newModFile == null) {
				newModFile = getProject().fileFromID(newMod.fileID);
			}
			return newModFile;
		}

		public String getNewModName() throws CurseException {
			return getNewModFile().name();
		}

		public Map<String, String> getChangelog() throws CurseException, IOException {
			if(isDowngrade()) {
				return Collections.emptyMap();
			}

			final Map<String, String> changelog = new LinkedHashMap<>();

			if(getNewModFile().uploader().equals("TeamCoFH")) {
				try {
					//CoFH just links to a txt file on their GitHub, so we do some black magic
					String url = getNewModFile().changelog().trim();
					url = url.split("]")[0].substring(1);
					url = url.replace("/blob", "");
					url = url.replace("github", "raw.githubusercontent");

					final String fullChangelog = DocumentUtils.read(url);

					String oldVersion = getOldModName().split("-")[2];
					int lengthToRemove = ArrayUtils.last(oldVersion.split("\\.")).length() + 1;
					oldVersion = oldVersion.substring(0, oldVersion.length() - lengthToRemove);

					String newVersion = getNewModName().split("-")[2];
					lengthToRemove = ArrayUtils.last(newVersion.split("\\.")).length() + 1;
					newVersion = newVersion.substring(0, newVersion.length() - lengthToRemove);

					final String[] lines = StringUtils.splitNewline(fullChangelog);
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
								if(line.substring(0, line.length() - 1).equals(oldVersion)) {
									break;
								}
							} else {
								if(line.substring(0, line.length() - 1).equals(newVersion)) {
									changelogStarted = true;
								}
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

					changelog.put("Changelog retrieved from GitHub", parsed.toString());
				} catch(CurseException ex) {
					if(!(ex.getCause() instanceof MalformedURLException)) {
						throw ex;
					}
				}
			}

			final CurseFileList files;
			if(getNewModFile().uploader().equals("mezz")) {
				//99% of the time, all of the needed information will just be in the
				//newest changelog due to how mezz does changelogs
				files = CurseFileList.of(getNewModFile());
			} else {
				files = getProject().files().filterVersions(mcVersion).
						between(getOldModFile(), getNewModFile());
			}

			for(int i = files.size() - 1; i >= 0; i--) {
				changelog.put(files.get(i).name(), files.get(i).changelog());
			}

			return changelog;
		}

		public boolean isDowngrade() {
			return oldMod.fileID > newMod.fileID;
		}

		@Override
		public UpdateInfo clone() {
			return new UpdateInfo(mcVersion, oldMod.clone(), newMod.clone());
		}
	}

	private final ExtendedCurseManifest oldManifest;
	private final ExtendedCurseManifest newManifest;

	private final TRLList<Mod> unchanged;
	private final TRLList<UpdateInfo> updated;
	private final TRLList<UpdateInfo> downgraded;
	private final TRLList<Mod> removed;
	private final TRLList<Mod> added;

	Changelog(ExtendedCurseManifest oldManifest, ExtendedCurseManifest newManifest) {
		this.oldManifest = oldManifest;
		this.newManifest = newManifest;

		final TRLList<Mod> unchanged = new TRLList<>();
		final TRLList<UpdateInfo> updated = new TRLList<>();
		final TRLList<UpdateInfo> downgraded = new TRLList<>();
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
						updated.add(new UpdateInfo(mcVersion, oldMod, newMod));
						break;
					}

					downgraded.add(new UpdateInfo(mcVersion, newMod, oldMod));
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

		this.unchanged = unchanged.toImmutableList();
		this.updated = updated.toImmutableList();
		this.downgraded = downgraded.toImmutableList();
		this.removed = removed.toImmutableList();
		this.added = added.toImmutableList();
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

	public TRLList<UpdateInfo> getUpdated() {
		return updated;
	}

	public TRLList<UpdateInfo> getDowngraded() {
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

	public String tryToString() throws CurseException, IOException {
		return changelogString(this);
	}

	@Override
	public String toString() {
		try {
			return tryToString();
		} catch(CurseException | IOException ex) {
			ex.printStackTrace();
		}
		return "";
	}

	public static Changelog changelog(ExtendedCurseManifest oldManifest,
			ExtendedCurseManifest newManifest) {
		return new Changelog(oldManifest, newManifest);
	}

	private static String changelogString(Changelog changelog) throws CurseException, IOException {
		final StringBuilder string = new StringBuilder();
		final String newline = IOConstants.LINE_SEPARATOR;

		string.append(changelog.getOldManifest().name + ' ' + changelog.getOldManifest().version).
				append(" to ").
				append(changelog.getNewManifest().name + ' ' + changelog.getNewManifest().version).
				append(newline).append(newline);

		if(!changelog.getAdded().isEmpty()) {
			string.append("Added:");

			for(Mod added : changelog.getAdded()) {
				string.append(newline).append("\t").append("- ").append(added.title);
			}

			string.append(newline).append(newline);
		}

		if(!changelog.getUpdated().isEmpty()) {
			//Preload updated files
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), changelog.getUpdated().size(),
					index -> changelog.getUpdated().get(index).getNewModFile());

			string.append("Updated:");

			for(UpdateInfo updated : changelog.getUpdated()) {
				string.append(newline).append("\t").append(updated.getModTitle()).
						append(" (went from ").append(updated.getOldModName()).append(" to ").
						append(updated.getNewModName()).append("):");

				for(Map.Entry<String, String> modChangelog : updated.getChangelog().entrySet()) {
					string.append(newline).append("\t\t").append(modChangelog.getKey()).
							append(':');

					final String[] lines = StringUtils.NEWLINE.split(modChangelog.getValue());
					for(String line : lines) {
						string.append(newline);

						if(!line.trim().isEmpty()) {
							string.append("\t\t\t").append(line);
						}
					}
				}

				string.append(newline);
			}

			string.append(newline);
		}

		if(!changelog.getDowngraded().isEmpty()) {
			string.append("Downgraded:");

			for(UpdateInfo downgraded : changelog.getDowngraded()) {
				string.append(newline).append("\t").append("- From ").
						append(downgraded.getOldModName()).append(" to ").
						append(downgraded.getNewModName());
			}

			string.append(newline).append(newline);
		}

		if(!changelog.getRemoved().isEmpty()) {
			string.append("Removed:");

			for(Mod removed : changelog.getRemoved()) {
				string.append(newline).append("\t").append("- ").append(removed.title);
			}

			string.append(newline).append(newline);
		}

		if(changelog.hasForgeVersionChanged()) {
			string.append("Went from Forge ").append(changelog.getOldForgeVersion()).
					append(" to ").append(changelog.getNewForgeVersion()).append('.').
					append(newline);
		}

		final String toString = string.toString();
		if(toString.endsWith(newline + newline)) {
			return toString.substring(0, toString.length() - newline.length());
		}
		return toString;
	}
}
