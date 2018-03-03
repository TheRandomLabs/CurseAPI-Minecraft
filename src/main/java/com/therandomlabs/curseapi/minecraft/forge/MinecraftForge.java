package com.therandomlabs.curseapi.minecraft.forge;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.curseapi.util.URLUtils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.misc.StringUtils;

public final class MinecraftForge {
	public static final String TITLE = "Minecraft Forge";

	public static final String URL = "https://files.minecraftforge.net/";

	public static final String MC_VERSION = "::MC_VERSION::";
	public static final String VERSION_SPECIFIC_URL =
			URL + "maven/net/minecraftforge/forge/index_" + MC_VERSION + ".html";

	public static final String FORGE_VERSION = "::FORGE_VERSION::";
	public static final String INSTALLER_URL = URL + "maven/net/minecraftforge/forge/" +
			FORGE_VERSION + "/forge-" + FORGE_VERSION + "-installer.jar";
	public static final String CHANGELOG_URL = URL + "maven/net/minecraftforge/forge/" +
			FORGE_VERSION + "/forge-" + FORGE_VERSION + "-changelog.txt";

	public static final String LATEST = "latest";
	public static final String RECOMMENDED = "recommended";

	private static final Map<String, String> changelog = new LinkedHashMap<>();
	private static final TRLList<String> versions = new TRLList<>();

	private MinecraftForge() {}

	public static boolean isValidVersion(String version) throws CurseException, IOException {
		if(versions.isEmpty()) {
			getChangelog();
		}

		return versions.contains(version);
	}

	public static String validateVersion(String version) throws CurseException, IOException {
		if(!isValidVersion(version)) {
			invalidVersion(version);
		}
		return version;
	}

	public static URL getPageURLFromMCVersion(String version) throws CurseException {
		return getPageURLFromMCVersion(MinecraftVersion.fromString(version));
	}

	public static URL getPageURLFromMCVersion(MinecraftVersion version) throws CurseException {
		return URLUtils.url(VERSION_SPECIFIC_URL.replaceAll(MC_VERSION, version.toString()));
	}

	public static String getLatestVersion(String mcVersion) throws CurseException {
		return getLatestVersion(MinecraftVersion.fromString(mcVersion));
	}

	public static String getLatestVersion(MinecraftVersion version) throws CurseException {
		return getLatestVersion(getPageURLFromMCVersion(version));
	}

	public static String getLatestVersion(URL url) throws CurseException {
		return DocumentUtils.getValue(url, "class=title;tag=small;text").replaceAll(" - ", "-");
	}

	public static String getLatestVersionWithoutChangelog() throws CurseException {
		return getLatestVersion(URLUtils.url(URL));
	}

	public static String getRecommendedVersion(String mcVersion) throws CurseException {
		return getRecommendedVersion(MinecraftVersion.fromString(mcVersion));
	}

	public static String getRecommendedVersion(MinecraftVersion version) throws CurseException {
		return getRecommendedVersion(getPageURLFromMCVersion(version));
	}

	public static String getRecommendedVersion(URL url) throws CurseException {
		return DocumentUtils.getValue(url, "class=title=1;tag=small;text").replaceAll(" - ", "-");
	}

	public static String getRecommendedVersion() throws CurseException {
		return getRecommendedVersion(URLUtils.url(URL));
	}

	public static URL getInstallerURL(String forgeVersion) throws CurseException, IOException {
		validateVersion(forgeVersion);
		return URLUtils.url(INSTALLER_URL.replaceAll(FORGE_VERSION, forgeVersion));
	}

	public static Path downloadInstaller(String forgeVersion, Path location)
			throws CurseException, IOException {
		return NIOUtils.download(getInstallerURL(forgeVersion), location);
	}

	public static Path downloadInstallerToDirectory(String forgeVersion, Path directory)
			throws CurseException, IOException {
		return NIOUtils.downloadToDirectory(getInstallerURL(forgeVersion), directory);
	}

	public static String getInstalledDirectoryName(String minecraftVersion, String forgeVersion) {
		return minecraftVersion + "-forge" + forgeVersion;
	}

	public static String getInstalledDirectoryName(MinecraftVersion minecraftVersion,
			String forgeVersion) {
		return getInstalledDirectoryName(minecraftVersion.toString(), forgeVersion);
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
		return getChangelogURL(getLatestVersion());
	}

	public static URL getChangelogURL(String version) throws CurseException, IOException {
		if(!changelog.isEmpty()) {
			validateVersion(version);
		}
		return URLUtils.url(CHANGELOG_URL.replaceAll(FORGE_VERSION, version));
	}

	public static Map<String, String> getChangelog() throws CurseException, IOException {
		return getChangelogs(null, null);
	}

	public static Map<String, String> getChangelogs(String oldVersion, String newVersion)
			throws CurseException, IOException {
		if(changelog.isEmpty()) {
			final String[] lines = StringUtils.splitNewline(DocumentUtils.read(getChangelogURL()));
			final StringBuilder entry = new StringBuilder();
			String version = null;

			for(int i = 1; i < lines.length; i++) {
				final String line = lines[i];
				if(line.startsWith("Build ")) {
					if(i == 2) {
						version = getLatestVersion();
					} else {
						version = StringUtils.removeLastChar(line.split(" ")[1]);
					}
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
		if(versions.isEmpty()) {
			getChangelog();
		}

		final int index1 = versions.indexOf(version1);
		final int index2 = versions.indexOf(version2);

		if(index1 == -1) {
			invalidVersion(version1);
		}

		if(index2 == -1) {
			invalidVersion(version2);
		}

		return Integer.compare(index1, index2);
	}

	public static void clearChangelogCache() {
		changelog.clear();
		versions.clear();
	}

	private static void invalidVersion(String version) throws CurseException {
		throw new CurseException("Invalid Forge version: " + version);
	}
}
