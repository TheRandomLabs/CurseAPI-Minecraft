package com.therandomlabs.curseapi.minecraft.modpack.installer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import com.therandomlabs.curseapi.util.CloneException;

public final class InstallerData implements Cloneable {
	public static class ModData implements Cloneable {
		public int projectID;
		public int fileID;
		public String location;
		public String[] relatedFiles;

		@Override
		public ModData clone() {
			final ModData data = new ModData();

			data.projectID = projectID;
			data.fileID = fileID;
			data.location = location;
			data.relatedFiles = relatedFiles;

			return data;
		}
	}

	public String minecraftVersion;
	public String forgeVersion;
	public Set<ModData> mods = ConcurrentHashMap.newKeySet();
	public Set<String> installedFiles = ConcurrentHashMap.newKeySet();

	@SuppressWarnings("unchecked")
	@Override
	public InstallerData clone() {
		final InstallerData data = new InstallerData();

		data.minecraftVersion = minecraftVersion;
		data.forgeVersion = forgeVersion;
		data.mods = CloneException.tryDeepCloneKeySet(
				(KeySetView<InstallerData.ModData, Boolean>) mods);
		data.installedFiles = CloneException.tryCloneKeySet(
				(KeySetView<String, Boolean>) installedFiles);

		return data;
	}
}
