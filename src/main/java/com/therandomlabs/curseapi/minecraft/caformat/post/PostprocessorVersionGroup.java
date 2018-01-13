package com.therandomlabs.curseapi.minecraft.caformat.post;

import java.util.Collections;
import java.util.List;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.caformat.CAManifest;
import com.therandomlabs.curseapi.minecraft.caformat.Variable;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.ImmutableList;

public class PostprocessorVersionGroup extends Postprocessor {
	PostprocessorVersionGroup() {}

	@Override
	public String toString() {
		return "=";
	}

	@Override
	public boolean isValid(String value, String[] args) {
		return MinecraftVersion.groupFromString(args[0]) != null;
	}

	@Override
	public List<String> apply(CAManifest manifest, String value, String[] args) {
		final MinecraftVersion modpackVersion = MinecraftVersion.groupFromString(
				manifest.getVariables().get(Variable.MINECRAFT));
		final MinecraftVersion toCompare = MinecraftVersion.groupFromString(args[0]);

		if(modpackVersion == toCompare) {
			return new ImmutableList<>(ArrayUtils.join(ArrayUtils.subArray(args, 1), " "));
		}

		return Collections.emptyList();
	}
}
