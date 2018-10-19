package com.therandomlabs.curseapi.minecraft;

import com.google.gson.annotations.SerializedName;

public enum Side {
	@SerializedName("both")
	BOTH("both"),
	@SerializedName("client")
	CLIENT("client"),
	@SerializedName("server")
	SERVER("server");

	private final String name;

	Side(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static Side fromBooleans(boolean client, boolean server, boolean both) {
		if(both || (client && server) || (!client && !server)) {
			return BOTH;
		}

		return client ? CLIENT : SERVER;
	}
}
