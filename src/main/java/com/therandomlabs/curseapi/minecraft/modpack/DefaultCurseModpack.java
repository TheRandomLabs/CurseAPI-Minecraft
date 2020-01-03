/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.curseapi.minecraft.modpack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.BasicCurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.minecraft.MCVersion;
import com.therandomlabs.curseapi.minecraft.MCVersions;
import com.therandomlabs.curseapi.util.MoshiUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings({"unused", "PMD.UnusedPrivateField", "squid:S1068"})
final class DefaultCurseModpack implements CurseModpack {
	private static final class MinecraftInfo {
		private static final class ModLoaderInfo {
			String id = "forge-14.23.5.2847";
			boolean primary = true;
		}

		String version = MCVersions.V1_12_2.versionString();
		List<ModLoaderInfo> modLoaders = Collections.singletonList(new ModLoaderInfo());
	}

	private static final class FileInfo extends BasicCurseFile.Immutable {
		boolean required = true;

		FileInfo() {
			super(CurseAPI.MIN_PROJECT_ID, CurseAPI.MIN_FILE_ID);
		}

		FileInfo(BasicCurseFile file) {
			super(file.projectID(), file.id());
		}
	}

	private MinecraftInfo minecraft = new MinecraftInfo();
	private String manifestType = "minecraftModpack";
	private int manifestVersion = 1;
	private String name = "";
	private String version = "";
	private String author = "";
	//This field is used for Moshi so that it knows that files should be converted to
	//BasicCurseFile.Immutables.
	private List<FileInfo> files = new ArrayList<>();
	//This field is used for everything else and uses the BasicCurseFile type.
	@Nullable
	private transient CurseFiles<BasicCurseFile> actualFiles;

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
	public CurseFiles<BasicCurseFile> files() {
		if (actualFiles == null) {
			actualFiles = new CurseFiles<>(files);
		}

		return actualFiles;
	}

	@Override
	public CurseModpack files(Collection<? extends BasicCurseFile> files) {
		Preconditions.checkNotNull(files, "files should not be null");
		files().clear();
		files().addAll(files);
		return this;
	}

	@Override
	public String toJSON() {
		//Convert BasicCurseFiles in actualFiles to FileInfos in files.
		files.clear();
		files().stream().map(FileInfo::new).forEach(files::add);
		return MoshiUtils.toJSON(this, DefaultCurseModpack.class);
	}

	@Override
	public void toJSON(Path path) throws CurseException {
		MoshiUtils.toJSON(this, DefaultCurseModpack.class, path);
	}
}
