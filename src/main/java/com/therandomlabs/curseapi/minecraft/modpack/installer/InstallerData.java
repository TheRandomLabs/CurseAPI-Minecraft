package com.therandomlabs.curseapi.minecraft.modpack.installer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.curseapi.util.CloneException;

public final class InstallerData implements Cloneable, Serializable {
	private static final long serialVersionUID = -2036469017004566167L;

	public String minecraftVersion;
	public String forgeVersion;
	public Set<ModData> mods = ConcurrentHashMap.newKeySet();
	public Set<String> installedFiles = ConcurrentHashMap.newKeySet();

	public static class ModData implements Cloneable, Serializable {
		private static final long serialVersionUID = -6045718265894030913L;

		public int projectID;
		public int fileID;
		public String location;
		public String[] relatedFiles;

		@Override
		public ModData clone() {
			try {
				return (ModData) super.clone();
			} catch(CloneNotSupportedException ignored) {}

			return null;
		}

		@Override
		public String toString() {
			return "[projectID=" + projectID + ",fileID=" + fileID + "]";
		}

		@Override
		public int hashCode() {
			return projectID + fileID;
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof ModData && object.hashCode() == hashCode();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstallerData clone() {
		try {
			final InstallerData data = (InstallerData) super.clone();

			data.mods = CloneException.tryClone(mods).toHashSet();
			data.installedFiles = new HashSet<>(installedFiles.size());
			data.installedFiles.addAll(installedFiles); //TODO TRLUtils.clone(Set<E>)

			return data;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	@Override
	public String toString() {
		return "[minecraftVersion=\"" + minecraftVersion + "\",forgeVersion=\"" + forgeVersion +
				"\",mods=" + mods.toString() + ",installedFiles=" + installedFiles.toString() + "]";
	}
}
