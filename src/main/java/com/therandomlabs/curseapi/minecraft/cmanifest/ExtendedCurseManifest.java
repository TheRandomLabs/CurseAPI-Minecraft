package com.therandomlabs.curseapi.minecraft.cmanifest;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.FileInfo;
import com.therandomlabs.curseapi.minecraft.Minecraft;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.curseapi.util.MiscUtils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class ExtendedCurseManifest implements Cloneable, Serializable {
	private static final long serialVersionUID = 6601285145733232922L;

	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String id = "";
	public String version;
	public String author;
	public String description;
	public Mod[] files;
	public Mod[] disabledMods = new Mod[0];
	public FileInfo[] additionalFiles = new FileInfo[0];
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;
	public int projectID;
	public String projectURL = "Unknown project URL";
	//OptiFine version can be null so it's easier to check whether a manifest is actually
	//extended
	public String optifineVersion;
	public int minimumRam = 3072;
	public int recommendedRam = 4096;
	public int minimumServerRam = 2048;
	public int recommendedServerRam = 3072;

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

	@Override
	public ExtendedCurseManifest clone() {
		try {
			final ExtendedCurseManifest manifest = (ExtendedCurseManifest) super.clone();

			manifest.files = CloneException.tryClone(files);
			manifest.disabledMods = CloneException.tryClone(disabledMods);
			manifest.additionalFiles = CloneException.tryClone(additionalFiles);
			manifest.minecraft = minecraft.clone();

			return manifest;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	public void validate() throws CurseException {
		//TODO
	}

	public void sort() {
		Arrays.sort(files);
		Arrays.sort(disabledMods);
		Arrays.sort(additionalFiles);
	}

	public int getFileID(int projectID) {
		CurseAPI.validateID(projectID);

		for(Mod mod : files) {
			if(mod.projectID == projectID) {
				return mod.fileID;
			}
		}

		for(Mod mod : disabledMods) {
			if(mod.projectID == projectID) {
				return mod.fileID;
			}
		}

		return 0;
	}

	public boolean contains(int projectID) {
		return getFileID(projectID) != 0;
	}

	public boolean contains(int projectID, int fileID) {
		return getFileID(projectID) == fileID;
	}

	public boolean containsAndIsEnabled(int projectID) {
		CurseAPI.validateID(projectID);

		for(Mod mod : files) {
			if(mod.projectID == projectID) {
				return true;
			}
		}

		return false;
	}

	public boolean containsAndIsEnabled(int projectID, int fileID) {
		CurseAPI.validateID(projectID, fileID);

		for(Mod mod : files) {
			if(mod.projectID == projectID && (fileID == 0 || mod.fileID == fileID)) {
				return true;
			}
		}

		return false;
	}

	public boolean enable(int projectID) {
		return enableIf(mod -> mod.projectID == projectID);
	}

	public boolean disable(int fileID) {
		return disableIf(mod -> mod.projectID == projectID);
	}

	public boolean enableIf(Predicate<Mod> predicate) {
		return move(true, predicate);
	}

	public boolean disableIf(Predicate<Mod> predicate) {
		return move(false, predicate);
	}

	public void enableMods(Side side) {
		enableIf(mod -> mod.side == side);
	}

	public void disableMods(Side side) {
		disableIf(mod -> mod.side == side);
	}

	private boolean move(boolean enable, Predicate<Mod> predicate) {
		final List<Mod> newMods = new TRLList<>(files.length);
		final List<Mod> newDisabledMods = new TRLList<>(disabledMods.length);

		boolean moved = false;

		for(Mod mod : enable ? disabledMods : files) {
			if(predicate.test(mod)) {
				(enable ? newMods : newDisabledMods).add(mod);
				moved = true;
			} else {
				(enable ? newDisabledMods : newMods).add(mod);
			}
		}

		files = newMods.toArray(new Mod[0]);
		disabledMods = newDisabledMods.toArray(new Mod[0]);

		return moved;
	}

	public TRLList<Mod> getAllMods() {
		final TRLList<Mod> mods = new TRLList<>(files);
		mods.addAll(disabledMods);
		return mods;
	}

	public TRLList<Mod> getMods(Predicate<Mod> predicate) {
		final TRLList<Mod> mods = new TRLList<>();

		for(Mod mod : files) {
			if(predicate.test(mod)) {
				mods.add(mod);
			}
		}

		for(Mod mod : disabledMods) {
			if(predicate.test(mod)) {
				mods.add(mod);
			}
		}

		return mods;
	}

	public TRLList<Mod> getMods(Side side) {
		return getMods(mod -> mod.side == side);
	}

	public TRLList<Mod> getOptionalMods() {
		return getMods(mod -> !mod.required);
	}

	public TRLList<String> getExcludedPaths(Side side) {
		final TRLList<String> paths = FileInfo.getExcludedPaths(additionalFiles, side);

		for(Mod mod : files) {
			paths.addAll(FileInfo.getExcludedPaths(mod.relatedFiles, side));
		}

		if(side == Side.SERVER) {
			paths.addAll(Minecraft.CLIENT_ONLY_FILES);
		}

		return paths;
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

	public void writeTo(String path) throws IOException {
		writeTo(Paths.get(path));
	}

	public void writeTo(Path path) throws IOException {
		NIOUtils.write(path, toJson(), true);
	}

	public void writeToPretty(String path) throws IOException {
		writeToPretty(Paths.get(path));
	}

	public void writeToPretty(Path path) throws IOException {
		NIOUtils.write(path, toPrettyJsonWithTabs(), true);
	}

	private boolean isActuallyExtended() {
		return !id.isEmpty() && optifineVersion != null;
	}

	public static ExtendedCurseManifest from(String path) throws IOException {
		return from(Paths.get(path));
	}

	public static ExtendedCurseManifest from(Path path) throws IOException {
		return tryEnsureExtended(MiscUtils.fromJson(path, ExtendedCurseManifest.class));
	}

	public static boolean isValidStringID(String id) {
		return StringUtils.isLowerCase(id, Locale.ROOT) && !StringUtils.containsWhitespace(id);
	}

	public static ExtendedCurseManifest ensureExtended(ExtendedCurseManifest manifest)
			throws CurseException {
		return manifest.isActuallyExtended() ?
				manifest : manifest.toCurseManifest().toExtendedManifest();
	}

	private static ExtendedCurseManifest tryEnsureExtended(ExtendedCurseManifest manifest) {
		try {
			return ensureExtended(manifest);
		} catch(CurseException ex) {
			//If there's an error, just use the unextended manifest
			ThrowableHandling.handleWithoutExit(ex);
		}
		return manifest;
	}
}
