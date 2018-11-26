package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.collection.ImmutableList;

public class ModLoaderVersionChange extends VersionChange {
	public static final ModLoaderHandler UNKNOWN_MOD_LOADER_HANDLER = new ModLoaderHandler() {
		@Override
		public int compare(String oldVersion, String newVersion) {
			return oldVersion.compareTo(newVersion);
		}

		@Override
		public ModLoaderVersionChange getVersionChange(MCVersion version, String oldVersion,
				String newVersion, boolean isDowngrade) {
			return new ModLoaderVersionChange(version, oldVersion, newVersion, isDowngrade);
		}
	};

	private static final HashMap<String, ModLoaderHandler> MOD_LOADERS = new HashMap<>();

	private final String oldVersion;
	private final String newVersion;
	private final boolean isDowngrade;

	ModLoaderVersionChange(MCVersion mcVersion, String oldVersion, String newVersion,
			boolean isDowngrade) {
		super(mcVersion, null, null);

		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
		this.isDowngrade = isDowngrade;
	}

	@Override
	public int hashCode() {
		return oldVersion.hashCode() * newVersion.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}

		return object instanceof ModLoaderVersionChange && object.hashCode() == hashCode();
	}

	@Override
	public Mod getOldMod() {
		return null;
	}

	@Override
	public CurseFile getOldFile() {
		return null;
	}

	@Override
	public String getOldFileName() {
		return oldVersion;
	}

	@Override
	public Mod getNewMod() {
		return null;
	}

	@Override
	public CurseFile getNewFile() {
		return null;
	}

	@Override
	public String getNewFileName() {
		return newVersion;
	}

	@Override
	public boolean isDowngrade() {
		return isDowngrade;
	}

	@Override
	public String getModTitle() {
		return "Unknown Mod Loader";
	}

	@Override
	public CurseProject getProject() {
		return null;
	}

	@Override
	public Map<String, String> getChangelogs(boolean urls) throws CurseException {
		return Collections.singletonMap("Unsupported", "This mod loader is currently unsupported.");
	}

	@Override
	void loadChangelogFiles() throws CurseException {}

	@Override
	List<String> getURLsToPreload() {
		return ImmutableList.empty();
	}

	public static void registerModLoaderHandler(String name, ModLoaderHandler handler) {
		MOD_LOADERS.put(name, handler);
	}

	public static void unregisterModLoaderHandler(String name) {
		MOD_LOADERS.remove(name);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, ModLoaderHandler> getModLoaderHandlers() {
		return (Map<String, ModLoaderHandler>) MOD_LOADERS.clone();
	}

	public static ModLoaderHandler getModLoaderHandler(String name) {
		final ModLoaderHandler handler = MOD_LOADERS.get(name);
		return handler == null ? UNKNOWN_MOD_LOADER_HANDLER : handler;
	}
}
