package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.List;
import java.util.function.IntPredicate;

public class PostprocessorVersion extends Postprocessor {
	private final String prefix;
	private final IntPredicate testCompareResult;

	PostprocessorVersion(String prefix, IntPredicate testCompareResult) {
		this.prefix = prefix;
		this.testCompareResult = testCompareResult;
	}

	@Override
	public String name() {
		return prefix + "version";
	}

	@Override
	public List<String> apply(CAManifest manifest, String value) {
		//TODO
		return null;
	}
}
