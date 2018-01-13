package com.therandomlabs.curseapi.minecraft.caformat.post;

import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.caformat.CAManifest;
import com.therandomlabs.curseapi.minecraft.caformat.Variable;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.ImmutableList;

public class PostprocessorVersion extends Postprocessor {
	private final String string;
	private final IntPredicate predicate;

	PostprocessorVersion(String string, IntPredicate predicate) {
		this.string = string;
		this.predicate = predicate;
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public boolean isValid(String value, String[] args) {
		return MinecraftVersion.fromString(args[0]) != null;
	}

	@Override
	public List<String> apply(CAManifest manifest, String value, String[] args) {
		final MinecraftVersion modpackVersion = MinecraftVersion.fromString(
				manifest.getVariables().get(Variable.MINECRAFT));
		final MinecraftVersion toCompare = MinecraftVersion.fromString(args[0]);

		if(predicate.test(toCompare.compareTo(modpackVersion))) {
			return new ImmutableList<>(ArrayUtils.join(ArrayUtils.subArray(args, 1), " "));
		}

		return Collections.emptyList();
	}
}
