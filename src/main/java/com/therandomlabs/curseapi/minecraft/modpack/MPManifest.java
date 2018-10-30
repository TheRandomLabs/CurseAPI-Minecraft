package com.therandomlabs.curseapi.minecraft.modpack;

import java.net.URL;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public abstract class MPManifest implements Cloneable {
	public static final String RECOMMENDED_OPTIFINE_VERSION = "recommended";
	public static final String LATEST_OPTIFINE_VERSION = "latest";

	@Override
	public MPManifest clone() {
		try {
			return (MPManifest) super.clone();
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	protected String id() {
		return StringUtils.replaceWhitespace(name().toLowerCase(Locale.ENGLISH), "_");
	}

	protected abstract String name();

	protected abstract String version();

	protected abstract String author();

	protected abstract String description();

	protected abstract ModList universalFiles();

	protected ModList serverOnlyFiles() {
		return ModList.empty();
	}

	protected ModList disabledByDefaultFiles() {
		return ModList.empty();
	}

	protected ModList optifineIncompatibleFiles() {
		return ModList.empty();
	}

	protected TRLList<FileInfo> additionalFilesOnDisk() {
		return new TRLList<>();
	}

	protected abstract String overrides();

	protected abstract MCVersion mcVersion();

	protected abstract String forgeVersion();

	protected abstract int projectID();

	protected URL projectURL() {
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

	protected String optifineVersion() {
		return RECOMMENDED_OPTIFINE_VERSION;
	}

	protected String clientJVMArguments() {
		return "";
	}

	protected int minimumClientRAM() {
		return 4096;
	}

	protected int recommendedClientRAM() {
		return 6144;
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toPrettyJsonWithTabs() {
		return StringUtils.replaceSpacesWithTabs(toPrettyJson(), 2);
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
}
