package com.therandomlabs.curseapi.minecraft;

import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;

public final class CurseAPIMinecraft {
	private CurseAPIMinecraft() {}

	public static void clearAllCache() {
		MinecraftForge.clearChangelogCache();
	}
}
