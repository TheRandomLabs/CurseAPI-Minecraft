package com.therandomlabs.curseapi.minecraft.modpack;

import java.util.Collection;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;

public class ModList extends TRLList<Mod> {
	private static final long serialVersionUID = -3618723187025236453L;

	private final MCVersion mcVersion;
	private final String modLoaderName;
	private final String modLoaderVersion;

	public ModList(int initialCapacity, MCVersion mcVersion, String modLoaderName,
			String modLoaderVersion) {
		super(initialCapacity);
		this.mcVersion = mcVersion;
		this.modLoaderName = modLoaderName;
		this.modLoaderVersion = modLoaderVersion;
	}

	public ModList(Mod[] mods, MCVersion mcVersion, String modLoaderName, String modLoaderVersion) {
		super(mods);
		this.mcVersion = mcVersion;
		this.modLoaderName = modLoaderName;
		this.modLoaderVersion = modLoaderVersion;
	}

	public ModList(Collection<Mod> mods, MCVersion mcVersion, String modLoaderName,
			String modLoaderVersion) {
		super(mods);
		this.mcVersion = mcVersion;
		this.modLoaderName = modLoaderName;
		this.modLoaderVersion = modLoaderVersion;
	}

	@Override
	public boolean equals(Object object) {
		if(!super.equals(object)) {
			return false;
		}

		final ModList modList = (ModList) object;
		return mcVersion == modList.mcVersion && modLoaderName.equals(modList.modLoaderName) &&
				modLoaderVersion.equals(modList.modLoaderVersion);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * mcVersion.hashCode() * modLoaderName.hashCode() *
				modLoaderVersion.hashCode();
	}

	@Override
	public ModList clone() {
		return new ModList(Utils.tryClone(this), mcVersion, modLoaderName, modLoaderVersion);
	}

	public MCVersion getMCVersion() {
		return mcVersion;
	}

	public String getModLoaderName() {
		return modLoaderName;
	}

	public String getModLoaderVersion() {
		return modLoaderVersion;
	}

	public boolean contains(int projectID, int fileID) {
		return get(projectID, fileID) != null;
	}

	public Mod get(int projectID, int fileID) {
		for(Mod mod : this) {
			if(mod.projectID == projectID && mod.fileID == fileID) {
				return mod;
			}
		}

		return null;
	}

	public boolean containsProjectID(int id) {
		return getByProjectID(id) != null;
	}

	public Mod getByProjectID(int id) {
		for(Mod mod : this) {
			if(mod.projectID == id) {
				return mod;
			}
		}

		return null;
	}

	public boolean containsFileID(int id) {
		return getByFileID(id) != null;
	}

	public Mod getByFileID(int id) {
		for(Mod mod : this) {
			if(mod.fileID == id) {
				return mod;
			}
		}

		return null;
	}

	public static ModList fromCurseFiles(CurseFile[] files, MCVersion mcVersion,
			String modLoaderName, String modLoaderVersion) throws CurseException {
		return fromCurseFiles(
				new ImmutableList<>(files), mcVersion, modLoaderName, modLoaderVersion
		);
	}

	public static ModList fromCurseFiles(Collection<CurseFile> files, MCVersion version,
			String modLoaderName, String modLoaderVersion) throws CurseException {
		return new ModList(Mod.fromFiles(files), version, modLoaderName, modLoaderVersion);
	}

	public static ModList fromCurseFilesBasic(CurseFile[] files, MCVersion mcVersion,
			String modLoaderName, String modLoaderVersion) {
		return fromCurseFilesBasic(
				new ImmutableList<>(files), mcVersion, modLoaderName, modLoaderVersion
		);
	}

	public static ModList fromCurseFilesBasic(Collection<CurseFile> files, MCVersion version,
			String modLoaderName, String modLoaderVersion) {
		return new ModList(Mod.fromFilesBasic(files), version, modLoaderName, modLoaderVersion);
	}

	public static ModList empty() {
		return new ModList(new Mod[0], MCVersion.UNKNOWN, "Unknown", "");
	}
}
