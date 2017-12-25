package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.minecraft.modpack.FileInfo;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.util.CloneException;

public class ExtendedCurseManifest implements Cloneable {
	public static class GroupInfo implements Cloneable {
		public String primary;
		public String[] alternatives;

		public GroupInfo(String primary, String[] alternatives) {
			this.primary = primary;
			this.alternatives = alternatives;
		}

		@Override
		public GroupInfo clone() {
			return new GroupInfo(primary, alternatives);
		}
	}

	public String manifestType;
	public int manifestVersion;
	public String name;
	public String version;
	public String author;
	public String description;
	public Mod[] files;
	public Mod[] alternativeMods;
	public GroupInfo[] groups;
	public FileInfo[] additionalFiles = new FileInfo[0];
	public String overrides;
	public MinecraftInfo minecraft;
	public String optifineVersion = "latest";
	public double minimumRam = 3.0;
	public double recommendedRam = 4.0;

	@Override
	public ExtendedCurseManifest clone() {
		final ExtendedCurseManifest manifest = new ExtendedCurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = manifest.description;
		manifest.files = CloneException.tryClone(files);
		manifest.alternativeMods = CloneException.tryClone(alternativeMods);
		manifest.groups = CloneException.tryClone(groups);
		manifest.additionalFiles = CloneException.tryClone(additionalFiles);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();
		manifest.optifineVersion = optifineVersion;
		manifest.minimumRam = minimumRam;
		manifest.recommendedRam = recommendedRam;

		return manifest;
	}
}
