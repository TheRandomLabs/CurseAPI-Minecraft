package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.util.CloneException;

public final class MinecraftInfo implements Cloneable {
	public MinecraftVersion version;
	public String libraries = "libraries";
	public ModLoaderInfo[] modLoaders;

	public MinecraftInfo() {}

	public MinecraftInfo(String version, String forgeVersion) {
		this(MinecraftVersion.fromString(version), forgeVersion);
	}

	public MinecraftInfo(MinecraftVersion version, String forgeVersion) {
		this.version = version;
		modLoaders = new ModLoaderInfo[1];
		modLoaders[0] = new ModLoaderInfo(forgeVersion);
	}

	@Override
	public MinecraftInfo clone() {
		final MinecraftInfo info = new MinecraftInfo();

		info.version = version;
		info.libraries = libraries;
		info.modLoaders = CloneException.tryClone(modLoaders);

		return info;
	}
}
