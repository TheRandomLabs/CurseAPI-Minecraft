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

package com.therandomlabs.curseapi.minecraft;

import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import com.therandomlabs.curseapi.CurseAPIProvider;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.game.CurseGameVersion;
import com.therandomlabs.curseapi.util.RetrofitUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CurseAPIProvider} that uses the API at {@code https://addons-ecs.forgesvc.net/}
 * used by the Twitch launcher to provide {@link CurseGameVersion}s for Minecraft.
 */
public final class ForgeSvcMinecraftProvider implements CurseAPIProvider {
	/**
	 * The singleton instance of {@link ForgeSvcMinecraftProvider}.
	 */
	public static final ForgeSvcMinecraftProvider INSTANCE = new ForgeSvcMinecraftProvider();

	static final ForgeSvcMinecraft FORGESVC_MINECRAFT =
			RetrofitUtils.get("https://addons-ecs.forgesvc.net/").create(ForgeSvcMinecraft.class);

	static final SortedSet<MCVersion> versions = getVersions();
	static boolean failedToRetrieveVersions;

	private ForgeSvcMinecraftProvider() {}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public NavigableSet<? extends CurseGameVersion<?>> gameVersions(int id) {
		MCVersions.initialize();
		return id == CurseAPIMinecraft.MINECRAFT_ID ? new TreeSet<>(versions) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public CurseGameVersion<?> gameVersion(int gameID, String versionString) {
		MCVersions.initialize();
		return gameID == CurseAPIMinecraft.MINECRAFT_ID ? MCVersions.get(versionString) : null;
	}

	@SuppressWarnings("PMD.ForLoopCanBeForeach")
	private static SortedSet<MCVersion> getVersions() {
		final Logger logger = LoggerFactory.getLogger(ForgeSvcMinecraftProvider.class);

		try {
			final List<MCVersion> versions =
					RetrofitUtils.execute(FORGESVC_MINECRAFT.getVersions());

			if (versions == null) {
				throw new CurseException("Could not retrieve Minecraft versions");
			}

			for (int i = 0; i < versions.size(); i++) {
				final MCVersion version = versions.get(i);
				//Initialize MCVersion#versionGroup so that the MCVersion adds itself to the
				//version group.
				version.versionGroup();
				//We multiply the index by 2 so that snapshots defined in MCVersions can fit in.
				version.setSortIndex((versions.size() - i - 1) * 2);
			}

			return new TreeSet<>(versions);
		} catch (CurseException ex) {
			logger.error(
					"Failed to retrieve Minecraft versions; a local copy will be used instead", ex
			);
		}

		failedToRetrieveVersions = true;
		//When MCVersions is initialized, it sees that failedToRetrieveVersions is true and
		//adds local MCVersion instances to this TreeSet.
		return new TreeSet<>();
	}
}
