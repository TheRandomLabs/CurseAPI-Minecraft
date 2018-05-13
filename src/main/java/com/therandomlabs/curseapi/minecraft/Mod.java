package com.therandomlabs.curseapi.minecraft;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class Mod implements Cloneable, Comparable<Mod>, Serializable {
	private static final long serialVersionUID = -3120215335212824363L;

	public static final String UNKNOWN_NAME = "Unknown Name";

	public String title = UNKNOWN_NAME;
	public int projectID;
	public int fileID;
	public Side side = Side.BOTH;
	public boolean required;
	public boolean isResourcePack;
	public boolean disabledByDefault;
	public int[] dependents = new int[0];
	public FileInfo[] relatedFiles = new FileInfo[0];
	public URL url;

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

		return title.toLowerCase(Locale.ENGLISH).compareTo(mod.title.toLowerCase(Locale.ENGLISH));
	}

	public void validate() {
		Assertions.nonEmpty(title, "title");
		CurseAPI.validateID(projectID, fileID);
		Assertions.nonNull(side, "side");
		Assertions.nonNull(dependents, "dependents");
		Arrays.stream(dependents).forEach(CurseAPI::validateID);
		Assertions.nonNull(relatedFiles, "relatedFiles");
		Arrays.stream(relatedFiles).forEach(FileInfo::validate);
	}

	public String title() throws CurseException {
		if(title.equals(UNKNOWN_NAME)) {
			title = CurseProject.fromID(projectID).title();
		}
		return title;
	}

	public String[] getRelatedFiles(Side side) {
		return FileInfo.getPaths(relatedFiles, side, false);
	}

	@Override
	public int hashCode() {
		return projectID + fileID;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Mod && object.hashCode() == hashCode();
	}

	@Override
	public Mod clone() {
		try {
			final Mod mod = (Mod) super.clone();
			mod.relatedFiles = CloneException.tryClone(relatedFiles);
			return mod;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	@Override
	public String toString() {
		return "[projectID=" + projectID + ",fileID=" + fileID + ",title=\"" + title + "\"]";
	}
}
