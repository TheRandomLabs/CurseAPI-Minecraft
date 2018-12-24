package com.therandomlabs.curseapi.minecraft.modpack;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.misc.StringUtils;

public final class ExMPManifest extends MPManifest {
	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;

	public String id;
	public String name = "Unknown Modpack";

	public String version;
	public String author;
	public String description = "No description provided.";

	public Mod[] files;
	public Mod[] serverOnlyFiles;
	public Mod[] disabledByDefaultFiles;
	public Mod[] optifineIncompatibleFiles;

	public FileInfo[] additionalFilesOnDisk = new FileInfo[0];
	public String[] persistentConfigs = new String[0];
	public String iconPath;

	public String overrides = "overrides";
	public String serverOnlyOverrides = "server_only_overrides";
	public MinecraftInfo minecraft;
	public int projectID;

	public URL projectURL;
	public String optifineVersion = RECOMMENDED_OPTIFINE_VERSION;

	public String clientJVMArguments = "";
	public int minimumClientRAM = 4096;
	public int recommendedClientRAM = 6144;

	@Override
	public ExMPManifest clone() {
		final ExMPManifest manifest = (ExMPManifest) getCloned();

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
	public String id() {
		return id == null ? super.id() : id;
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
		return modList(files);
	}

	@Override
	public ModList serverOnlyFiles() {
		return modList(serverOnlyFiles);
	}

	@Override
	public ModList disabledByDefaultFiles() {
		return modList(disabledByDefaultFiles);
	}

	@Override
	public ModList optifineIncompatibleFiles() {
		return modList(optifineIncompatibleFiles);
	}

	@Override
	public TRLList<FileInfo> additionalFilesOnDisk() {
		return new TRLList<>(additionalFilesOnDisk);
	}

	@Override
	public TRLList<String> persistentConfigs() {
		return new TRLList<>(persistentConfigs);
	}

	@Override
	public String iconPath() {
		return iconPath;
	}

	@Override
	public String overrides() {
		return overrides;
	}

	@Override
	public String serverOnlyOverrides() {
		return serverOnlyOverrides;
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

	@Override
	public URL projectURL() {
		return projectURL == null ? super.projectURL() : projectURL;
	}

	@Override
	public String optifineVersion() {
		return optifineVersion;
	}

	@Override
	public String clientJVMArguments() {
		return clientJVMArguments;
	}

	@Override
	public int minimumClientRAM() {
		return minimumClientRAM;
	}

	@Override
	public int recommendedClientRAM() {
		return recommendedClientRAM;
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void validate() {
		Assertions.equals(manifestType, "manifestType", "minecraftModpack");
		Assertions.equals(manifestVersion, "manifestVersion", 1);
		Assertions.nonEmpty(name, "name");

		if(id != null) {
			Assertions.nonEmpty(id, "id");

			if(!StringUtils.isSanitized(id)) {
				throw new IllegalArgumentException("Unsanitized modpack ID: " + id);
			}

			if(StringUtils.containsWhitespace(id)) {
				throw new IllegalArgumentException("Modpack ID contains whitespace: " + id);
			}
		}

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
				Assertions.equals(mod.side, "optifineIncompatibleFiles.side", Side.CLIENT);
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

		if(iconPath != null && !iconPath.isEmpty()) {
			Assertions.validPath(iconPath);
		}

		Assertions.validPath(overrides);

		if(serverOnlyOverrides != null && !serverOnlyOverrides.isEmpty()) {
			Assertions.validPath(serverOnlyOverrides);
		}

		minecraft.validate();

		if(projectID != 0) {
			CurseAPI.validateProjectID(projectID);
		}

		Assertions.nonEmpty(optifineVersion, "optifineVersion");

		Assertions.positive(minimumClientRAM, "minimumClientRAM", false);
		Assertions.positive(recommendedClientRAM, "recommendedClientRAM", false);

		Assertions.larger(
				recommendedClientRAM, "recommendedClientRAM",
				minimumClientRAM, "minimumClientRAM"
		);
	}

	@Override
	public void sort() {
		sortMods(files);
		sortMods(serverOnlyFiles);
		sortMods(disabledByDefaultFiles);
		sortMods(optifineIncompatibleFiles);
		sort(additionalFilesOnDisk);
		sort(persistentConfigs);
	}

	public boolean isActuallyExtended() {
		return id != null;
	}

	private void sortMods(Mod[] mods) {
		if(mods == null) {
			return;
		}

		Arrays.sort(mods);

		for(Mod mod : mods) {
			mod.sort();
		}
	}

	private void sort(Object[] objects) {
		if(objects != null) {
			Arrays.sort(objects);
		}
	}

	public static ExMPManifest from(Path path) throws IOException {
		return Utils.fromJson(path, ExMPManifest.class);
	}
}
