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

import com.therandomlabs.curseapi.game.CurseGameVersionGroup;

/**
 * Contains {@link CurseGameVersionGroup} constants that represent all version groups of Minecraft
 * supported by CurseForge.
 */
public final class MCVersionGroups {
	/**
	 * Minecraft 1.0.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_0 = MCVersions.V1_0.versionGroup();

	/**
	 * Minecraft 1.1.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_1 = MCVersions.V1_1.versionGroup();

	/**
	 * Minecraft 1.2.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_2 = MCVersions.V1_2_1.versionGroup();

	/**
	 * Minecraft 1.3.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_3 = MCVersions.V1_3_1.versionGroup();

	/**
	 * Minecraft 1.4.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_4 = MCVersions.V1_4_2.versionGroup();

	/**
	 * Minecraft 1.5.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_5 = MCVersions.V1_5_1.versionGroup();

	/**
	 * Minecraft 1.6.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_6 = MCVersions.V1_6_1.versionGroup();

	/**
	 * Minecraft 1.7.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_7 = MCVersions.V1_7_2.versionGroup();

	/**
	 * Minecraft 1.8.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_8 = MCVersions.V1_8.versionGroup();

	/**
	 * Minecraft 1.9.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_9 = MCVersions.V1_9.versionGroup();

	/**
	 * Minecraft 1.10.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_10 = MCVersions.V1_10.versionGroup();

	/**
	 * Minecraft 1.11.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_11 = MCVersions.V1_11.versionGroup();

	/**
	 * Minecraft 1.12.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_12 = MCVersions.V1_12.versionGroup();

	/**
	 * Minecraft 1.13.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_13 = MCVersions.V1_13.versionGroup();

	/**
	 * Minecraft 1.14.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_14 = MCVersions.V1_14.versionGroup();

	/**
	 * Minecraft 1.15.
	 */
	public static final CurseGameVersionGroup<MCVersion> V1_15 = MCVersions.V1_15.versionGroup();

	private MCVersionGroups() {}
}
