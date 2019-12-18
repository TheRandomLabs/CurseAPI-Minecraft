package com.therandomlabs.curseapi.minecraft.modpack;

import java.nio.file.Path;
import java.util.Collection;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.BasicCurseFile;
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
	 * Returns this modpack's files.
	 *
	 * @return a mutable list containing {@link BasicCurseFile} instances that represent
	 * this modpack's files. Changes to this list are reflected in this
	 * {@link CurseModpack} instance.
	 */
	CurseFiles<BasicCurseFile> files();

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
	String toJSON();

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
