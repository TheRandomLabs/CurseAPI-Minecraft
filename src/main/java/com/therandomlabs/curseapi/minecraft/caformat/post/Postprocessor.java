package com.therandomlabs.curseapi.minecraft.caformat.post;

import java.util.List;
import com.therandomlabs.curseapi.minecraft.caformat.CAManifest;
import com.therandomlabs.curseapi.minecraft.caformat.VariableMap;
import com.therandomlabs.utils.collection.TRLList;

public abstract class Postprocessor {
	public static final char CHARACTER = '*';
	public static final Postprocessor NEWER_THAN =
			new PostprocessorVersion(">", result -> result > 0);
	public static final Postprocessor NEWER_THAN_OR_EQUAL_TO =
			new PostprocessorVersion(">=", result -> result >= 0);
	public static final Postprocessor EQUAL_TO =
			new PostprocessorVersion("==", result -> result == 0);
	public static final Postprocessor OLDER_THAN =
			new PostprocessorVersion("<", result -> result < 0);
	public static final Postprocessor OLDER_THAN_OR_EQUAL_TO =
			new PostprocessorVersion("<=", result -> result <= 0);
	public static final Postprocessor EQUAL_TO_VERSION_GROUP =
			new PostprocessorVersionGroup();

	private static final List<Postprocessor> postprocessors = new TRLList<>();

	protected Postprocessor() {
		postprocessors.add(this);
	}

	public static Postprocessor fromName(String name) {
		for(Postprocessor postprocessor : postprocessors) {
			if(postprocessor.toString().equalsIgnoreCase(name)) {
				return postprocessor;
			}
		}

		return null;
	}

	@Override
	public abstract String toString();

	public abstract boolean isValid(String value, String[] args);

	public abstract List<String> apply(VariableMap variables, List<CAManifest.ModData> mods,
			String value, String[] args);
}
