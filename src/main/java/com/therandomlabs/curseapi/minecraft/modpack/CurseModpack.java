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
import java.util.Collection;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.BasicCurseFile;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.minecraft.MCVersion;
import com.therandomlabs.curseapi.util.MoshiUtils;

/**
 * Represents a CurseForge Minecraft modpack.
 */
public interface CurseModpack {
	/**
	 * Returns this modpack's Minecraft version.
	 *
	 * @return an {@link MCVersion} instance representing this modpack's Minecraft version.
	 */
	MCVersion mcVersion();

	/**
	 * Sets this modpack's Minecraft version.
	 *
	 * @param version an {@link MCVersion}.
	 * @return this {@link CurseModpack}.
	 */
	CurseModpack mcVersion(MCVersion version);

	/**
	 * Returns this modpack's Minecraft Forge version.
	 *
	 * @return this modpack's Minecraft Forge version string.
	 */
	String forgeVersion();

	/**
	 * Sets this modpack's Minecraft Forge version.
	 *
	 * @param version a Minecraft Forge version string.
	 * @return this {@link CurseModpack}.
	 */
	CurseModpack forgeVersion(String version);

	/**
	 * Returns this modpack's name.
	 *
	 * @return this modpack's name.
	 */
	String name();

	/**
	 * Sets this modpack's name.
	 *
	 * @param name a name.
	 * @return this {@link CurseModpack}.
	 */
	CurseModpack name(String name);

	/**
	 * Returns this modpack's version.
	 *
	 * @return this modpack's version string.
	 */
	String version();

	/**
	 * Sets this modpack's version.
	 *
	 * @param version a version string.
	 * @return this {@link CurseModpack}.
	 */
	CurseModpack version(String version);

	/**
	 * Returns this modpack's author.
	 *
	 * @return this modpack's author.
	 */
	String author();

	/**
	 * Sets this modpack's author.
	 *
	 * @param author an author.
	 * @return this {@link CurseModpack}.
	 */
	CurseModpack author(String author);

	/**
	 * Returns this modpack's files as {@link BasicCurseFile}s.
	 *
	 * @return a {@link CurseFiles} containing {@link BasicCurseFile} instances that represent
	 * this modpack's files. Changes to this list are reflected in this
	 * {@link CurseModpack} instance.
	 */
	CurseFiles<BasicCurseFile> basicFiles();

	/**
	 * Returns this modpack's files.
	 *
	 * @return a {@link CurseFiles} containing {@link CurseFile} instances that represent
	 * this modpack's files. Changes to this list are <strong>not</strong> reflected in this
	 * {@link CurseModpack} instance.
	 */
	CurseFiles<CurseFile> files() throws CurseException;

	/**
	 * Sets this modpack's files.
	 *
	 * @param files a {@link Collection} of {@link BasicCurseFile}s.
	 * @return this {@link CurseModpack}.
	 */
	CurseModpack files(Collection<? extends BasicCurseFile> files);

	/**
	 * Returns this modpack as a JSON string.
	 *
	 * @return this modpack as a JSON string.
	 */
	String toJSON() throws CurseException;

	/**
	 * Writes this modpack as a JSON string to the specified {@link Path}.
	 *
	 * @param path a {@link Path}.
	 * @throws CurseException if an error occurs.
	 */
	void toJSON(Path path) throws CurseException;

	/**
	 * Creates a {@link CurseModpack} instance from a JSON string.
	 *
	 * @param json a JSON string.
	 * @return a {@link CurseModpack} instance.
	 * @throws CurseException if an error occurs.
	 */
	static CurseModpack fromJSON(String json) throws CurseException {
		return MoshiUtils.fromJSON(json, DefaultCurseModpack.class);
	}

	/**
	 * Parses the specified JSON string to create a {@link CurseModpack} instance.
	 *
	 * @param json a JSON string.
	 * @return a {@link CurseModpack} instance.
	 * @throws CurseException if an error occurs.
	 */
	static CurseModpack fromJSON(Path json) throws CurseException {
		return MoshiUtils.fromJSON(json, DefaultCurseModpack.class);
	}

	/**
	 * Returns an empty {@link CurseModpack} instance.
	 *
	 * @return an empty {@link CurseModpack} instance.
	 */
	static CurseModpack createEmpty() {
		return new DefaultCurseModpack();
	}
}
