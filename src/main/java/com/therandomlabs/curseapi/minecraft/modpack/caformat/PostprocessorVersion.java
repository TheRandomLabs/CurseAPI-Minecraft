package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.List;
import java.util.function.IntPredicate;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;

public class PostprocessorVersion extends Postprocessor {
	private final String prefix;
	private final IntPredicate testCompareResult;

	PostprocessorVersion(String prefix, IntPredicate testCompareResult) {
		this.prefix = prefix;
		this.testCompareResult = testCompareResult;
	}

	@Override
	public String toString() {
		return prefix + "version";
	}

	@Override
	public boolean isValid(String value) {
		return MinecraftVersion.fromString(value) != null;
	}

	@Override
	public List<String> apply(CAManifest manifest, String value) {
		//TODO
		return null;
	}
}
