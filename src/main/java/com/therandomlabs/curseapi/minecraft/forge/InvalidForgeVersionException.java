package com.therandomlabs.curseapi.minecraft.forge;

public class InvalidForgeVersionException extends RuntimeException {
	private static final long serialVersionUID = -629537773419950101L;

	public InvalidForgeVersionException(String version) {
		super("Invalid Forge version: " + version);
	}
}
