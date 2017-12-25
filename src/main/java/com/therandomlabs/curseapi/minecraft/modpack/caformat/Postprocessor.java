package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.List;
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

	private static final List<Postprocessor> postprocessors = new TRLList<>();

	protected Postprocessor() {
		postprocessors.add(this);
	}

	@Override
	public abstract String toString();

	public abstract boolean isValid(String value);

	public abstract List<String> apply(CAManifest manifest, String value);

	public static Postprocessor fromName(String name) {
		for(Postprocessor postprocessor : postprocessors) {
			if(postprocessor.toString().equalsIgnoreCase(name)) {
				return postprocessor;
			}
		}

		return null;
	}
}
