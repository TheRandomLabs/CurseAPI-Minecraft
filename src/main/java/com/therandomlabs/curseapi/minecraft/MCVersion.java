package com.therandomlabs.curseapi.minecraft;

import com.therandomlabs.curseapi.game.CurseGameVersion;

/**
 * Represents a version of Minecraft supported by CurseForge.
 */
public final class MCVersion extends CurseGameVersion<MCVersion> {
	private int index;
	private String versionString;

	/**
	 * Returns the value returned by {@link #versionString()}.
	 *
	 * @return the value returned by {@link #versionString()}}.
	 */
	@Override
	public String toString() {
		return versionString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int gameID() {
		return CurseAPIMinecraft.MINECRAFT_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String versionString() {
		return versionString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(MCVersion version) {
		return Integer.compare(version.index, index);
	}

	//This method is called by ForgeSVCMinecraftProvider.
	void setIndex(int index) {
		this.index = index;
	}
}
