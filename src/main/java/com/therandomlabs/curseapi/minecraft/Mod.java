package com.therandomlabs.curseapi.minecraft;

import java.net.URL;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseProject;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public class Mod implements Cloneable, Comparable<Mod> {
	public static final String UNKNOWN_NAME = "Unknown Name";

	public String title = UNKNOWN_NAME;
	public int projectID;
	public int fileID;
	public Side side = Side.BOTH;
	public boolean required;
	public FileInfo[] relatedFiles = new FileInfo[0];
	public String[] groups = new String[0];
	public URL url;

	@Override
	public Mod clone() {
		final Mod mod = new Mod();

		mod.title = title;
		mod.projectID = projectID;
		mod.fileID = fileID;
		mod.side = side;
		mod.required = required;
		mod.relatedFiles = CloneException.tryClone(relatedFiles);
		mod.groups = groups.clone();
		mod.url = url;

		return mod;
	}

	@Override
	public int compareTo(Mod mod) {
		if(title.equals(UNKNOWN_NAME)) {
			try {
				title = CurseProject.fromID(projectID).title();
			} catch(CurseException ex) {
				ThrowableHandling.handle(ex);
			}
		}

		if(mod.title.equals(UNKNOWN_NAME)) {
			try {
				mod.title = CurseProject.fromID(mod.projectID).title();
			} catch(CurseException ex) {
				ThrowableHandling.handle(ex);
			}
		}

		return title.compareTo(mod.title);
	}

	public String title() throws CurseException {
		if(title.equals(UNKNOWN_NAME)) {
			title = CurseProject.fromID(projectID).title();
		}
		return title;
	}

	public String[] getRelatedFiles(Side side) {
		return FileInfo.getPaths(relatedFiles, side);
	}
}
