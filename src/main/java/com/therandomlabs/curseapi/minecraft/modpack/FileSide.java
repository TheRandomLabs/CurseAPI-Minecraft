package com.therandomlabs.curseapi.minecraft.modpack;

import com.google.gson.annotations.SerializedName;

public enum FileSide {
	@SerializedName("both")
	BOTH("both"),
	@SerializedName("client")
	CLIENT("client"),
	@SerializedName("server")
	SERVER("server");

	private final String name;

	FileSide(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static FileSide fromBooleans(boolean client, boolean server, boolean both) {
		if(both || (client && server) || (!client && !server && !both)) {
			return BOTH;
		}
		return client ? CLIENT : SERVER;
	}
}
