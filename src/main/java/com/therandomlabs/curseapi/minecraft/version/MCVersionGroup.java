package com.therandomlabs.curseapi.minecraft.version;

import com.google.gson.annotations.SerializedName;
import com.therandomlabs.curseapi.game.GameVersionGroup;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;

public enum MCVersionGroup implements GameVersionGroup<MCVersion, MCVersionGroup> {
	@SerializedName("1.0-Group")
	V1_0,
	@SerializedName("1.1-Group")
	V1_1,
	@SerializedName("1.2-Group")
	V1_2,
	@SerializedName("1.3-Group")
	V1_3,
	@SerializedName("1.4-Group")
	V1_4,
	@SerializedName("1.5-Group")
	V1_5,
	@SerializedName("1.6-Group")
	V1_6,
	@SerializedName("1.7-Group")
	V1_7,
	@SerializedName("1.8-Group")
	V1_8,
	@SerializedName("1.9-Group")
	V1_9,
	@SerializedName("1.10-Group")
	V1_10,
	@SerializedName("1.11-Group")
	V1_11,
	@SerializedName("1.12-Group")
	V1_12,
	@SerializedName("1.13-Group")
	V1_13,
	@SerializedName("1.14-Group")
	V1_14;

	private final String id;
	private final ImmutableList<MCVersion> versions;

	MCVersionGroup() {
		id = super.toString().substring(1).replaceAll("_", ".");

		final TRLList<MCVersion> versions = new TRLList<>();

		for(MCVersion version : MCVersion.values()) {
			final String id = version.id();

			if(id.equals(this.id) || id.startsWith(this.id + ".")) {
				versions.add(version);
				version.group = this;
			}
		}

		this.versions = versions.toImmutableList();
	}

	@Override
	public String toString() {
		return id + " Group";
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public ImmutableList<MCVersion> getVersions() {
		return versions;
	}
}
