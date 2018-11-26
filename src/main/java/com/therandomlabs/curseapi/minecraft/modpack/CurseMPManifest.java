package com.therandomlabs.curseapi.minecraft.modpack;

import java.io.IOException;
import java.nio.file.Path;
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
		final CurseMPManifest manifest = (CurseMPManifest) super.clone();

		manifest.files = Utils.tryClone(files);
		manifest.minecraft = minecraft.clone();

		return manifest;
	}

	@Override
	protected String name() {
		return name;
	}

	@Override
	protected String version() {
		return version;
	}

	@Override
	protected String author() {
		return author;
	}

	@Override
	protected String description() {
		return description;
	}

	@Override
	protected ModList universalFiles() {
		final String modLoaderName = "Minecraft Forge";
		final String modLoaderVersion = minecraft.forgeVersion();

		if(downloadExtendedFileData) {
			try {
				return new ModList(
						CurseMod.toExtendedMods(files),
						minecraft.version,
						modLoaderName,
						modLoaderVersion
				);
			} catch(CurseException ex) {
				ThrowableHandling.handleWithoutExit(ex);
			}
		}

		return new ModList(
				CurseMod.toExtendedModsWithoutExtendedData(files),
				minecraft.version,
				modLoaderName,
				modLoaderVersion
		);
	}

	@Override
	protected String overrides() {
		return overrides;
	}

	@Override
	protected MCVersion mcVersion() {
		return minecraft.version;
	}

	@Override
	protected String forgeVersion() {
		return minecraft.forgeVersion();
	}

	@Override
	protected int projectID() {
		return projectID;
	}

	@SuppressWarnings("Duplicates")
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
