package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.util.CloneException;

public class CurseManifest implements Cloneable {
	public static class Mod implements Cloneable {
		public int projectID;
		public int fileID;

		public Mod() {}

		public Mod(int projectID, int fileID) {
			this.projectID = projectID;
			this.fileID = fileID;
		}

		@Override
		public Mod clone() {
			return new Mod(projectID, fileID);
		}
	}

	public String manifestType;
	public int manifestVersion;
	public String name;
	public String version;
	public String author;
	public String description;
	public Mod[] files;
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
		manifest.description = manifest.description;
		manifest.files = CloneException.tryClone(files);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft;

		return manifest;
	}
}
