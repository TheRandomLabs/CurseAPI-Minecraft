package com.therandomlabs.curseapi.minecraft.modpack;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.misc.ThreadUtils;

public class CurseMod implements Cloneable {
	public int projectID;
	public int fileID;
	public boolean required;

	public CurseMod() {}

	public CurseMod(int projectID, int fileID, boolean required) {
		this.projectID = projectID;
		this.fileID = fileID;
		this.required = required;
	}

	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}

		return object instanceof CurseMod && object.hashCode() == hashCode();
	}

	@Override
	public int hashCode() {
		return projectID + fileID;
	}

	@Override
	public String toString() {
		return "[projectID=" + projectID + ",fileID=" + fileID + ",required=" + required + "]";
	}

	@Override
	public CurseMod clone() {
		try {
			return (CurseMod) super.clone();
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	public void validate() {
		CurseAPI.validateProjectID(projectID);
		CurseAPI.validateFileID(fileID);
	}

	public Mod toExtendedMod() throws CurseException {
		final Mod mod = toExtendedModWithoutExtendedData();
		mod.reloadData();
		return mod;
	}

	public Mod toExtendedModWithoutExtendedData() {
		final Mod mod = new Mod();

		mod.projectID = projectID;
		mod.fileID = fileID;
		mod.required = required;

		return mod;
	}

	public static Mod[] toExtendedMods(CurseMod[] mods) throws CurseException {
		final Mod[] extendedMods = new Mod[mods.length];

		ThreadUtils.splitWorkload(
				CurseAPI.getMaximumThreads(),
				mods.length,
				index -> extendedMods[index] = mods[index].toExtendedMod()
		);

		return extendedMods;
	}

	public static Mod[] toExtendedModsWithoutExtendedData(CurseMod[] mods) {
		return ArrayUtils.map(new Mod[0], mods, CurseMod::toExtendedModWithoutExtendedData);
	}

	public static CurseMod fromExtendedMod(Mod mod) {
		return new CurseMod(mod.projectID, mod.fileID, mod.required);
	}

	public static CurseMod[] fromExtendedMods(Mod[] mods) {
		final CurseMod[] curseMods = new CurseMod[mods.length];

		for(int i = 0; i < mods.length; i++) {
			curseMods[i] = fromExtendedMod(mods[i]);
		}

		return curseMods;
	}
}
