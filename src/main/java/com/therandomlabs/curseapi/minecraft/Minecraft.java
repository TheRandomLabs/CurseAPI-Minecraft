package com.therandomlabs.curseapi.minecraft;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.platform.Platform;

public final class Minecraft {
	public static final TRLList<String> CLIENT_ONLY_FILES = new ImmutableList<>(
			"resourcepacks",
			"server-resource-packs",
			"shaderpacks",
			"options.txt",
			"optionsof.txt",
			"realms_persistence.json",
			"servers.dat"
	);

	private Minecraft() {}

	public static Path getDirectory() {
		switch(Platform.OS) {
		case WINDOWS:
			return Paths.get(System.getenv("APPDATA"), ".minecraft");
		case MAC_OS_X:
			return Paths.get("~/Library/Application Support/minecraft");
		default:
			return Paths.get("~/.minecraft");
		}
	}
}
