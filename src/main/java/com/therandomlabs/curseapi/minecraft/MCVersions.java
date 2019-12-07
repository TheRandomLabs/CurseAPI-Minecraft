package com.therandomlabs.curseapi.minecraft;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Contains {@link MCVersion} constants that represent all versions of Minecraft supported
 * by CurseForge.
 */
public final class MCVersions {
	/**
	 * Minecraft 1.0.
	 */
	public static final MCVersion V1_0 = get("1.0");

	/**
	 * Minecraft 1.1.
	 */
	public static final MCVersion V1_1 = get("1.1");

	/**
	 * Minecraft 1.2.1.
	 */
	public static final MCVersion V1_2_1 = get("1.2.1");

	/**
	 * Minecraft 1.2.2.
	 */
	public static final MCVersion V1_2_2 = get("1.2.2");

	/**
	 * Minecraft 1.2.3.
	 */
	public static final MCVersion V1_2_3 = get("1.2.3");

	/**
	 * Minecraft 1.2.4.
	 */
	public static final MCVersion V1_2_4 = get("1.2.4");

	/**
	 * Minecraft 1.2.5.
	 */
	public static final MCVersion V1_2_5 = get("1.2.5");

	/**
	 * Minecraft 1.3.1.
	 */
	public static final MCVersion V1_3_1 = get("1.3.1");

	/**
	 * Minecraft 1.3.2.
	 */
	public static final MCVersion V1_3_2 = get("1.3.2");

	/**
	 * Minecraft 1.4.2.
	 */
	public static final MCVersion V1_4_2 = get("1.4.2");

	/**
	 * Minecraft 1.4.4.
	 */
	public static final MCVersion V1_4_4 = get("1.4.4");

	/**
	 * Minecraft 1.4.5.
	 */
	public static final MCVersion V1_4_5 = get("1.4.5");

	/**
	 * Minecraft 1.4.6.
	 */
	public static final MCVersion V1_4_6 = get("1.4.6");

	/**
	 * Minecraft 1.4.7.
	 */
	public static final MCVersion V1_4_7 = get("1.4.7");

	/**
	 * Minecraft 1.5.1.
	 */
	public static final MCVersion V1_5_1 = get("1.5.1");

	/**
	 * Minecraft 1.5.2.
	 */
	public static final MCVersion V1_5_2 = get("1.5.2");

	/**
	 * Minecraft 1.6.1.
	 */
	public static final MCVersion V1_6_1 = get("1.6.1");

	/**
	 * Minecraft 1.6.2.
	 */
	public static final MCVersion V1_6_2 = get("1.6.2");

	/**
	 * Minecraft 1.6.4.
	 */
	public static final MCVersion V1_6_4 = get("1.6.4");

	/**
	 * Minecraft 1.7.2.
	 */
	public static final MCVersion V1_7_2 = get("1.7.2");

	/**
	 * Minecraft 1.7.3.
	 */
	public static final MCVersion V1_7_3 = get("1.7.3");

	/**
	 * Minecraft 1.7.4.
	 */
	public static final MCVersion V1_7_4 = get("1.7.4");

	/**
	 * Minecraft 1.7.5.
	 */
	public static final MCVersion V1_7_5 = get("1.7.5");

	/**
	 * Minecraft 1.7.6.
	 */
	public static final MCVersion V1_7_6 = get("1.7.6");

	/**
	 * Minecraft 1.7.7.
	 */
	public static final MCVersion V1_7_7 = get("1.7.7");

	/**
	 * Minecraft 1.7.8.
	 */
	public static final MCVersion V1_7_8 = get("1.7.8");

	/**
	 * Minecraft 1.7.9.
	 */
	public static final MCVersion V1_7_9 = get("1.7.9");

	/**
	 * Minecraft 1.7.10.
	 */
	public static final MCVersion V1_7_10 = get("1.7.10");

	/**
	 * Minecraft 1.8.
	 */
	public static final MCVersion V1_8 = get("1.8");

	/**
	 * Minecraft 1.8.1.
	 */
	public static final MCVersion V1_8_1 = get("1.8.1");

	/**
	 * Minecraft 1.8.2.
	 */
	public static final MCVersion V1_8_2 = get("1.8.2");

	/**
	 * Minecraft 1.8.3.
	 */
	public static final MCVersion V1_8_3 = get("1.8.3");

	/**
	 * Minecraft 1.8.4.
	 */
	public static final MCVersion V1_8_4 = get("1.8.4");

	/**
	 * Minecraft 1.8.5.
	 */
	public static final MCVersion V1_8_5 = get("1.8.5");

	/**
	 * Minecraft 1.8.6.
	 */
	public static final MCVersion V1_8_6 = get("1.8.6");

	/**
	 * Minecraft 1.8.7.
	 */
	public static final MCVersion V1_8_7 = get("1.8.7");

	/**
	 * Minecraft 1.8.8.
	 */
	public static final MCVersion V1_8_8 = get("1.8.8");

	/**
	 * Minecraft 1.8.9.
	 */
	public static final MCVersion V1_8_9 = get("1.8.9");

	/**
	 * Minecraft 1.9.
	 */
	public static final MCVersion V1_9 = get("1.9");

	/**
	 * Minecraft 1.9.1.
	 */
	public static final MCVersion V1_9_1 = get("1.9.1");

	/**
	 * Minecraft 1.9.2.
	 */
	public static final MCVersion V1_9_2 = get("1.9.2");

	/**
	 * Minecraft 1.9.3.
	 */
	public static final MCVersion V1_9_3 = get("1.9.3");

	/**
	 * Minecraft 1.9.4.
	 */
	public static final MCVersion V1_9_4 = get("1.9.4");

	/**
	 * Minecraft 1.10.
	 */
	public static final MCVersion V1_10 = get("1.10");

	/**
	 * Minecraft 1.10.1.
	 */
	public static final MCVersion V1_10_1 = get("1.10.1");

	/**
	 * Minecraft 1.10.2.
	 */
	public static final MCVersion V1_10_2 = get("1.10.2");

	/**
	 * Minecraft 1.11.
	 */
	public static final MCVersion V1_11 = get("1.11");

	/**
	 * Minecraft 1.11.1.
	 */
	public static final MCVersion V1_11_1 = get("1.11.1");

	/**
	 * Minecraft 1.11.2.
	 */
	public static final MCVersion V1_11_2 = get("1.11.2");

	/**
	 * Minecraft 1.12.
	 */
	public static final MCVersion V1_12 = get("1.12");

	/**
	 * Minecraft 1.12.1.
	 */
	public static final MCVersion V1_12_1 = get("1.12.1");

	/**
	 * Minecraft 1.12.2.
	 */
	public static final MCVersion V1_12_2 = get("1.12.2");

	/**
	 * Minecraft 1.13.
	 */
	public static final MCVersion V1_13 = get("1.13");

	/**
	 * Minecraft 1.13.1.
	 */
	public static final MCVersion V1_13_1 = get("1.13.1");

	/**
	 * Minecraft 1.13.2.
	 */
	public static final MCVersion V1_13_2 = get("1.13.2");

	/**
	 * Minecraft 1.14.
	 */
	public static final MCVersion V1_14 = get("1.14");

	/**
	 * Minecraft 1.14.1.
	 */
	public static final MCVersion V1_14_1 = get("1.14.1");

	/**
	 * Minecraft 1.14.2.
	 */
	public static final MCVersion V1_14_2 = get("1.14.2");

	/**
	 * Minecraft 1.14.3.
	 */
	public static final MCVersion V1_14_3 = get("1.14.3");

	/**
	 * Minecraft 1.14.4.
	 */
	public static final MCVersion V1_14_4 = get("1.14.4");

	private MCVersions() {}

	/**
	 * Returns all Minecraft versions supported by CurseForge.
	 *
	 * @return a mutable {@link SortedSet} containing {@link MCVersion} instances that
	 * represent all Minecraft versions supported by CurseForge.
	 */
	public static SortedSet<MCVersion> getAll() {
		return new TreeSet<>(ForgeSVCMinecraftProvider.versions);
	}

	/**
	 * Returns a {@link Stream} for all Minecraft versions supported by CurseForge.
	 *
	 * @return a {@link Stream} for all Minecraft versions supported by CurseForge.
	 */
	public static Stream<MCVersion> streamAll() {
		return ForgeSVCMinecraftProvider.versions.stream();
	}

	private static MCVersion get(String versionString) {
		if (ForgeSVCMinecraftProvider.failedToRetrieveVersions) {
			final MCVersion version =
					new MCVersion(ForgeSVCMinecraftProvider.versions.size(), versionString);
			ForgeSVCMinecraftProvider.versions.add(version);
			return version;
		}

		for (MCVersion version : ForgeSVCMinecraftProvider.versions) {
			if (versionString.equals(version.versionString())) {
				return version;
			}
		}

		throw new IllegalArgumentException("Invalid version string: " + versionString);
	}
}
