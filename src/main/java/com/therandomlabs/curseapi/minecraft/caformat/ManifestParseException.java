package com.therandomlabs.curseapi.minecraft.caformat;

import com.therandomlabs.curseapi.CurseException;

public class ManifestParseException extends CurseException {
	private static final long serialVersionUID = 4378171312681446251L;

	public ManifestParseException(String message) {
		super(message);
	}

	public ManifestParseException(String message, Object... args) {
		super(String.format(message, args));
	}
}
