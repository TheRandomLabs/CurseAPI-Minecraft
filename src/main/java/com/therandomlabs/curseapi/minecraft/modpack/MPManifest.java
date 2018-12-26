package com.therandomlabs.curseapi.minecraft.modpack;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.CurseAPIMinecraft;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;
import static com.therandomlabs.utils.logging.Logging.getLogger;

public abstract class MPManifest implements Cloneable {
	public static final String RECOMMENDED_OPTIFINE_VERSION = "recommended";
	public static final String LATEST_OPTIFINE_VERSION = "latest";

	@Override
	public abstract MPManifest clone();

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toPrettyJsonWithTabs() {
		return StringUtils.replaceSpacesWithTabs(toPrettyJson(), 2);
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public String id() {
		return StringUtils.replaceWhitespace(sanitizedName().toLowerCase(Locale.ENGLISH), "");
	}

	public String name() {
		final int projectID = projectID();

		if(!CurseAPI.isValidProjectID(projectID)) {
			return "Unknown Modpack";
		}

		try {
			return CurseProject.fromID(projectID).title();
		} catch(CurseException ex) {
			getLogger().error(
					"Error retrieving name for project with ID: " + projectID
			);
			getLogger().printStackTrace(ex);
		}

		return "Unknown Modpack";
	}

	public String sanitizedName() {
		return name().replaceAll("[\\\\/:*?\"<>|]", "");
	}

	public abstract String version();

	public String nameWithVersion() {
		return name() + " " + version();
	}

	public abstract String author();

	public String description() {
		final int projectID = projectID();

		if(!CurseAPI.isValidProjectID(projectID)) {
			return "No description provided.";
		}

		try {
			return CurseProject.fromID(projectID).shortDescription();
		} catch(CurseException ex) {
			getLogger().error(
					"Error retrieving description for project with ID: " + projectID
			);
			getLogger().printStackTrace(ex);
		}

		return "No description provided.";
	}

	public abstract ModList universalFiles();

	public ModList serverOnlyFiles() {
		return new ModList();
	}

	public ModList disabledByDefaultFiles() {
		return new ModList();
	}

	public ModList optifineIncompatibleFiles() {
		return new ModList();
	}

	public ModList getAllFiles() {
		final ModList universalFiles = universalFiles();
		final ModList serverOnlyFiles = serverOnlyFiles();
		final ModList disabledByDefaultFiles = disabledByDefaultFiles();
		final ModList optifineIncompatibleFiles = optifineIncompatibleFiles();

		final ModList allFiles = new ModList(
				universalFiles.size() + serverOnlyFiles.size() + disabledByDefaultFiles.size() +
						optifineIncompatibleFiles.size(),
				mcVersion(),
				CurseAPIMinecraft.MINECRAFT_FORGE,
				forgeVersion()
		);

		allFiles.addAll(universalFiles);
		allFiles.addAll(serverOnlyFiles);
		allFiles.addAll(disabledByDefaultFiles);
		allFiles.addAll(optifineIncompatibleFiles);

		return allFiles;
	}

	public TRLList<FileInfo> additionalFilesOnDisk() {
		return new TRLList<>();
	}

	public TRLList<String> persistentConfigs() {
		return new TRLList<>();
	}

	public String iconPath() {
		return null;
	}

	public abstract String overrides();

	public String serverOnlyOverrides() {
		return "";
	}

	public abstract MCVersion mcVersion();

	public abstract String forgeVersion();

	public abstract int projectID();

	public URL projectURL() {
		final int id = projectID();

		if(CurseAPI.isValidProjectID(id)) {
			try {
				return CurseProject.fromID(id).url();
			} catch(CurseException ex) {
				ThrowableHandling.handleWithoutExit(ex);
			}
		}

		return null;
	}

	public String optifineVersion() {
		return RECOMMENDED_OPTIFINE_VERSION;
	}

	public String clientJVMArguments() {
		//https://www.reddit.com/r/feedthebeast/comments/5jhuk9/
		//modded_mc_and_memory_usage_a_history_with_a/
		//and
		//https://www.reddit.com/r/feedthebeast/comments/5jhuk9/
		//modded_mc_and_memory_usage_a_history_with_a/dbgfm2s/
		return minimumClientRAM() >= 3072 ?
				"-XX:+UseG1GC -Dsun.rmi.dgc.server.gcInterval=2147483646 " +
						"-XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 " +
						"-XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 " +
						"-XX:G1HeapRegionSize=32M" : "";
	}

	public int minimumClientRAM() {
		return 4096;
	}

	public int recommendedClientRAM() {
		return 6144;
	}

	public abstract void validate();

	public abstract void sort();

	protected final MPManifest getCloned() {
		try {
			return (MPManifest) super.clone();
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	protected final ModList modList(Mod[] mods) {
		if(mods == null || mods.length == 0) {
			return new ModList();
		}

		return new ModList(mods, mcVersion(), CurseAPIMinecraft.MINECRAFT_FORGE, forgeVersion());
	}

	public static MPManifest from(Path path) throws IOException {
		final ExMPManifest manifest = ExMPManifest.from(path);
		return manifest.isActuallyExtended() ? manifest : CurseMPManifest.from(path);
	}
}
