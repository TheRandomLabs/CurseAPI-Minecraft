package com.therandomlabs.curseapi.minecraft.modpack.manifest;

public final class ModLoaderInfo implements Cloneable {
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
}
