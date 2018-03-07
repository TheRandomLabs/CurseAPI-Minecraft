package com.therandomlabs.curseapi.minecraft.caformat.pre;

import java.io.IOException;
import java.util.List;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.utils.collection.TRLList;

public abstract class Preprocessor {
	public static final char CHARACTER = '#';

	private static final List<Preprocessor> preprocessors = new TRLList<>();

	public static final Preprocessor IMPORT = new PreprocessorImport();

	protected Preprocessor() {
		preprocessors.add(this);
	}

	public static Preprocessor fromName(String name) {
		for(Preprocessor preprocessor : preprocessors) {
			if(preprocessor.toString().equalsIgnoreCase(name)) {
				return preprocessor;
			}
		}

		return null;
	}

	@Override
	public abstract String toString();

	public abstract boolean isValid(String value, String[] args);

	public abstract List<String> apply(String value, String[] args)
			throws CurseException, IOException;
}
