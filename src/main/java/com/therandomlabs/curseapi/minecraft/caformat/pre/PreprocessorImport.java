package com.therandomlabs.curseapi.minecraft.caformat.pre;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.CurseAPIMinecraft;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.io.IOUtils;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.network.NetworkUtils;

public class PreprocessorImport extends Preprocessor {
	public static final char CHARACTER = '#';

	PreprocessorImport() {}

	@Override
	public String toString() {
		return "import";
	}

	@Override
	public boolean isValid(String value, String[] args) {
		return value.equalsIgnoreCase(CurseAPIMinecraft.LIGHTCHOCOLATE_ID) ||
				value.equalsIgnoreCase(CurseAPIMinecraft.DARKCHOCOLATE_ID) ||
				NetworkUtils.isValidURL(value) || IOUtils.isValidPath(value);
	}

	@Override
	public List<String> apply(String value, String[] args) throws CurseException, IOException {
		List<String> linesToImport;

		try {
			if(value.equalsIgnoreCase(CurseAPIMinecraft.LIGHTCHOCOLATE_ID)) {
				value = CurseAPIMinecraft.LIGHTCHOCOLATE_MANIFEST_TXT;
			} else if(value.equalsIgnoreCase(CurseAPIMinecraft.DARKCHOCOLATE_ID)) {
				value = CurseAPIMinecraft.DARKCHOCOLATE_MANIFEST_TXT;
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
