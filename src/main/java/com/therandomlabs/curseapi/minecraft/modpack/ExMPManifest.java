package com.therandomlabs.curseapi.minecraft.modpack;

import java.net.URL;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.Assertions;

public final class ExMPManifest extends MPManifest {
	public String manifestType;
	public int manifestVersion;

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
	public String[] persistentConfigs;

	public String overrides;
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

		if(persistentConfigs != null) {
			manifest.persistentConfigs = persistentConfigs.clone();
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
	protected TRLList<String> persistentConfigs() {
		return new TRLList<>(persistentConfigs);
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

	@SuppressWarnings("Duplicates")
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
		}

		if(serverOnlyFiles != null) {
			for(Mod mod : serverOnlyFiles) {
				mod.validate();
			}
		}

		if(disabledByDefaultFiles != null) {
			for(Mod mod : disabledByDefaultFiles) {
				mod.validate();
			}
		}

		if(optifineIncompatibleFiles != null) {
			for(Mod mod : optifineIncompatibleFiles) {
				mod.validate();
			}
		}

		if(additionalFilesOnDisk != null) {
			for(FileInfo file : additionalFilesOnDisk) {
				file.validate();
			}
		}

		if(persistentConfigs != null) {
			for(String config : persistentConfigs) {
				Assertions.validPath(config);
			}
		}

		Assertions.validPath(overrides);

		minecraft.validate();

		if(projectID != 0) {
			CurseAPI.validateProjectID(projectID);
		}

		Assertions.nonNull(projectURL, "projectURL");
		Assertions.nonEmpty(optifineVersion, "optifineVersion");

		Assertions.positive(minimumClientRAM, "minimumClientRAM", false);
		Assertions.positive(recommendedClientRAM, "recommendedClientRAM", false);

		Assertions.larger(
				recommendedClientRAM, "recommendedClientRAM",
				minimumClientRAM, "minimumClientRAM"
		);
	}

	private ModList modList(Mod[] mods) {
		if(mods == null || mods.length == 0) {
			return ModList.empty();
		}

		return new ModList(mods, minecraft.version, "MinecraftForge", minecraft.forgeVersion());
	}
}
