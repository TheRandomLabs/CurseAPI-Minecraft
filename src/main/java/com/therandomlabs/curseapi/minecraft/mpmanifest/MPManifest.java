package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.Serializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
import com.therandomlabs.curseapi.project.ProjectType;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.misc.ThreadUtils;

public final class MPManifest implements Cloneable, Serializable {
	public static class CurseMod implements Cloneable, Serializable {
		private static final long serialVersionUID = -6936293567291965636L;

		public int projectID;
		public int fileID;
		public boolean required;

		public CurseMod() {}

		public CurseMod(int projectID, int fileID, boolean required) {
			this.projectID = projectID;
			this.fileID = fileID;
			this.required = required;
		}

		public Mod toMod() throws CurseException {
			return toMod(true);
		}

		public Mod toMod(boolean downloadExtendedData) throws CurseException {
			final Mod mod = new Mod();

			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.required = required;

			CurseProject project = null;

			if(downloadExtendedData) {
				final boolean cached = CurseProject.isCached(projectID);

				if(!cached) {
					MCEventHandling.forEach(handler -> handler.downloadingModData(projectID));
				}

				try {
					project = CurseProject.fromID(projectID);

					if(!cached) {
						final CurseProject curseProject = project;
						MCEventHandling.forEach(handler -> handler.downloadedModData(curseProject));
					}
				} catch(InvalidProjectIDException ignored) {}
			}

			if(project == null) {
				mod.title = CurseProject.UNKNOWN_TITLE;
			} else {
				mod.title = project.title();
				mod.isResourcePack = project.type() == ProjectType.Minecraft.TEXTURE_PACKS;
			}

			return mod;
		}

		@Override
		public CurseMod clone() {
			try {
				return (CurseMod) super.clone();
			} catch(CloneNotSupportedException ignored) {}

			return null;
		}

		public static Mod[] toMods(CurseMod[] curseMods) throws CurseException {
			return toMods(curseMods, true);
		}

		public static Mod[] toMods(CurseMod[] curseMods, boolean downloadModData)
				throws CurseException {
			final Mod[] mods = new Mod[curseMods.length];
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), curseMods.length,
					index -> mods[index] = curseMods[index].toMod(downloadModData));
			return mods;
		}

		public static CurseMod[] fromMods(Mod[] mods) {
			final CurseMod[] curseMods = new CurseMod[mods.length];
			for(int i = 0; i < mods.length; i++) {
				curseMods[i] = fromMod(mods[i]);
			}
			return curseMods;
		}

		@Override
		public int hashCode() {
			return projectID + fileID;
		}

		@Override
		public String toString() {
			return "[projectID=" + projectID + ",fileID=" + fileID + ",required=" + required + "]";
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof CurseMod && object.hashCode() == hashCode();
		}

		public static CurseMod fromMod(Mod mod) {
			return new CurseMod(mod.projectID, mod.fileID, mod.required);
		}
	}

	private static final long serialVersionUID = -8163938330549340465L;

	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String version;
	public String author;
	public String description;
	public CurseMod[] files;
	public String overrides = "overrides";
	public MinecraftInfo minecraft;
	public int projectID;

	public ExtendedMPManifest toExtendedManifest() throws CurseException {
		return toExtendedManifest(true);
	}

	public ExtendedMPManifest toExtendedManifest(boolean downloadModData) throws CurseException {
		final ExtendedMPManifest manifest = new ExtendedMPManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.id = ExtendedMPManifest.asID(name);
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CurseMod.toMods(files, downloadModData);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();
		manifest.projectID = projectID;
		manifest.optifineVersion = "latest";

		return manifest;
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toPrettyJsonWithTabs() {
		return StringUtils.replaceSpacesWithTabs(toPrettyJson(), 2);
	}

	@Override
	public MPManifest clone() {
		try {
			final MPManifest manifest = (MPManifest) super.clone();

			manifest.files = Utils.tryClone(files);
			manifest.minecraft = minecraft.clone();

			return manifest;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public boolean containsMod(int projectID, int fileID) {
		for(CurseMod mod : files) {
			if(mod.projectID == projectID && mod.fileID == fileID) {
				return true;
			}
		}
		return false;
	}
}
