package com.therandomlabs.curseapi.minecraft.modpack;

import java.util.Arrays;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.misc.Assertions;

public final class MinecraftInfo implements Cloneable {
	public MCVersion version;
	public String libraries = "libraries";
	public ModLoaderInfo[] modLoaders;

	public MinecraftInfo() {}

	public MinecraftInfo(String forgeVersion) {
		version = MCVersion.HANDLER.get(forgeVersion.split("-")[0]);
		modLoaders = new ModLoaderInfo[1];
		modLoaders[0] = new ModLoaderInfo(forgeVersion);
	}

	@Override
	public MinecraftInfo clone() {
		try {
			final MinecraftInfo info = (MinecraftInfo) super.clone();
			info.modLoaders = Utils.tryClone(modLoaders);
			return info;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	@Override
	public String toString() {
		return "[version=\"" + version + ",modLoaders=" + Arrays.toString(modLoaders) + "]";
	}

	public void validate() {
		Assertions.nonNull(version, "version");
		Assertions.validPath(libraries);
		Assertions.nonNull(modLoaders, "modLoaders");

		boolean primaryFound = false;

		for(ModLoaderInfo modLoader : modLoaders) {
			modLoader.validate();

			if(modLoader.primary) {
				if(primaryFound) {
					throw new IllegalStateException("Only one mod loader may be primary");
				}

				primaryFound = true;
			}
		}

		if(!primaryFound) {
			throw new IllegalStateException("There must be a primary mod loader");
		}
	}

	public String forgeVersion() {
		return modLoaders[0].id.replaceAll("forge", version.toString());
	}
}
