package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.Serializable;
import java.util.Arrays;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.misc.Assertions;

public final class MinecraftInfo implements Cloneable, Serializable {
	private static final long serialVersionUID = -5189220713785105134L;

	public MinecraftVersion version;
	public String libraries = "libraries";
	public ModLoaderInfo[] modLoaders;

	public MinecraftInfo() {}

	public MinecraftInfo(String forgeVersion) {
		this.version = MinecraftForge.getMCVersion(forgeVersion);
		modLoaders = new ModLoaderInfo[1];
		modLoaders[0] = new ModLoaderInfo(forgeVersion);
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

	public String getForgeVersion() {
		return modLoaders[0].id.replaceAll("forge", version.toString());
	}
}
