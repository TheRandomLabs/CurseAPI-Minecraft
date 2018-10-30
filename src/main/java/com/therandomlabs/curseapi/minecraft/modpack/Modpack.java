package com.therandomlabs.curseapi.minecraft.modpack;

public final class Modpack implements Cloneable {
	private MPManifest manifest;

	public Modpack(MPManifest manifest) {
		this.manifest = manifest;
	}

	@Override
	public Modpack clone() {
		try {
			final Modpack modpack = (Modpack) super.clone();
			modpack.manifest = manifest.clone();
			return modpack;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}
}
