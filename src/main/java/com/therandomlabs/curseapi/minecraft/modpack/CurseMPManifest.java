package com.therandomlabs.curseapi.minecraft.modpack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class CurseMPManifest extends MPManifest {
	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;

	public String name;

	public String version;
	public String author;
	public String description;

	public CurseMod[] files;

	public String overrides = "overrides";
	public MinecraftInfo minecraft;
	public int projectID;

	private boolean downloadExtendedFileData;

	@Override
	public CurseMPManifest clone() {
		final CurseMPManifest manifest = (CurseMPManifest) getCloned();

		manifest.files = Utils.tryClone(files);
		manifest.minecraft = minecraft.clone();

		return manifest;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public String author() {
		return author;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public ModList universalFiles() {
		if(downloadExtendedFileData) {
			try {
				return modList(CurseMod.toExtendedMods(files));
			} catch(CurseException ex) {
				ThrowableHandling.handleWithoutExit(ex);
			}
		}

		return modList(CurseMod.toExtendedModsWithoutExtendedData(files));
	}

	@Override
	public String overrides() {
		return overrides;
	}

	@Override
	public MCVersion mcVersion() {
		return minecraft.version;
	}

	@Override
	public String forgeVersion() {
		return minecraft.forgeVersion();
	}

	@Override
	public int projectID() {
		return projectID;
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void validate() {
		Assertions.equals(manifestType, "manifestType", "minecraftModpack");
		Assertions.equals(manifestVersion, "manifestVersion", 1);
		Assertions.nonEmpty(name, "name");
		Assertions.nonEmpty(version, "version");
		Assertions.nonEmpty(author, "author");
		Assertions.nonEmpty(description, "description");
		Assertions.nonNull(files, "files");

		for(CurseMod mod : files) {
			mod.validate();
		}

		Assertions.validPath(overrides);

		minecraft.validate();

		if(projectID != 0) {
			CurseAPI.validateProjectID(projectID);
		}
	}

	@Override
	public void sort() {
		Arrays.sort(files);
	}

	public boolean downloadExtendedFileData() {
		return downloadExtendedFileData;
	}

	public void downloadExtendedFileData(boolean flag) {
		downloadExtendedFileData = flag;
	}

	public static CurseMPManifest from(Path path) throws IOException {
		return Utils.fromJson(path, CurseMPManifest.class);
	}
}
