package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.List;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;

public class PostprocessorVersionGroup extends Postprocessor {
	PostprocessorVersionGroup() {}

	@Override
	public String toString() {
		return "=version";
	}

	@Override
	public boolean isValid(String value) {
		return MinecraftVersion.groupFromString(value) != null;
	}

	@Override
	public List<String> apply(CAManifest manifest, String value) {
		//TODO
		return null;
	}
}
