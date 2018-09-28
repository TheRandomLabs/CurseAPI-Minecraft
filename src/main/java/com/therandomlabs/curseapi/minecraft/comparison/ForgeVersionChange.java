package com.therandomlabs.curseapi.minecraft.comparison;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;

public class ForgeVersionChange extends ModLoaderVersionChange {
	ForgeVersionChange(MCVersion mcVersion, String oldVersion, String newVersion,
			boolean isDowngrade) {
		super(mcVersion, oldVersion, newVersion, isDowngrade);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof ForgeVersionChange && object.hashCode() == hashCode();
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

	@Override
	public Map<String, String> getChangelogs(boolean urls) throws CurseException {
		try {
			final String oldVersion = getOldFileName();
			final String newVersion = getNewFileName();

			if(urls) {
				final String newerVersion = isDowngrade() ? oldVersion : newVersion;
				final URL changelogURL = MinecraftForge.getChangelogURL(newerVersion);

				return Collections.singletonMap(
						ModListComparer.VIEW_CHANGELOG_AT,
						changelogURL.toString()
				);
			}

			return MinecraftForge.getChangelog(oldVersion, newVersion);
		} catch(IOException ex) {
			throw CurseException.fromThrowable(ex);
		}
	}
}
