package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.Serializable;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
import com.therandomlabs.curseapi.util.CloneException;
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

		public static Mod[] toMods(CurseMod[] curseMods) throws CurseException {
			final Mod[] mods = new Mod[curseMods.length];
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), curseMods.length, index ->
					mods[index] = curseMods[index].toMod());
			return mods;
		}

		public Mod toMod() throws CurseException {
			final Mod mod = new Mod();

			try {
				mod.title = CurseProject.fromID(projectID).title();
			} catch(InvalidProjectIDException ex) {
				mod.title = Mod.UNKNOWN_NAME;
			}
			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.required = required;

			return mod;
		}

		@Override
		public CurseMod clone() {
			try {
				return (CurseMod) super.clone();
			} catch(CloneNotSupportedException ignored) {}

			return null;
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
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;
	public int projectID;

	public ExtendedMPManifest toExtendedManifest() throws CurseException {
		final ExtendedMPManifest manifest = new ExtendedMPManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.id = StringUtils.replaceWhitespace(name.toLowerCase(Locale.ROOT), "_");
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

	public String toPrettyJsonWithTabs() {
		return toPrettyJson().replaceAll(" {2}", "\t");
	}

	@Override
	public MPManifest clone() {
		try {
			final MPManifest manifest = (MPManifest) super.clone();

			manifest.files = CloneException.tryClone(files);
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
