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

	static final List<Postprocessor> postprocessors = new TRLList<>();

	protected Postprocessor() {
		postprocessors.add(this);
	}

	public abstract String name();

	public abstract List<String> apply(CAManifest manifest, String value);
}
