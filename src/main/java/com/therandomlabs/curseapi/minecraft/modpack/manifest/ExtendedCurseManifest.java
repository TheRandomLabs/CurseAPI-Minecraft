package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.util.Arrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String version;
	public String author;
	public String description;
	public Mod[] files;
	public Mod[] serverOnlyMods = new Mod[0];
	public Mod[] alternativeMods = new Mod[0];
	public GroupInfo[] groups = new GroupInfo[0];
	public FileInfo[] additionalFiles = new FileInfo[0];
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;
	public String optifineVersion = "latest";
	public int minimumRam = 3072;
	public int recommendedRam = 4096;

	@Override
	public ExtendedCurseManifest clone() {
		final ExtendedCurseManifest manifest = new ExtendedCurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CloneException.tryClone(files);
		manifest.serverOnlyMods = CloneException.tryClone(serverOnlyMods);
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

	public CurseManifest toCurseManifest() {
		final CurseManifest manifest = new CurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CurseManifest.CurseMod.fromMods(files);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();

		return manifest;
	}

	public void sort() {
		Arrays.sort(files, (mod1, mod2) -> mod1.title.compareTo(mod2.title));
		Arrays.sort(serverOnlyMods, (mod1, mod2) -> mod1.title.compareTo(mod2.title));
		Arrays.sort(alternativeMods, (mod1, mod2) -> mod1.title.compareTo(mod2.title));
		Arrays.sort(additionalFiles, (file1, file2) -> file1.path.compareTo(file2.path));
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public String toPrettyJsonWithTabs() {
		return toPrettyJson().replaceAll("  ", "\t");
	}
}
