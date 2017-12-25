package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.util.CloneException;

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
	}

	public String manifestType;
	public int manifestVersion;
	public String name;
	public String version;
	public String author;
	public String description;
	public CurseMod[] files;
	public String overrides;
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
		manifest.minecraft = minecraft;

		return manifest;
	}
}
