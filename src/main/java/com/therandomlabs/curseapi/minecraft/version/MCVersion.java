package com.therandomlabs.curseapi.minecraft.version;

import com.google.gson.annotations.SerializedName;
import com.therandomlabs.curseapi.game.GameVersion;
import com.therandomlabs.curseapi.game.GameVersionHandler;
import com.therandomlabs.curseapi.game.GameVersions;

public enum MCVersion implements GameVersion<MCVersion, MCVersionGroup> {
	@SerializedName("1.14")
	V1_14,
	@SerializedName("1.14-Snapshot")
	V1_14_SNAPSHOT,
	@SerializedName("1.13.2")
	V1_13_2,
	@SerializedName("1.13.1")
	V1_13_1,
	@SerializedName("1.13")
	V1_13,
	@SerializedName("1.13-Snapshot")
	V1_13_SNAPSHOT,
	@SerializedName("1.12.2")
	V1_12_2,
	@SerializedName("1.12.1")
	V1_12_1,
	@SerializedName("1.12")
	V1_12,
	@SerializedName("1.12-Snapshot")
	V1_12_SNAPSHOT,
	@SerializedName("1.11.2")
	V1_11_2,
	@SerializedName("1.11.1")
	V1_11_1,
	@SerializedName("1.11")
	V1_11,
	@SerializedName("1.11-Snapshot")
	V1_11_SNAPSHOT,
	@SerializedName("1.10.2")
	V1_10_2,
	@SerializedName("1.10.1")
	V1_10_1,
	@SerializedName("1.10")
	V1_10,
	@SerializedName("1.10-Snapshot")
	V1_10_SNAPSHOT,
	@SerializedName("1.9.4")
	V1_9_4,
	@SerializedName("1.9.3")
	V1_9_3,
	@SerializedName("1.9.2")
	V1_9_2,
	@SerializedName("1.9.1")
	V1_9_1,
	@SerializedName("1.9")
	V1_9,
	@SerializedName("1.9-Snapshot")
	V1_9_SNAPSHOT,
	@SerializedName("1.8.9")
	V1_8_9,
	@SerializedName("1.8.8")
	V1_8_8,
	@SerializedName("1.8.7")
	V1_8_7,
	@SerializedName("1.8.6")
	V1_8_6,
	@SerializedName("1.8.5")
	V1_8_5,
	@SerializedName("1.8.4")
	V1_8_4,
	@SerializedName("1.8.3")
	V1_8_3,
	@SerializedName("1.8.2")
	V1_8_2,
	@SerializedName("1.8.1")
	V1_8_1,
	@SerializedName("1.8")
	V1_8,
	@SerializedName("1.8-Snapshot")
	V1_8_SNAPSHOT,
	@SerializedName("1.7.10")
	V1_7_10,
	@SerializedName("1.7.9")
	V1_7_9,
	@SerializedName("1.7.8")
	V1_7_8,
	@SerializedName("1.7.7")
	V1_7_7,
	@SerializedName("1.7.6")
	V1_7_6,
	@SerializedName("1.7.5")
	V1_7_5,
	@SerializedName("1.7.4")
	V1_7_4,
	@SerializedName("1.7.2")
	V1_7_2,
	@SerializedName("1.6.4")
	V1_6_4,
	@SerializedName("1.6.2")
	V1_6_2,
	@SerializedName("1.6.1")
	V1_6_1,
	@SerializedName("1.5.2")
	V1_5_2,
	@SerializedName("1.5.1")
	V1_5_1,
	@SerializedName("1.5.0")
	V1_5_0,
	@SerializedName("1.4.7")
	V1_4_7,
	@SerializedName("1.4.6")
	V1_4_6,
	@SerializedName("1.4.5")
	V1_4_5,
	@SerializedName("1.4.4")
	V1_4_4,
	@SerializedName("1.4.2")
	V1_4_2,
	@SerializedName("1.3.2")
	V1_3_2,
	@SerializedName("1.3.1")
	V1_3_1,
	@SerializedName("1.2.5")
	V1_2_5,
	@SerializedName("1.2.4")
	V1_2_4,
	@SerializedName("1.2.3")
	V1_2_3,
	@SerializedName("1.2.2")
	V1_2_2,
	@SerializedName("1.2.1")
	V1_2_1,
	@SerializedName("1.1")
	V1_1,
	@SerializedName("1.0.0")
	V1_0_0,
	@SerializedName("Unknown")
	UNKNOWN;

	@SuppressWarnings("unchecked")
	public static final GameVersionHandler<MCVersion, MCVersionGroup> HANDLER =
			new MCVersionHandler();

	MCVersionGroup group;
	private final String version;

	static {
		GameVersions.registerHandler(HANDLER);
	}

	MCVersion() {
		version = super.toString().substring(1).replaceAll("_", ".").
				replaceAll(".SNAPSHOT", "-Snapshot");
	}

	@Override
	public String toString() {
		return version;
	}

	@Override
	public MCVersionGroup getGroup() {
		//Make sure MCVersionGroup is initialized since MCVersionGroup sets MCVersion.group
		MCVersionGroup.values();
		return group;
	}
}
