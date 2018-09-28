package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.CurseAPIMinecraft;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.io.IOUtils;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

//TODO rewrite/redo
public final class ExtendedMPManifest implements Cloneable {
	public static final String UNKNOWN_PROJECT_URL = "Unknown project URL";

	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String id = "unknown";
	public String version;
	public String author;
	public String description;
	public Mod[] files;
	public Mod[] disabledMods = new Mod[0];
	public FileInfo[] additionalFiles = new FileInfo[0];
	public String overrides = "overrides";
	public MinecraftInfo minecraft;
	public int projectID;
	public String projectURL = UNKNOWN_PROJECT_URL;
	//OptiFine version can be null so it's easier to check whether a manifest is actually
	//extended
	public String optifineVersion;
	public int minimumRam = 3072;
	public int recommendedRam = 4096;
	public int minimumServerRam = 2048;
	public int recommendedServerRam = 3072;

	@Override
	public ExtendedMPManifest clone() {
		try {
			final ExtendedMPManifest manifest = (ExtendedMPManifest) super.clone();

			manifest.files = Utils.tryClone(files);
			manifest.disabledMods = Utils.tryClone(disabledMods);
			manifest.additionalFiles = Utils.tryClone(additionalFiles);
			manifest.minecraft = minecraft.clone();

			return manifest;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	public MPManifest toCurseManifest() {
		final MPManifest manifest = new MPManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = MPManifest.CurseMod.fromMods(files);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();
		manifest.projectID = projectID;

		return manifest;
	}

	public void validate() {
		Assertions.equals(manifestType, "manifestType", "minecraftModpack");
		Assertions.equals(manifestVersion, "manifestVersion", 1);
		Assertions.nonEmpty(name, "name");
		Assertions.nonEmpty(id, "id");
		Assertions.nonEmpty(version, "version");
		Assertions.nonEmpty(author, "author");
		Assertions.nonEmpty(description, "description");

		Assertions.nonNull(files, "files");

		for(Mod mod : files) {
			mod.validate();

			for(int dependent : mod.dependents) {
				if(!contains(dependent)) {
					throw new IllegalStateException("No dependent found with ID: " + dependent);
				}
			}
		}

		Assertions.nonNull(disabledMods, "disabledMods");

		for(Mod mod : disabledMods) {
			mod.validate();

			if(mod.dependents.isEmpty()) {
				continue;
			}

			for(int dependent : mod.dependents) {
				if(!contains(dependent)) {
					throw new IllegalStateException("No dependent found with ID: " + dependent);
				}

				if(containsAndIsEnabled(dependent)) {
					throw new IllegalStateException(String.format("Dependent (%s) is enabled " +
							"but dependency (%s) isn't", dependent, mod.projectID));
				}
			}
		}

		Assertions.nonNull(additionalFiles, "additionalFiles");
		Arrays.stream(additionalFiles).forEach(FileInfo::validate);

		Assertions.validPath(overrides);

		minecraft.validate();

		if(projectID != 0) {
			CurseAPI.validateProjectID(projectID);
		}

		Assertions.nonNull(projectURL, "projectURL");

		if(!projectURL.equals(UNKNOWN_PROJECT_URL)) {
			Assertions.validURL(projectURL);
		}

		Assertions.nonEmpty(optifineVersion, "optifineVersion");

		Assertions.positive(minimumRam, "minimumRam", false);
		Assertions.positive(recommendedRam, "recommendedRam", false);
		Assertions.positive(minimumServerRam, "minimumServerRam", false);
		Assertions.positive(recommendedServerRam, "recommendedServerRam", false);
	}

	public void resetDisabledMods() {
		disableIf(mod -> mod.side == Side.SERVER || mod.disabledByDefault);
	}

	public void sort() {
		Arrays.sort(files);
		Arrays.sort(disabledMods);
		Arrays.sort(additionalFiles);
	}

	public int getFileID(int projectID) {
		CurseAPI.validateProjectID(projectID);

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
		CurseAPI.validateProjectID(projectID);

		for(Mod mod : files) {
			if(mod.projectID == projectID) {
				return true;
			}
		}

		return false;
	}

	public boolean containsAndIsEnabled(int projectID, int fileID) {
		CurseAPI.validateProjectID(projectID);
		CurseAPI.validateFileID(fileID);

		for(Mod mod : files) {
			if(mod.projectID == projectID && (fileID == 0 || mod.fileID == fileID)) {
				return true;
			}
		}

		return false;
	}

	public boolean isDisabled(int projectID) {
		CurseAPI.validateProjectID(projectID);

		for(Mod mod : disabledMods) {
			if(mod.projectID == projectID) {
				return true;
			}
		}

		return false;
	}

	public boolean enable(int projectID) {
		return enableIf(mod -> mod.projectID == projectID);
	}

	public boolean disable(int projectID) {
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

	public void enableAll() {
		enableIf(mod -> true);
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
			paths.addAll(CurseAPIMinecraft.CLIENT_ONLY_FILES);
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
		return StringUtils.replaceSpacesWithTabs(toPrettyJson(), 2);
	}

	public void writeTo(String path) throws IOException {
		writeTo(Paths.get(path));
	}

	public void writeTo(Path path) throws IOException {
		IOUtils.write(path, toJson());
	}

	public void writeToPretty(String path) throws IOException {
		writeToPretty(Paths.get(path));
	}

	public void writeToPretty(Path path) throws IOException {
		IOUtils.write(path, toPrettyJsonWithTabs());
	}

	private boolean move(boolean enable, Predicate<Mod> predicate) {
		final List<Mod> newMods = new TRLList<>(files);
		final List<Mod> newDisabledMods = new TRLList<>(disabledMods);

		final List<Mod> source = enable ? newDisabledMods : newMods;
		final List<Mod> destination = enable ? newMods : newDisabledMods;

		boolean moved = false;

		final Iterator<Mod> it = source.iterator();

		while(it.hasNext()) {
			final Mod mod = it.next();

			if(predicate.test(mod)) {
				destination.add(mod);
				it.remove();
				moved = true;
			}
		}

		files = newMods.toArray(new Mod[0]);
		disabledMods = newDisabledMods.toArray(new Mod[0]);

		return moved;
	}

	private boolean isActuallyExtended() {
		return !id.isEmpty() && optifineVersion != null;
	}

	public static ExtendedMPManifest from(String path) throws IOException {
		return from(path, false);
	}

	public static ExtendedMPManifest from(String path, boolean downloadModData) throws IOException {
		return from(Paths.get(path), downloadModData);
	}

	public static ExtendedMPManifest from(Path path) throws IOException {
		return from(path, false);
	}

	public static ExtendedMPManifest from(Path path, boolean downloadModData) throws IOException {
		return tryEnsureExtended(Utils.fromJson(path, ExtendedMPManifest.class),
				downloadModData);
	}

	public static boolean isValidStringID(String id) {
		return StringUtils.isLowerCase(id, Locale.ROOT) && !StringUtils.containsWhitespace(id);
	}

	public static ExtendedMPManifest ensureExtended(ExtendedMPManifest manifest)
			throws CurseException {
		return ensureExtended(manifest, true);
	}

	public static ExtendedMPManifest ensureExtended(ExtendedMPManifest manifest,
			boolean downloadModData) throws CurseException {
		return manifest.isActuallyExtended() ?
				manifest : manifest.toCurseManifest().toExtendedManifest(downloadModData);
	}

	public static ExtendedMPManifest ofFiles(String name, CurseFileList files)
			throws CurseException {
		Assertions.nonNull(name, "name");
		Assertions.nonNull(files, "files");

		final MCVersion version;

		if(files.isEmpty()) {
			version = MCVersion.V1_12_2; //TODO latest with Forge
		} else {
			version = files.get(0).gameVersion();
		}

		final ExtendedMPManifest manifest = new ExtendedMPManifest();

		manifest.name = name;
		manifest.id = asID(name);
		manifest.version = version + "-1.0.0.0";
		manifest.author = "CurseAPI-Minecraft";
		manifest.description = "Extended mpmanifest manifest generated by CurseAPI-Minecraft";

		manifest.files = new Mod[files.size()];
		for(int i = 0; i < manifest.files.length; i++) {
			manifest.files[i] = Mod.fromFile(files.get(i));
		}

		manifest.minecraft = new MinecraftInfo(MinecraftForge.getLatestVersion(version));

		return manifest;
	}

	static String asID(String name) {
		return StringUtils.replaceWhitespace(name.toLowerCase(Locale.ROOT), "_");
	}

	private static ExtendedMPManifest tryEnsureExtended(ExtendedMPManifest manifest,
			boolean downloadModData) {
		try {
			return ensureExtended(manifest, downloadModData);
		} catch(CurseException ex) {
			//If there's an error, just use the unextended manifest
			ThrowableHandling.handleWithoutExit(ex);
		}
		return manifest;
	}
}
