package com.therandomlabs.curseapi.minecraft.modpack;

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
		return StringUtils.replaceWhitespace(name().toLowerCase(Locale.ENGLISH), "_");
	}

	public abstract String name();

	public abstract String version();

	public abstract String author();

	public abstract String description();

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

	public TRLList<FileInfo> additionalFilesOnDisk() {
		return new TRLList<>();
	}

	public TRLList<String> persistentConfigs() {
		return new TRLList<>();
	}

	public Path iconPath() {
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
		return "";
	}

	public int minimumClientRAM() {
		return 4096;
	}

	public int recommendedClientRAM() {
		return 6144;
	}

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
}
