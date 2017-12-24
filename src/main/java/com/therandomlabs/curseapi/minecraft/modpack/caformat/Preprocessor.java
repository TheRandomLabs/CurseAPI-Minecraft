package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.io.IOException;
import java.util.List;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.utils.collection.TRLList;

public abstract class Preprocessor {
	public static final Preprocessor IMPORT = new PreprocessorImport();

	static final List<Preprocessor> preprocessors = new TRLList<>();

	protected Preprocessor() {
		preprocessors.add(this);
	}

	public abstract String name();

	public abstract boolean isValid(String value);

	public abstract void apply(TRLList<String> lines, int index, String value)
			throws CurseException, IOException;
}
