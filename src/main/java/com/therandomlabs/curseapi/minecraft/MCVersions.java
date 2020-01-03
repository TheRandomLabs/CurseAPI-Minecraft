/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
	 * Represents an unknown version of Minecraft.
	 */
	public static final MCVersion UNKNOWN = new MCVersion(-1, "Unknown");

	/**
	 * Minecraft 1.0.
	 */
	public static final MCVersion V1_0 = initialize("1.0");

	/**
	 * Minecraft 1.1.
	 */
	public static final MCVersion V1_1 = initialize("1.1");

	/**
	 * Minecraft 1.2.1.
	 */
	public static final MCVersion V1_2_1 = initialize("1.2.1");

	/**
	 * Minecraft 1.2.2.
	 */
	public static final MCVersion V1_2_2 = initialize("1.2.2");

	/**
	 * Minecraft 1.2.3.
	 */
	public static final MCVersion V1_2_3 = initialize("1.2.3");

	/**
	 * Minecraft 1.2.4.
	 */
	public static final MCVersion V1_2_4 = initialize("1.2.4");

	/**
	 * Minecraft 1.2.5.
	 */
	public static final MCVersion V1_2_5 = initialize("1.2.5");

	/**
	 * Minecraft 1.3.1.
	 */
	public static final MCVersion V1_3_1 = initialize("1.3.1");

	/**
	 * Minecraft 1.3.2.
	 */
	public static final MCVersion V1_3_2 = initialize("1.3.2");

	/**
	 * Minecraft 1.4.2.
	 */
	public static final MCVersion V1_4_2 = initialize("1.4.2");

	/**
	 * Minecraft 1.4.4.
	 */
	public static final MCVersion V1_4_4 = initialize("1.4.4");

	/**
	 * Minecraft 1.4.5.
	 */
	public static final MCVersion V1_4_5 = initialize("1.4.5");

	/**
	 * Minecraft 1.4.6.
	 */
	public static final MCVersion V1_4_6 = initialize("1.4.6");

	/**
	 * Minecraft 1.4.7.
	 */
	public static final MCVersion V1_4_7 = initialize("1.4.7");

	/**
	 * Minecraft 1.5.1.
	 */
	public static final MCVersion V1_5_1 = initialize("1.5.1");

	/**
	 * Minecraft 1.5.2.
	 */
	public static final MCVersion V1_5_2 = initialize("1.5.2");

	/**
	 * Minecraft 1.6.1.
	 */
	public static final MCVersion V1_6_1 = initialize("1.6.1");

	/**
	 * Minecraft 1.6.2.
	 */
	public static final MCVersion V1_6_2 = initialize("1.6.2");

	/**
	 * Minecraft 1.6.4.
	 */
	public static final MCVersion V1_6_4 = initialize("1.6.4");

	/**
	 * Minecraft 1.7.2.
	 */
	public static final MCVersion V1_7_2 = initialize("1.7.2");

	/**
	 * Minecraft 1.7.3.
	 */
	public static final MCVersion V1_7_3 = initialize("1.7.3");

	/**
	 * Minecraft 1.7.4.
	 */
	public static final MCVersion V1_7_4 = initialize("1.7.4");

	/**
	 * Minecraft 1.7.5.
	 */
	public static final MCVersion V1_7_5 = initialize("1.7.5");

	/**
	 * Minecraft 1.7.6.
	 */
	public static final MCVersion V1_7_6 = initialize("1.7.6");

	/**
	 * Minecraft 1.7.7.
	 */
	public static final MCVersion V1_7_7 = initialize("1.7.7");

	/**
	 * Minecraft 1.7.8.
	 */
	public static final MCVersion V1_7_8 = initialize("1.7.8");

	/**
	 * Minecraft 1.7.9.
	 */
	public static final MCVersion V1_7_9 = initialize("1.7.9");

	/**
	 * Minecraft 1.7.10.
	 */
	public static final MCVersion V1_7_10 = initialize("1.7.10");

	/**
	 * Minecraft 1.8 snapshots.
	 */
	public static final MCVersion V1_8_SNAPSHOT = create("1.8", "1.8-Snapshot");

	/**
	 * Minecraft 1.8.
	 */
	public static final MCVersion V1_8 = initialize("1.8");

	/**
	 * Minecraft 1.8.1.
	 */
	public static final MCVersion V1_8_1 = initialize("1.8.1");

	/**
	 * Minecraft 1.8.2.
	 */
	public static final MCVersion V1_8_2 = initialize("1.8.2");

	/**
	 * Minecraft 1.8.3.
	 */
	public static final MCVersion V1_8_3 = initialize("1.8.3");

	/**
	 * Minecraft 1.8.4.
	 */
	public static final MCVersion V1_8_4 = initialize("1.8.4");

	/**
	 * Minecraft 1.8.5.
	 */
	public static final MCVersion V1_8_5 = initialize("1.8.5");

	/**
	 * Minecraft 1.8.6.
	 */
	public static final MCVersion V1_8_6 = initialize("1.8.6");

	/**
	 * Minecraft 1.8.7.
	 */
	public static final MCVersion V1_8_7 = initialize("1.8.7");

	/**
	 * Minecraft 1.8.8.
	 */
	public static final MCVersion V1_8_8 = initialize("1.8.8");

	/**
	 * Minecraft 1.8.9.
	 */
	public static final MCVersion V1_8_9 = initialize("1.8.9");

	/**
	 * Minecraft 1.9 snapshots.
	 */
	public static final MCVersion V1_9_SNAPSHOT = create("1.9", "1.9-Snapshot");

	/**
	 * Minecraft 1.9.
	 */
	public static final MCVersion V1_9 = initialize("1.9");

	/**
	 * Minecraft 1.9.1.
	 */
	public static final MCVersion V1_9_1 = initialize("1.9.1");

	/**
	 * Minecraft 1.9.2.
	 */
	public static final MCVersion V1_9_2 = initialize("1.9.2");

	/**
	 * Minecraft 1.9.3.
	 */
	public static final MCVersion V1_9_3 = initialize("1.9.3");

	/**
	 * Minecraft 1.9.4.
	 */
	public static final MCVersion V1_9_4 = initialize("1.9.4");

	/**
	 * Minecraft 1.10 snapshots.
	 */
	public static final MCVersion V1_10_SNAPSHOT = create("1.10", "1.10-Snapshot");

	/**
	 * Minecraft 1.10.
	 */
	public static final MCVersion V1_10 = initialize("1.10");

	/**
	 * Minecraft 1.10.1.
	 */
	public static final MCVersion V1_10_1 = initialize("1.10.1");

	/**
	 * Minecraft 1.10.2.
	 */
	public static final MCVersion V1_10_2 = initialize("1.10.2");

	/**
	 * Minecraft 1.11 snapshots.
	 */
	public static final MCVersion V1_11_SNAPSHOT = create("1.11", "1.11-Snapshot");

	/**
	 * Minecraft 1.11.
	 */
	public static final MCVersion V1_11 = initialize("1.11");

	/**
	 * Minecraft 1.11.1.
	 */
	public static final MCVersion V1_11_1 = initialize("1.11.1");

	/**
	 * Minecraft 1.11.2.
	 */
	public static final MCVersion V1_11_2 = initialize("1.11.2");

	/**
	 * Minecraft 1.12 snapshots.
	 */
	public static final MCVersion V1_12_SNAPSHOT = create("1.12", "1.12-Snapshot");

	/**
	 * Minecraft 1.12.
	 */
	public static final MCVersion V1_12 = initialize("1.12");

	/**
	 * Minecraft 1.12.1.
	 */
	public static final MCVersion V1_12_1 = initialize("1.12.1");

	/**
	 * Minecraft 1.12.2.
	 */
	public static final MCVersion V1_12_2 = initialize("1.12.2");

	/**
	 * Minecraft 1.13 snapshots.
	 */
	public static final MCVersion V1_13_SNAPSHOT = create("1.13", "1.13-Snapshot");

	/**
	 * Minecraft 1.13.
	 */
	public static final MCVersion V1_13 = initialize("1.13");

	/**
	 * Minecraft 1.13.1.
	 */
	public static final MCVersion V1_13_1 = initialize("1.13.1");

	/**
	 * Minecraft 1.13.2.
	 */
	public static final MCVersion V1_13_2 = initialize("1.13.2");

	/**
	 * Minecraft 1.14 snapshots.
	 */
	public static final MCVersion V1_14_SNAPSHOT = create("1.14", "1.14-Snapshot");

	/**
	 * Minecraft 1.14.
	 */
	public static final MCVersion V1_14 = initialize("1.14");

	/**
	 * Minecraft 1.14.1.
	 */
	public static final MCVersion V1_14_1 = initialize("1.14.1");

	/**
	 * Minecraft 1.14.2.
	 */
	public static final MCVersion V1_14_2 = initialize("1.14.2");

	/**
	 * Minecraft 1.14.3.
	 */
	public static final MCVersion V1_14_3 = initialize("1.14.3");

	/**
	 * Minecraft 1.14.4.
	 */
	public static final MCVersion V1_14_4 = initialize("1.14.4");

	/**
	 * Minecraft 1.15 snapshots.
	 */
	public static final MCVersion V1_15_SNAPSHOT = create("1.15", "1.15-Snapshot");

	/**
	 * Minecraft 1.15.
	 */
	public static final MCVersion V1_15 = initialize("1.15");

	/**
	 * Minecraft 1.15.1.
	 */
	public static final MCVersion V1_15_1 = initialize("1.15.1");

	/**
	 * The Fabric modloader.
	 */
	public static final MCVersion FABRIC = initialize("Fabric");

	/**
	 * The Minecraft Forge modloader.
	 */
	public static final MCVersion FORGE = initialize("Forge");

	/**
	 * The Rift modloader.
	 */
	public static final MCVersion RIFT = initialize("Rift");

	static {
		UNKNOWN.setUnknown();
	}

	private MCVersions() {}

	/**
	 * Returns all Minecraft versions supported by CurseForge.
	 *
	 * @return a mutable {@link SortedSet} containing {@link MCVersion} instances that
	 * represent all Minecraft versions supported by CurseForge.
	 */
	public static SortedSet<MCVersion> getAll() {
		return new TreeSet<>(ForgeSvcMinecraftProvider.versions);
	}

	/**
	 * Returns a {@link Stream} for all Minecraft versions supported by CurseForge.
	 *
	 * @return a {@link Stream} for all Minecraft versions supported by CurseForge.
	 */
	public static Stream<MCVersion> streamAll() {
		return ForgeSvcMinecraftProvider.versions.stream();
	}

	/**
	 * Returns the {@link MCVersion} instance for the specified version string.
	 *
	 * @param versionString a Minecraft version string.
	 * @return the {@link MCVersion} instance for the specified version string.
	 */
	public static MCVersion get(String versionString) {
		if (versionString == null) {
			return UNKNOWN;
		}

		for (MCVersion version : ForgeSvcMinecraftProvider.versions) {
			if (versionString.equals(version.versionString())) {
				return version;
			}
		}

		return UNKNOWN;
	}

	static void initialize() {
		//This is called by ForgeSVCMinecraftProvider.
	}

	private static MCVersion initialize(String versionString) {
		if (ForgeSvcMinecraftProvider.failedToRetrieveVersions) {
			return create(-1, versionString);
		}

		for (MCVersion version : ForgeSvcMinecraftProvider.versions) {
			if (versionString.equals(version.versionString())) {
				return version;
			}
		}

		//The Minecraft versions API often takes some time to acknowledge newer versions of
		//Minecraft.
		return create(-1, versionString);
	}

	//Snapshots and modloaders are also not provided in the Minecraft versions API.
	private static MCVersion create(int index, String versionString) {
		if (index == -1) {
			index = ForgeSvcMinecraftProvider.versions.size() * 2;
		}

		final MCVersion version = new MCVersion(index, versionString);
		//Initialize MCVersion#versionGroup so that the MCVersion adds itself to the version group.
		version.versionGroup();
		ForgeSvcMinecraftProvider.versions.add(version);
		return version;
	}

	//This method is used to create snapshot versions.
	private static MCVersion create(String nextVersionString, String versionString) {
		if (ForgeSvcMinecraftProvider.failedToRetrieveVersions) {
			return create(-1, versionString);
		}

		final MCVersion nextVersion = get(nextVersionString);
		//Since sort indexes are normally separated by 2,
		//there should be a gap that this version can fit in.
		return create(nextVersion.isUnknown() ? -1 : nextVersion.getSortIndex() - 1, versionString);
	}
}
