package com.therandomlabs.curseapi.minecraft.modpack.manifest;

public class Changelog {
	/*public static class UpdateInfo implements Cloneable {
		private CurseProject project;

		private final String mcVersion;

		private final ModInfo oldMod;
		private CurseFile oldModFile;

		private final ModInfo newMod;
		private CurseFile newModFile;

		UpdateInfo(String mcVersion, ModInfo oldMod, ModInfo newMod) {
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

		public ModInfo getOldMod() {
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

		public ModInfo getNewMod() {
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

		public Map<String, String> getChangelog() throws CurseException {
			if(isDowngrade()) {
				return Collections.emptyMap();
			}

			final Map<String, String> changelog = new LinkedHashMap<>();

			final CurseFileList files = getProject().files().filterMCVersionGroup(mcVersion).
					between(getOldModFile(), getNewModFile());

			for(CurseFile file : files) {
				changelog.put(file.name(), file.changelog());
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

	public static class Changelog {
		private final ModpackManifest oldManifest;
		private final ModpackManifest newManifest;

		private final TRLList<ModInfo> unchanged;
		private final TRLList<UpdateInfo> updated;
		private final TRLList<UpdateInfo> downgraded;
		private final TRLList<ModInfo> removed;
		private final TRLList<ModInfo> added;

		Changelog(ModpackManifest oldManifest, ModpackManifest newManifest) {
			this.oldManifest = oldManifest;
			this.newManifest = newManifest;

			final TRLList<ModInfo> unchanged = new TRLList<>();
			final TRLList<UpdateInfo> updated = new TRLList<>();
			final TRLList<UpdateInfo> downgraded = new TRLList<>();
			final TRLList<ModInfo> removed = new TRLList<>();
			final TRLList<ModInfo> added = new TRLList<>();

			final String mcVersion = newManifest.minecraft.version.toString();

			for(ModInfo oldMod : oldManifest.files) {
				boolean found = false;

				for(ModInfo newMod : newManifest.files) {
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

			for(ModInfo newMod : newManifest.files) {
				boolean found = false;

				for(ModInfo oldMod : oldManifest.files) {
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

		public ModpackManifest getOldManifest() {
			return oldManifest;
		}

		public ModpackManifest getNewManifest() {
			return newManifest;
		}

		public TRLList<ModInfo> getUnchanged() {
			return unchanged;
		}

		public TRLList<UpdateInfo> getUpdated() {
			return updated;
		}

		public TRLList<UpdateInfo> getDowngraded() {
			return downgraded;
		}

		public TRLList<ModInfo> getRemoved() {
			return removed;
		}

		public TRLList<ModInfo> getAdded() {
			return added;
		}

		@Override
		public String toString() {
			try {
				return ModpackManifest.changelogString(this);
			} catch(CurseException ex) {
				ex.printStackTrace();
			}
			return "";
		}
	}

	public static Changelog changelog(ModpackManifest oldManifest, ModpackManifest newManifest) {
		return new Changelog(oldManifest, newManifest);
	}

	static String changelogString(Changelog changelog) throws CurseException {
		final StringBuilder string = new StringBuilder();
		final String newline = IOConstants.LINE_SEPARATOR;

		if(!changelog.getUpdated().isEmpty()) {
			//Preload updated files
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), changelog.getUpdated().size(),
					index -> changelog.getUpdated().get(index).getNewModFile());

			string.append("Updated:");

			for(UpdateInfo updated : changelog.getUpdated()) {
				string.append(newline).append("\t").append(updated.getModTitle()).append(':');

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

			for(ModInfo removed : changelog.getRemoved()) {
				string.append(newline).append("\t").append("- ").append(removed.title);
			}

			string.append(newline).append(newline);
		}

		if(!changelog.getAdded().isEmpty()) {
			string.append("Added:");

			for(ModInfo added : changelog.getAdded()) {
				string.append(newline).append("\t").append("- ").append(added.title);
			}

			string.append(newline);
		}

		final String toString = string.toString();
		if(toString.endsWith(newline + newline)) {
			return toString.substring(0, toString.length() - newline.length());
		}
		return toString;
	}
}*/
}
