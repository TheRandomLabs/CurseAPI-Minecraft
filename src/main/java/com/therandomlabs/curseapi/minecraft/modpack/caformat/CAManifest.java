package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.HashMap;
import java.util.Map;

public class CAManifest {
	public static class Mod {

	}

	private final HashMap<Variable, String> variables = new HashMap<>();
	private final HashMap<Preprocessor, String> preprocessors = new HashMap<>();
	private final HashMap<Postprocessor, String> postprocessors = new HashMap<>();

	@SuppressWarnings("unchecked")
	public Map<Variable, String> getVariables() {
		return (Map<Variable, String>) variables.clone();
	}

	@SuppressWarnings("unchecked")
	public Map<Preprocessor, String> getPreprocessors() {
		return (Map<Preprocessor, String>) preprocessors.clone();
	}

	@SuppressWarnings("unchecked")
	public Map<Postprocessor, String> getPostprocessors() {
		return (Map<Postprocessor, String>) postprocessors.clone();
	}
}
