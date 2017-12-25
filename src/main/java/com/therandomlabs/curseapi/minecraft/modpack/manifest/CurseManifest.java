package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseProject;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.utils.concurrent.ThreadUtils;

public class CurseManifest implements Cloneable {
	public static class CurseMod implements Cloneable {
		public int projectID;
		public int fileID;

		public CurseMod() {}

		public CurseMod(int projectID, int fileID) {
			this.projectID = projectID;
			this.fileID = fileID;
		}

		@Override
		public CurseMod clone() {
			return new CurseMod(projectID, fileID);
		}

		public Mod toMod() throws CurseException {
			final Mod mod = new Mod();

			mod.title = CurseProject.fromID(projectID).title();
			mod.projectID = projectID;
			mod.fileID = fileID;

			return mod;
		}

		public static Mod[] toMods(CurseMod[] curseMods) throws CurseException {
			final Mod[] mods = new Mod[curseMods.length];
			ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), curseMods.length, index -> {
				mods[index] = curseMods[index].toMod();
			});
			return mods;
		}

		public static CurseMod fromMod(Mod mod) {
			return new CurseMod(mod.projectID, mod.fileID);
		}

		public static CurseMod[] fromMods(Mod[] mods) {
			final CurseMod[] curseMods = new CurseMod[mods.length];
			for(int i = 0; i < mods.length; i++) {
				curseMods[i] = fromMod(mods[i]);
			}
			return curseMods;
		}
	}

	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String version;
	public String author;
	public String description;
	public CurseMod[] files;
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;

	@Override
	public CurseManifest clone() {
		final CurseManifest manifest = new CurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CloneException.tryClone(files);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();

		return manifest;
	}

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

		return manifest;
	}
}
