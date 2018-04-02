package com.therandomlabs.curseapi.minecraft;

import java.net.MalformedURLException;
import java.net.URL;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;

public final class CurseAPIMinecraft {
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

	public static final String DARKCHOCOLATE_MANIFEST_TXT = "https://raw.githubusercontent.com/" +
			"TheRandomLabs/LightChocolate/master/darkchocolate.txt";
	public static final URL DARKCHOCOLATE_MANIFEST_TXT_URL = url(DARKCHOCOLATE_MANIFEST_TXT);

	private CurseAPIMinecraft() {}

	public static void clearAllCache() {
		MinecraftForge.clearChangelogCache();
	}

	private static URL url(String url) {
		try {
			return new URL(url);
		} catch(MalformedURLException ignored) {
			//This will never happen unless I screw up the URLs somehow
		}
		return null;
	}
}
