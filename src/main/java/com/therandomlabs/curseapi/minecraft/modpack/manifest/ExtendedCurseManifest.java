package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.minecraft.modpack.FileSide;
import com.therandomlabs.curseapi.util.CloneException;

public class ExtendedCurseManifest implements Cloneable {
	public static class Mod implements Cloneable {
		public String title = UNKNOWN_NAME;
		public int projectID;
		public int fileID;
		public FileSide side = FileSide.BOTH;
		public boolean optional;
		public FileInfo[] relatedFiles = new FileInfo[0];

		@Override
		public Mod clone() {
			final Mod mod = new Mod();

			mod.title = title;
			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.side = side;
			mod.optional = optional;
			mod.relatedFiles = CloneException.tryClone(relatedFiles);

			return mod;
		}
	}

	public static class ModWithAlternatives extends Mod {
		public String group;
		public String[] alternativeGroups;

		@Override
		public ModWithAlternatives clone() {
			final ModWithAlternatives mod = new ModWithAlternatives();

			mod.title = title;
			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.side = side;
			mod.optional = optional;
			mod.relatedFiles = CloneException.tryClone(relatedFiles);
			mod.group = group;
			mod.alternativeGroups = alternativeGroups.clone();

			return mod;
		}
	}

	public static final String UNKNOWN_NAME = "Unknown Name";

	public String manifestType;
	public int manifestVersion;
	public String name;
	public String version;
	public String author;
	public String description;
	public Mod[] files;
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
		manifest.overrides = overrides;
		manifest.minecraft = minecraft;
		manifest.optifineVersion = optifineVersion;
		manifest.minimumRam = minimumRam;
		manifest.recommendedRam = recommendedRam;

		return manifest;
	}
}
