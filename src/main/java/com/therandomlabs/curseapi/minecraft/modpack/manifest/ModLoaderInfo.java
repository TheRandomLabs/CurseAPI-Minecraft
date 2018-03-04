package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.Serializable;

public final class ModLoaderInfo implements Cloneable, Serializable {
	private static final long serialVersionUID = -5375584834183726145L;

	public String id;
	public boolean primary = true;

	public ModLoaderInfo() {}

	public ModLoaderInfo(String forgeVersion) {
		id = "forge-" + forgeVersion.split("-")[1];
	}

	@Override
	public ModLoaderInfo clone() {
		final ModLoaderInfo info = new ModLoaderInfo();

		info.id = id;
		info.primary = primary;

		return info;
	}

	@Override
	public String toString() {
		return "[id=" + id + ",primary=" + primary + "]";
	}
}
