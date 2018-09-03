package com.therandomlabs.curseapi.minecraft.forge;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.util.Documents;
import com.therandomlabs.curseapi.util.URLs;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class MinecraftForge {
	public static final String TITLE = "Minecraft Forge";

	public static final String FORGE_URL_STRING = "https://files.minecraftforge.net/";
	public static final URL FORGE_URL;

	public static final String MC_VERSION = "::MC_VERSION::";
	public static final String VERSION_SPECIFIC_URL =
			FORGE_URL_STRING + "maven/net/minecraftforge/forge/index_" + MC_VERSION + ".html";

	public static final String FORGE_VERSION = "::FORGE_VERSION::";
	public static final String INSTALLER_URL = FORGE_URL_STRING + "maven/net/minecraftforge/" +
			"forge/" + FORGE_VERSION + "/forge-" + FORGE_VERSION + "-installer.jar";
	public static final String CHANGELOG_URL = FORGE_URL_STRING + "maven/net/minecraftforge/" +
			"forge/" + FORGE_VERSION + "/forge-" + FORGE_VERSION + "-changelog.txt";

	public static final String LATEST = "latest";
	public static final String RECOMMENDED = "recommended";

	private static final Map<String, String> changelog = new LinkedHashMap<>();
	private static final TRLList<String> versions = new TRLList<>();

	static {
		URL url = null;

		try {
			url = new URL(FORGE_URL_STRING);
		} catch(MalformedURLException ex) {
			ThrowableHandling.handle(ex);
		}

		FORGE_URL = url;
	}

	private MinecraftForge() {}

	public static boolean isValidVersion(String version) throws CurseException, IOException {
		//Temporary fix
		if(version.equals("1.12.2-14.23.4.2759")) {
			return true;
		}

		if(versions.isEmpty()) {
			getChangelog();
		}

		return versions.contains(version);
	}

	public static String validateVersion(String version) throws CurseException, IOException {
		if(!isValidVersion(version)) {
			throw new InvalidForgeVersionException(version);
		}

		return version;
	}

	public static URL getPageURLFromMCVersion(String version) {
		return getPageURLFromMCVersion(MinecraftVersion.fromString(version));
	}

	public static URL getPageURLFromMCVersion(MinecraftVersion version) {
		try {
			return new URL(VERSION_SPECIFIC_URL.replaceAll(MC_VERSION, version.toString()));
		} catch(MalformedURLException ignored) {}

		return null;
	}

	public static String getLatestVersion(String mcVersion) throws CurseException {
		return getLatestVersion(MinecraftVersion.fromString(mcVersion));
	}

	public static String getLatestVersion(MinecraftVersion version) throws CurseException {
		return getLatestVersion(getPageURLFromMCVersion(version));
	}

	public static String getLatestVersion(URL url) throws CurseException {
		return Documents.getValue(url, "class=title;tag=small;text").replaceAll(" - ", "-");
	}

	public static String getLatestVersionWithoutChangelog() throws CurseException, IOException{
		return getLatestVersion(FORGE_URL);
	}

	public static String getRecommendedVersion(String mcVersion)
			throws CurseException, IOException {
		return getRecommendedVersion(MinecraftVersion.fromString(mcVersion));
	}

	public static String getRecommendedVersion(MinecraftVersion version)
			throws CurseException, IOException {
		return getRecommendedVersion(getPageURLFromMCVersion(version));
	}

	public static String getRecommendedVersion(URL url) throws CurseException, IOException {
		return Documents.getValue(url, "class=title=1;tag=small;text").replaceAll(" - ", "-");
	}

	public static String getRecommendedVersion() throws CurseException, IOException {
		return getRecommendedVersion(FORGE_URL);
	}

	public static URL getInstallerURL(String forgeVersion) throws CurseException, IOException {
		validateVersion(forgeVersion);
		return URLs.of(INSTALLER_URL.replaceAll(FORGE_VERSION, forgeVersion));
	}

	public static Path downloadInstaller(String forgeVersion, Path location)
			throws CurseException, IOException {
		return NIOUtils.download(getInstallerURL(forgeVersion), location);
	}

	public static Path downloadInstallerToDirectory(String forgeVersion, Path directory)
			throws CurseException, IOException {
		return NIOUtils.downloadToDirectory(getInstallerURL(forgeVersion), directory);
	}

	public static String getInstalledDirectoryName(MinecraftVersion minecraftVersion,
			String forgeVersion) {
		return getInstalledDirectoryName(minecraftVersion.toString(), forgeVersion);
	}

	public static String getInstalledDirectoryName(String minecraftVersion, String forgeVersion) {
		return minecraftVersion + "-forge" + forgeVersion;
	}

	public static String get(String minecraftVersion, String forgeVersion)
			throws CurseException, IOException {
		return get(MinecraftVersion.fromString(minecraftVersion), forgeVersion);
	}

	public static String get(MinecraftVersion minecraftVersion, String forgeVersion)
			throws CurseException, IOException {
		forgeVersion = forgeVersion.toLowerCase(Locale.ENGLISH);

		if(forgeVersion.equals(LATEST)) {
			return getLatestVersion(minecraftVersion);
		}

		if(forgeVersion.equals(RECOMMENDED)) {
			return getRecommendedVersion(minecraftVersion);
		}

		return validateVersion(forgeVersion);
	}

	public static URL getChangelogURL() throws CurseException, IOException {
		String latestVersion = versions.isEmpty() ?
				getLatestVersionWithoutChangelog() : versions.get(0);
		return getChangelogURL(latestVersion);
	}

	public static URL getChangelogURL(String version) throws CurseException, IOException {
		//Temporary fix
		if(version.equals("1.12.2-14.23.4.2759")) {
			version = "1.12.2-14.23.4.2758";
		}

		if(!changelog.isEmpty()) {
			validateVersion(version);
		}

		return new URL(CHANGELOG_URL.replaceAll(FORGE_VERSION, version));
	}

	public static Map<String, String> getChangelog() throws CurseException, IOException {
		return getChangelog(null, null);
	}

	public static Map<String, String> getChangelog(String oldVersion, String newVersion)
			throws CurseException, IOException {
		if(changelog.isEmpty()) {
			final String[] lines = StringUtils.splitNewline(Documents.read(getChangelogURL()));
			final StringBuilder entry = new StringBuilder();
			String version = getLatestVersionWithoutChangelog();

			//Temporary fix
			if(version.equals("1.12.2-14.23.4.2759")) {
				versions.add("1.12.2-14.23.4.2759");
				changelog.put("1.12.2-14.23.4.2759", "LexManos: Remove BlamingTransformer (#5115)");
			}

			for(int i = 2; i < lines.length; i++) {
				final String line = lines[i];
				if(line.startsWith("Build ")) {
					version = StringUtils.removeLastChar(line.split(" ")[1]);
					continue;
				}

				if(line.isEmpty()) {
					versions.add(version);
					changelog.put(version, entry.toString());
					entry.setLength(0);
					version = null;
				}

				if(version != null) {
					entry.append(line.substring(1)).append(System.lineSeparator());
				}
			}
		}

		if((oldVersion != null && newVersion != null) && compare(oldVersion, newVersion) >= 0) {
			throw new IllegalArgumentException("oldVersion must be older than newVersion");
		}

		final Map<String, String> subchangelog = new LinkedHashMap<>();
		boolean newVersionFound = false;

		for(Map.Entry<String, String> entry : changelog.entrySet()) {
			if(entry.getKey().equals(newVersion)) {
				newVersionFound = true;
			}

			if(newVersionFound) {
				if(entry.getKey().equals(oldVersion)) {
					break;
				}

				subchangelog.put(entry.getKey(), entry.getValue());
			}
		}

		return subchangelog;
	}

	public static TRLList<String> getVersions() throws CurseException, IOException {
		if(versions.isEmpty()) {
			getChangelog();
		}

		return versions.toImmutableList();
	}

	public static String getLatestVersion() throws CurseException, IOException {
		if(versions.isEmpty()) {
			getChangelog();
		}

		return versions.get(0);
	}

	public static int compare(String version1, String version2) throws CurseException, IOException {
		if(version1.equals(version2)) {
			return 0;
		}

		if(versions.isEmpty()) {
			getChangelog();
		}

		final int index1 = versions.indexOf(version1);
		final int index2 = versions.indexOf(version2);

		if(index1 == -1) {
			throw new InvalidForgeVersionException(version1);
		}

		if(index2 == -1) {
			throw new InvalidForgeVersionException(version2);
		}

		//If version2 is older than version1, this will return a positive value,
		//so if version1 is newer than version2, then a positive value will be returned
		return Integer.compare(index2, index1);
	}

	public static MinecraftVersion getMCVersion(String version) {
		final String[] split = version.split("-");

		if(split.length != 2) {
			throw new InvalidForgeVersionException(version);
		}

		final MinecraftVersion mcVersion = MinecraftVersion.fromString(split[0]);

		if(mcVersion == null) {
			throw new InvalidForgeVersionException(version);
		}

		return mcVersion;
	}

	public static void clearChangelogCache() {
		changelog.clear();
		versions.clear();
	}
}
