package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.utils.collection.TRLList;

public class CAManifest {
	// [ Primary Mod Group ] [ Some Other Mod That Does Something Similar ]
	public static final char GROUP_DEFINER_OPENER = '[';
	public static final char GROUP_DEFINER_CLOSER = ']';
	public static final char GROUP_MARKER = ';';

	private final Map<Variable, String> variables = new HashMap<>();
	private final Map<Preprocessor, String> preprocessors = new HashMap<>();
	private final Map<Postprocessor, String> postprocessors = new HashMap<>();
	private final TRLList<Mod> mods = new TRLList<>();

	public Map<Variable, String> getVariables() {
		return variables;
	}

	public Map<Preprocessor, String> getPreprocessors() {
		return preprocessors;
	}

	public Map<Postprocessor, String> getPostprocessors() {
		return postprocessors;
	}

	public TRLList<Mod> getMods() {
		return mods;
	}
}
