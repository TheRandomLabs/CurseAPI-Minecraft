package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;

public class PostprocessorVersion extends Postprocessor {
	private final String name;
	@SuppressWarnings("unused")
	private final IntPredicate testCompareResult;

	PostprocessorVersion(String name, IntPredicate testCompareResult) {
		this.name = name;
		this.testCompareResult = testCompareResult;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isValid(String value, String[] args) {
		return MinecraftVersion.fromString(args[0]) != null;
	}

	@Override
	public List<String> apply(CAManifest manifest, String value, String[] args) {
		//TODO
		return Collections.emptyList();
	}
}
