package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.ImmutableMap;
import com.therandomlabs.utils.collection.ImmutableSet;

public class ForgeVersionChange extends VersionChange {
	private static final long serialVersionUID = -4645680856319627592L;

	private final String oldVersion;
	private final String newVersion;
	private final boolean isDowngrade;

	ForgeVersionChange(String mcVersion, String oldVersion, String newVersion,
			boolean isDowngrade) {
		super(mcVersion, null, null);
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
		this.isDowngrade = isDowngrade;
	}

	@Override
	public Mod getOldMod() {
		return null;
	}

	@Override
	public CurseFile getOldFile() {
		return null;
	}

	@Override
	public String getOldFileName() {
		return oldVersion;
	}

	@Override
	public Mod getNewMod() {
		return null;
	}

	@Override
	public CurseFile getNewFile() {
		return null;
	}

	@Override
	public String getNewFileName() {
		return newVersion;
	}

	@Override
	public boolean isDowngrade() {
		return isDowngrade;
	}

	@Override
	public String getModTitle() {
		return MinecraftForge.TITLE;
	}

	@Override
	public CurseProject getProject() {
		return null;
	}

	@Override
	void preload() throws CurseException {
		try {
			MinecraftForge.getChangelog();
		} catch(IOException ex) {
			throw CurseException.fromThrowable(ex);
		}
	}

	@Override
	List<String> getURLsToPreload() {
		return ImmutableList.empty();
	}

	@Override
	public Map<String, String> getChangelogs(boolean urls) throws CurseException, IOException {
		if(urls) {
			final String newerVersion = isDowngrade() ? oldVersion : newVersion;
			final URL changelogURL = MinecraftForge.getChangelogURL(newerVersion);
			return new ImmutableMap<>(
					new ImmutableSet<>(ManifestComparer.VIEW_CHANGELOG_AT),
					new ImmutableList<>(changelogURL.toString())
			);
		}
		return MinecraftForge.getChangelog(oldVersion, newVersion);
	}
}
