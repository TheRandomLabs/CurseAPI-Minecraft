package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.FileInfo;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.curseapi.util.MiscUtils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.io.NIOUtils;

public final class ExtendedCurseManifest implements Cloneable, Serializable {
	private static final long serialVersionUID = 6601285145733232922L;

	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String version;
	public String author;
	public String description;
	public Mod[] files;
	public Mod[] serverOnlyMods = new Mod[0];
	public FileInfo[] additionalFiles = new FileInfo[0];
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;
	public int projectID;
	//OptiFine version can be null so it's easier to check whether a manifest is actually
	//extended
	public String optifineVersion;
	public int minimumRam = 3072;
	public int recommendedRam = 4096;
	public int minimumServerRam = 2048;
	public int recommendedServerRam = 3072;

	public static ExtendedCurseManifest ensureExtended(ExtendedCurseManifest manifest)
			throws CurseException {
		return manifest.isActuallyExtended() ?
				manifest : manifest.toCurseManifest().toExtendedManifest();
	}

	public boolean isActuallyExtended() {
		return optifineVersion != null;
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
		manifest.projectID = projectID;

		return manifest;
	}

	public static ExtendedCurseManifest from(String path) throws IOException {
		return from(Paths.get(path));
	}

	public static ExtendedCurseManifest from(Path path) throws IOException {
		return MiscUtils.fromJson(path, ExtendedCurseManifest.class);
	}

	@Override
	public ExtendedCurseManifest clone() {
		try {
			final ExtendedCurseManifest manifest = (ExtendedCurseManifest) super.clone();

			manifest.files = CloneException.tryClone(files);
			manifest.serverOnlyMods = CloneException.tryClone(serverOnlyMods);
			manifest.additionalFiles = CloneException.tryClone(additionalFiles);
			manifest.minecraft = minecraft.clone();

			return manifest;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	public void sort() {
		Arrays.sort(files);
		Arrays.sort(serverOnlyMods);
		Arrays.sort(additionalFiles);
	}

	public boolean containsMod(int projectID, int fileID) {
		for(Mod mod : files) {
			if(mod.projectID == projectID && mod.fileID == fileID) {
				return true;
			}
		}
		return false;
	}

	public void client() {
		removeModsIf(mod -> mod.side == Side.SERVER);
	}

	public void removeModsIf(Predicate<Mod> predicate) {
		final List<Mod> newMods = new TRLList<>(files.length);
		for(Mod mod : files) {
			if(!predicate.test(mod)) {
				newMods.add(mod);
			}
		}
		files = newMods.toArray(new Mod[0]);
	}

	public void both() {
		moveServerOnlyModsToFiles();
	}

	public void moveServerOnlyModsToFiles() {
		final TRLList<Mod> files = new TRLList<>(this.files);
		files.addAll(serverOnlyMods);
		this.files = files.toArray(new Mod[0]);
	}

	public void server() {
		removeModsIf(mod -> mod.side == Side.CLIENT);
		moveServerOnlyModsToFiles();
	}

	public TRLList<Mod> getOptionalMods() {
		final TRLList<Mod> mods = new TRLList<>();
		for(Mod mod : files) {
			if(!mod.required) {
				mods.add(mod);
			}
		}
		return mods;
	}

	public TRLList<String> getExcludedPaths(Side side) {
		final TRLList<String> paths = FileInfo.getExcludedPaths(additionalFiles, side);
		for(Mod mod : files) {
			paths.addAll(FileInfo.getExcludedPaths(mod.relatedFiles, side));
		}
		return paths;
	}

	public void writeTo(String path) throws IOException {
		writeTo(Paths.get(path));
	}

	public void writeTo(Path path) throws IOException {
		NIOUtils.write(path, toPrettyJsonWithTabs(), true);
	}

	public void writeToMinified(Path path) throws IOException {
		NIOUtils.write(path, toJson(), true);
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public String toPrettyJsonWithTabs() {
		return toPrettyJson().replaceAll(" {2}", "\t");
	}
}
