package com.therandomlabs.curseapi.minecraft;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.platform.Platform;
import com.therandomlabs.utils.systemproperty.SystemProperties;

public final class CurseAPIMinecraft {
	public static final String MINECRAFT_FORGE = "Minecraft Forge";

	public static final int LIGHTCHOCOLATE_PROJECT_ID = 257165;
	public static final String LIGHTCHOCOLATE_ID = "lightchocolate";

	public static final String LIGHTCHOCOLATE_REPO =
			"https://github.com/TheRandomLabs/LightChocolate";
	public static final URL LIGHTCHOCOLATE_REPO_URL = url(LIGHTCHOCOLATE_REPO);
	public static final String LIGHTCHOCOLATE_DOWNLOAD =
			LIGHTCHOCOLATE_REPO + "/archive/master.zip";
	public static final URL LIGHTCHOCOLATE_DOWNLOAD_URL = url(LIGHTCHOCOLATE_DOWNLOAD);
	public static final String LIGHTCHOCOLATE_MANIFEST_TXT =
			"https://raw.githubusercontent.com/TheRandomLabs/LightChocolate/master/manifest.txt";
	public static final URL LIGHTCHOCOLATE_MANIFEST_TXT_URL = url(LIGHTCHOCOLATE_MANIFEST_TXT);
	public static final String LIGHTCHOCOLATE_MANIFEST_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/LightChocolate/master/manifest.json";
	public static final URL LIGHTCHOCOLATE_MANIFEST_JSON_URL = url(LIGHTCHOCOLATE_MANIFEST_JSON);

	public static final String DARKCHOCOLATE_ID = "darkchocolate";

	public static final String DARKCHOCOLATE_REPO =
			"https://github.com/TheRandomLabs/DarkChocolate";
	public static final URL DARKCHOCOLATE_REPO_URL = url(DARKCHOCOLATE_REPO);
	public static final String DARKCHOCOLATE_DOWNLOAD = DARKCHOCOLATE_REPO + "/archive/master.zip";
	public static final URL DARKCHOCOLATE_DOWNLOAD_URL = url(DARKCHOCOLATE_DOWNLOAD);
	public static final String DARKCHOCOLATE_MANIFEST_TXT =
			"https://raw.githubusercontent.com/TheRandomLabs/DarkChocolate/master/manifest.txt";
	public static final URL DARKCHOCOLATE_MANIFEST_TXT_URL = url(LIGHTCHOCOLATE_MANIFEST_TXT);
	public static final String DARKCHOCOLATE_MANIFEST_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/DarkChocolate/master/manifest.json";
	public static final URL DARKCHOCOLATE_MANIFEST_JSON_URL = url(DARKCHOCOLATE_MANIFEST_JSON);

	public static final TRLList<String> CLIENT_ONLY_FILES = new ImmutableList<>(
			"resourcepacks",
			"server-resource-packs",
			"shaderpacks",
			"options.txt",
			"optionsof.txt",
			"realms_persistence.json",
			"servers.dat"
	);

	static {
		//Initialize MCVersion
		MCVersion.UNKNOWN.toString();
	}

	private CurseAPIMinecraft() {}

	public static Path getDefaultMCDirectory() {
		switch(Platform.OS) {
		case WINDOWS:
			return Paths.get(System.getenv("APPDATA"), ".minecraft");
		case MAC_OS_X:
			return Paths.get(
					SystemProperties.USER_HOME_DIRECTORY.get(),
					"Library",
					"Application Support",
					"minecraft"
			);
		default:
			return Paths.get(SystemProperties.USER_HOME_DIRECTORY.get(), ".minecraft");
		}
	}

	public static CurseProject downloadProjectData(int projectID) throws CurseException {
		if(CurseProject.isCached(projectID)) {
			return CurseProject.fromID(projectID);
		}

		MCEventHandling.forEach(handler -> handler.downloadingProjectData(projectID));

		final CurseProject project = CurseProject.fromID(projectID, true);

		if(project == CurseProject.NULL_PROJECT) {
			MCEventHandling.forEach(handler -> handler.projectNotFound(projectID));
		} else {
			MCEventHandling.forEach(handler -> handler.downloadedProjectData(project));
		}

		return project;
	}

	private static URL url(String url) {
		try {
			return new URL(url);
		} catch(MalformedURLException ignored) {}

		return null;
	}
}
