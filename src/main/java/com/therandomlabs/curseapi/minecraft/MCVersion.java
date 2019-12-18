package com.therandomlabs.curseapi.minecraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.therandomlabs.curseapi.game.CurseGameVersion;
import com.therandomlabs.curseapi.game.CurseGameVersionGroup;

/**
 * Represents a version of Minecraft supported by CurseForge.
 */
public final class MCVersion extends CurseGameVersion<MCVersion> {
	private static class MCVersionGroup extends CurseGameVersionGroup<MCVersion> {
		private static final Map<String, MCVersionGroup> versionGroups = new HashMap<>();

		private String versionString;
		private Set<MCVersion> versions = new HashSet<>();

		private MCVersionGroup(String versionString) {
			this.versionString = versionString;
		}

		@Override
		public int gameID() {
			return CurseAPIMinecraft.MINECRAFT_ID;
		}

		@Override
		public String versionString() {
			return versionString;
		}

		@Override
		public Set<MCVersion> versions() {
			return new TreeSet<>(versions);
		}

		static MCVersionGroup get(MCVersion version) {
			final String[] versionElements = version.versionString().split("\\.");
			final String versionString = versionElements[0] + "." + versionElements[1];
			final MCVersionGroup versionGroup =
					versionGroups.computeIfAbsent(versionString, MCVersionGroup::new);
			versionGroup.versions.add(version);
			return versionGroup;
		}
	}

	private int index;
	private String versionString;

	private transient MCVersionGroup versionGroup;

	MCVersion(int index, String versionString) {
		this.index = index;
		this.versionString = versionString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseGameVersionGroup<MCVersion> versionGroup() {
		if (versionGroup == null) {
			versionGroup = MCVersionGroup.get(this);
		}

		return versionGroup;
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
		return Integer.compare(index, version.index);
	}

	//This method is called by ForgeSVCMinecraftProvider.
	void setIndex(int index) {
		this.index = index;
	}
}
