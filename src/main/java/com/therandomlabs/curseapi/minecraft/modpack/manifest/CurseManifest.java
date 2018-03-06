package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.Serializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.utils.concurrent.ThreadUtils;

public final class CurseManifest implements Cloneable, Serializable {
	private static final long serialVersionUID = -8163938330549340465L;
	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String version;
	public String author;
	public String description;
	public CurseMod[] files;
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;
	public int projectID;

	public ExtendedCurseManifest toExtendedManifest() throws CurseException {
		final ExtendedCurseManifest manifest = new ExtendedCurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CurseMod.toMods(files);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();
		manifest.projectID = projectID;

		return manifest;
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	@Override
	public CurseManifest clone() {
		try {
			final CurseManifest manifest = (CurseManifest) super.clone();

			manifest.files = CloneException.tryClone(files);
			manifest.minecraft = minecraft.clone();

			return manifest;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	public String toPrettyJsonWithTabs() {
		return toPrettyJson().replaceAll(" [2]", "\t");
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

		public static Mod[] toMods(CurseMod[] curseMods) throws CurseException {
			final Mod[] mods = new Mod[curseMods.length];
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), curseMods.length, index ->
					mods[index] = curseMods[index].toMod());
			return mods;
		}

		@Override
		public CurseMod clone() {
			try {
				return (CurseMod) super.clone();
			} catch(CloneNotSupportedException ignored) {}

			return null;
		}

		public Mod toMod() throws CurseException {
			final Mod mod = new Mod();

			mod.title = CurseProject.fromID(projectID).title();
			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.required = required;

			return mod;
		}

		@Override
		public String toString() {
			return "[projectID=" + projectID + ",fileID=" + fileID + ",required=" + required + "]";
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

		public static CurseMod fromMod(Mod mod) {
			return new CurseMod(mod.projectID, mod.fileID, mod.required);
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof CurseMod && object.hashCode() == hashCode();
		}
	}
}
