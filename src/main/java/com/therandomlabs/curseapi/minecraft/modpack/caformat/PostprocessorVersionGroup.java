package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.List;

public class PostprocessorVersionGroup extends Postprocessor {
	PostprocessorVersionGroup() {}

	@Override
	public String name() {
		return "=version";
	}

	@Override
	public List<String> apply(CAManifest manifest, String value) {
		//TODO
		return null;
	}
}
