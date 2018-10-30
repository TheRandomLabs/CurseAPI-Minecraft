package com.therandomlabs.curseapi.minecraft.modpack;

import java.net.URL;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.TRLList;

public final class ExMPManifest extends MPManifest {
	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;

	public String id;
	public String name;

	public String version;
	public String author;
	public String description;

	public Mod[] files;
	public Mod[] serverOnlyFiles;
	public Mod[] disabledByDefaultFiles;
	public Mod[] optifineIncompatibleFiles;

	public FileInfo[] additionalFilesOnDisk;

	public String overrides = "overrides";
	public MinecraftInfo minecraft;
	public int projectID;

	public URL projectURL;
	public String optifineVersion = RECOMMENDED_OPTIFINE_VERSION;

	public String clientJVMArguments = "";

	public int minimumClientRAM = 4096;
	public int recommendedClientRAM = 6144;

	@Override
	public ExMPManifest clone() {
		final ExMPManifest manifest = (ExMPManifest) super.clone();

		manifest.files = Utils.tryClone(files);

		if(serverOnlyFiles != null) {
			manifest.serverOnlyFiles = Utils.tryClone(serverOnlyFiles);
		}

		if(disabledByDefaultFiles != null) {
			manifest.disabledByDefaultFiles = Utils.tryClone(disabledByDefaultFiles);
		}

		if(optifineIncompatibleFiles != null) {
			manifest.optifineIncompatibleFiles = Utils.tryClone(optifineIncompatibleFiles);
		}

		if(additionalFilesOnDisk != null) {
			manifest.additionalFilesOnDisk = Utils.tryClone(additionalFilesOnDisk);
		}

		manifest.minecraft = minecraft.clone();

		return manifest;
	}

	@Override
	protected String id() {
		return id;
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
		return modList(files);
	}

	@Override
	protected ModList serverOnlyFiles() {
		return modList(serverOnlyFiles);
	}

	@Override
	protected ModList disabledByDefaultFiles() {
		return modList(disabledByDefaultFiles);
	}

	@Override
	protected ModList optifineIncompatibleFiles() {
		return modList(optifineIncompatibleFiles);
	}

	@Override
	protected TRLList<FileInfo> additionalFilesOnDisk() {
		return new TRLList<>(additionalFilesOnDisk);
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
		return minecraft.getForgeVersion();
	}

	@Override
	protected int projectID() {
		return projectID;
	}

	@Override
	protected URL projectURL() {
		return projectURL == null ? super.projectURL() : projectURL;
	}

	@Override
	protected String optifineVersion() {
		return optifineVersion;
	}

	@Override
	protected String clientJVMArguments() {
		return clientJVMArguments;
	}

	@Override
	protected int minimumClientRAM() {
		return minimumClientRAM;
	}

	@Override
	protected int recommendedClientRAM() {
		return recommendedClientRAM;
	}

	private ModList modList(Mod[] mods) {
		if(mods == null || mods.length == 0) {
			return ModList.EMPTY;
		}

		return new ModList(mods, minecraft.version, "MinecraftForge", minecraft.getForgeVersion());
	}
}
