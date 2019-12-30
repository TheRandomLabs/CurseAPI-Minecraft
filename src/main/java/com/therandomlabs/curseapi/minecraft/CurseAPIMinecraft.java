package com.therandomlabs.curseapi.minecraft;

import com.therandomlabs.curseapi.CurseAPI;

/**
 * The main CurseAPI-Minecraft class.
 */
public final class CurseAPIMinecraft {
	/**
	 * Minecraft's ID on CurseForge.
	 */
	public static final int MINECRAFT_ID = 432;

	private CurseAPIMinecraft() {}

	/**
	 * Initializes CurseAPI-Minecraft.
	 * This method should be called first in any project that uses CurseAPI-Minecraft.
	 */
	public static void initialize() {
		CurseAPI.addProvider(ForgeSvcMinecraftProvider.INSTANCE, false);
	}
}
