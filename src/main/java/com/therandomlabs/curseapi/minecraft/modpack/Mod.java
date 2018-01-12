package com.therandomlabs.curseapi.minecraft.modpack;

import com.therandomlabs.curseapi.util.CloneException;

public class Mod implements Cloneable {
	public static final String UNKNOWN_NAME = "Unknown Name";

	public String title = UNKNOWN_NAME;
	public int projectID;
	public int fileID;
	public Side side = Side.BOTH;
	public boolean optional;
	public FileInfo[] relatedFiles = new FileInfo[0];
	public String group = "";

	@Override
	public Mod clone() {
		final Mod mod = new Mod();

		mod.title = title;
		mod.projectID = projectID;
		mod.fileID = fileID;
		mod.side = side;
		mod.optional = optional;
		mod.relatedFiles = CloneException.tryClone(relatedFiles);
		mod.group = group;

		return mod;
	}

	public String[] getRelatedFiles(Side side) {
		return FileInfo.getPaths(relatedFiles, side);
	}
}
