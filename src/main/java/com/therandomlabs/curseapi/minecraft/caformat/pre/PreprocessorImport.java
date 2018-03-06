package com.therandomlabs.curseapi.minecraft.caformat.pre;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.misc.StringUtils;

public class PreprocessorImport extends Preprocessor {
	public static final char CHARACTER = '#';

	public static final String LIGHTCHOCOLATE = "lightchocolate";
	public static final String LIGHTCHOCOLATE_URL =
			"https://raw.githubusercontent.com/TheRandomLabs/LightChocolate/master/manifest.txt";

	public static final String DARKCHOCOLATE = "darkchocolate";
	public static final String DARKCHOCOLATE_URL =
			"https://raw.githubusercontent.com/TheRandomLabs/LightChocolate/master/" +
					"darkchocolate.txt";

	PreprocessorImport() {}

	@Override
	public String toString() {
		return "import";
	}

	@Override
	public boolean isValid(String value, String[] args) {
		if(value.equalsIgnoreCase(LIGHTCHOCOLATE) || value.equalsIgnoreCase(DARKCHOCOLATE)) {
			return true;
		}

		try {
			new URL(value);
			return true;
		} catch(MalformedURLException ex) {}

		try {
			Paths.get(value);
			return true;
		} catch(InvalidPathException ex) {}

		return false;
	}

	@Override
	public List<String> apply(String value, String[] args) throws CurseException, IOException {
		List<String> linesToImport = null;

		try {
			if(value.equalsIgnoreCase(LIGHTCHOCOLATE)) {
				value = LIGHTCHOCOLATE_URL;
			} else if(value.equalsIgnoreCase(DARKCHOCOLATE)) {
				value = DARKCHOCOLATE_URL;
			}

			final URL url = new URL(value);

			final String toImport = DocumentUtils.read(url);

			linesToImport = new ImmutableList<>(StringUtils.splitNewline(toImport));
		} catch(MalformedURLException ex) {
			linesToImport = Files.readAllLines(Paths.get(value));
		}

		return linesToImport;
	}
}
