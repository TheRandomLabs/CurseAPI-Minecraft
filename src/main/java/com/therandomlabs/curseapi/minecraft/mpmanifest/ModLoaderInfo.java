package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.Serializable;
import com.therandomlabs.utils.misc.Assertions;

public final class ModLoaderInfo implements Cloneable, Serializable {
	private static final long serialVersionUID = -5375584834183726145L;

	public String id;
	public boolean primary = true;

	public ModLoaderInfo() {}

	public ModLoaderInfo(String forgeVersion) {
		id = "forge-" + forgeVersion.split("-")[1];
	}

	@Override
	public ModLoaderInfo clone() {
		try {
			return (ModLoaderInfo) super.clone();
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	@Override
	public String toString() {
		return "[id=" + id + ",primary=" + primary + "]";
	}

	public void validate() {
		Assertions.nonEmpty(id, "id");
	}
}
