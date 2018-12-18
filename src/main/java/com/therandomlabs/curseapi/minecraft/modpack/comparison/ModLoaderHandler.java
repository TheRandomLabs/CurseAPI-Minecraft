package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import com.therandomlabs.curseapi.minecraft.version.MCVersion;

public interface ModLoaderHandler {
	int compare(String oldVersion, String newVersion);

	ModLoaderVersionChange getVersionChange(MCVersion version, String oldVersion, String newVersion,
			boolean isDowngrade);
}
