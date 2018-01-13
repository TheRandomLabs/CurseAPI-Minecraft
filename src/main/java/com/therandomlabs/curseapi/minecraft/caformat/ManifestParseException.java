package com.therandomlabs.curseapi.minecraft.caformat;

public class ManifestParseException extends Exception {
	private static final long serialVersionUID = 4378171312681446251L;

	public ManifestParseException(String message) {
		super(message);
	}

	public ManifestParseException(String message, Object... args) {
		super(String.format(message, args));
	}
}
