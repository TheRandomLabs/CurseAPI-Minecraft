package com.therandomlabs.curseapi.minecraft.modpack;

import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.utils.misc.Assertions;

public final class Modpack implements Cloneable {
	private MPManifest manifest;
	private boolean optifineEnabled = true;
	private Side side = Side.CLIENT;

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

	@SuppressWarnings("unchecked")
	public <M extends MPManifest> M getManifest() {
		return (M) manifest;
	}

	public boolean optifineEnabled() {
		return optifineEnabled;
	}

	public Modpack optifineEnabled(boolean flag) {
		optifineEnabled = flag;
		return this;
	}

	public Side side() {
		return side;
	}

	public Modpack side(Side side) {
		Assertions.nonNull(side, "side");
		this.side = side;
		return this;
	}

	//TODO validate dependencies/dependents
}
