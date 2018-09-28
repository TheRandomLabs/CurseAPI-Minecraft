package com.therandomlabs.curseapi.minecraft.comparison;

import java.io.IOException;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;

public class ForgeVersionChange extends ModLoaderVersionChange {
	ForgeVersionChange(MCVersion mcVersion, String oldVersion, String newVersion,
			boolean isDowngrade) {
		super(mcVersion, oldVersion, newVersion, isDowngrade);
	}

	@Override
	public String getModTitle() {
		return MinecraftForge.TITLE;
	}

	@Override
	void loadChangelogFiles() throws CurseException {
		try {
			MinecraftForge.getChangelog();
		} catch(IOException ex) {
			throw CurseException.fromThrowable(ex);
		}
	}
}
