package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.util.CurseEventHandling;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.network.NetworkUtils;

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
	public void apply(TRLList<String> lines, int index, String value, String[] args)
			throws CurseException, IOException {
		List<String> linesToImport = null;

		try {
			final URL url = new URL(value);

			CurseEventHandling.forEach(handler -> handler.preDownloadDocument(value));

			final String toImport = NetworkUtils.read(url);

			CurseEventHandling.forEach(handler -> handler.postDownloadDocument(value));

			linesToImport = new ImmutableList<>(StringUtils.splitNewline(toImport));
		} catch(MalformedURLException ex) {
			linesToImport = Files.readAllLines(Paths.get(value));
		}

		if(linesToImport == null) {
			return;
		}

		lines.addAll(index + 1, CAManifest.prune(linesToImport));
	}
}
