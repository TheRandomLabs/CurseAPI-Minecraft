package com.therandomlabs.curseapi.minecraft.modpack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.MinimalCurseFile;
import com.therandomlabs.curseapi.minecraft.MCVersion;
import com.therandomlabs.curseapi.minecraft.MCVersions;
import com.therandomlabs.curseapi.util.MoshiUtils;

@SuppressWarnings("squid:S1068")
final class DefaultCurseModpack implements CurseModpack {
	private static final class MinecraftInfo {
		private static final class ModLoaderInfo {
			String id = "forge-14.23.5.2847";
			boolean primary = true;
		}

		String version = MCVersions.V1_12_2.versionString();
		List<ModLoaderInfo> modLoaders = Collections.singletonList(new ModLoaderInfo());
	}

	private static final class FileInfo extends MinimalCurseFile.Immutable {
		boolean required = true;

		FileInfo() {
			super(CurseAPI.MIN_PROJECT_ID, CurseAPI.MIN_FILE_ID);
		}
	}

	private MinecraftInfo minecraft = new MinecraftInfo();
	private String manifestType = "minecraftModpack";
	private int manifestVersion = 1;
	private String name = "";
	private String version = "";
	private String author = "";
	//This files field is used for Moshi so that it knows that files should be converted t o
	//MinimalCurseFile.Immutables.
	private List<FileInfo> files = new ArrayList<>();
	//This files field is used for everything else and uses the MinimalCurseFile type.
	private transient List<MinimalCurseFile> actualFiles;

	@Override
	public MCVersion mcVersion() {
		return MCVersions.get(minecraft.version);
	}

	@Override
	public CurseModpack mcVersion(MCVersion version) {
		Preconditions.checkNotNull(version, "version should not be null");
		minecraft.version = version.versionString();
		return this;
	}

	@Override
	public String forgeVersion() {
		//"forge-14.23.5.2847" becomes "1.12.2-14.23.5.2847".
		return minecraft.modLoaders.get(0).id.replace("forge", minecraft.version);
	}

	@Override
	public CurseModpack forgeVersion(String version) {
		Preconditions.checkNotNull(version, "version should not be null");

		final MinecraftInfo.ModLoaderInfo forge = minecraft.modLoaders.get(0);

		if (version.startsWith("forge")) {
			forge.id = version.replace("forge", minecraft.version);
		} else if (!version.startsWith(minecraft.version)) {
			forge.id = minecraft.version + '-' + version;
		} else {
			forge.id = version;
		}

		return this;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public CurseModpack name(String name) {
		Preconditions.checkNotNull(name, "name should not be null");
		this.name = name;
		return this;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public CurseModpack version(String version) {
		Preconditions.checkNotNull(version, "version should not be null");
		this.version = version;
		return this;
	}

	@Override
	public String author() {
		return author;
	}

	@Override
	public CurseModpack author(String author) {
		Preconditions.checkNotNull(version, "version should not be null");
		this.author = author;
		return this;
	}

	@Override
	public List<MinimalCurseFile> files() {
		if (actualFiles == null) {
			actualFiles = new ArrayList<>(files);
		}

		return actualFiles;
	}

	@Override
	public CurseModpack files(Collection<? extends MinimalCurseFile> files) {
		Preconditions.checkNotNull(files, "files should not be null");
		this.actualFiles.clear();
		this.actualFiles.addAll(files);
		return this;
	}

	@Override
	public String toJSON() {
		return MoshiUtils.toJSON(this, DefaultCurseModpack.class);
	}

	@Override
	public void toJSON(Path path) throws CurseException {
		MoshiUtils.toJSON(this, DefaultCurseModpack.class, path);
	}
}