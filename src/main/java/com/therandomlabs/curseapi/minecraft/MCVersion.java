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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import com.therandomlabs.curseapi.game.CurseGameVersion;
import com.therandomlabs.curseapi.game.CurseGameVersionGroup;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a version of Minecraft supported by CurseForge.
 */
public final class MCVersion extends CurseGameVersion<MCVersion> {
	private static class MCVersionGroup extends CurseGameVersionGroup<MCVersion> {
		private static final Splitter FULL_STOP_SPLITTER = Splitter.on('.');

		private static final Map<String, MCVersionGroup> versionGroups = new HashMap<>();

		private String versionString;
		private Set<MCVersion> versions = new HashSet<>();

		private MCVersionGroup(String versionString) {
			this.versionString = versionString;
		}

		@Override
		public int gameID() {
			return CurseAPIMinecraft.MINECRAFT_ID;
		}

		@Override
		public String versionString() {
			return versionString;
		}

		@Override
		public Set<MCVersion> versions() {
			return new TreeSet<>(versions);
		}

		static CurseGameVersionGroup<MCVersion> get(MCVersion version) {
			final List<String> versionElements =
					FULL_STOP_SPLITTER.splitToList(version.versionString());

			//Modloaders don't have version groups.
			if (Ints.tryParse(versionElements.get(0)) == null) {
				return CurseGameVersionGroup.none(CurseAPIMinecraft.MINECRAFT_ID);
			}

			final String versionString = versionElements.get(0) + "." + versionElements.get(1);
			final MCVersionGroup versionGroup =
					versionGroups.computeIfAbsent(versionString, MCVersionGroup::new);
			versionGroup.versions.add(version);
			return versionGroup;
		}
	}

	private int sortIndex;
	private final String versionString;
	private boolean unknown;

	@Nullable
	private transient CurseGameVersionGroup<MCVersion> versionGroup;

	MCVersion(int sortIndex, String versionString) {
		this.sortIndex = sortIndex;
		this.versionString = versionString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseGameVersionGroup<MCVersion> versionGroup() {
		if (versionGroup == null) {
			versionGroup = MCVersionGroup.get(this);
		}

		return versionGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int gameID() {
		return CurseAPIMinecraft.MINECRAFT_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String versionString() {
		return versionString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(MCVersion version) {
		return Integer.compare(sortIndex, version.sortIndex);
	}

	/**
	 * Returns whether this {@link MCVersion} represents an unknown version of Minecraft.
	 *
	 * @return {@code true} if this {@link MCVersion} is {@link MCVersions#UNKNOWN},
	 * or ottherwise {@code false}.
	 */
	public boolean isUnknown() {
		return unknown;
	}

	//This method is called by MCVersions.
	void setUnknown() {
		unknown = true;
	}

	//This method is called by MCVersions.
	int getSortIndex() {
		return sortIndex;
	}

	//This method is called by ForgeSVCMinecraftProvider and MCVersions.
	void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
}
