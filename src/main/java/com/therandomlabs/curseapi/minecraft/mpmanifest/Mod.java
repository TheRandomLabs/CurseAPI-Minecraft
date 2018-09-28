package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
import com.therandomlabs.curseapi.project.ProjectType;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class Mod implements Cloneable, Comparable<Mod> {
	public String title = CurseProject.UNKNOWN_TITLE;
	public int projectID;
	public int fileID;
	public Side side = Side.BOTH;
	public boolean required;
	public boolean isResourcePack;
	public boolean disabledByDefault;
	public List<Integer> dependents = new TRLList<>();
	public FileInfo[] relatedFiles = new FileInfo[0];
	public URL downloadURL;

	@Override
	public int compareTo(Mod mod) {
		try {
			title();
			mod.title();
		} catch(CurseException ex) {
			ThrowableHandling.handle(ex);
		}

		return title.toLowerCase(Locale.ENGLISH).compareTo(mod.title.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public int hashCode() {
		return projectID + fileID;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Mod && object.hashCode() == hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Mod clone() {
		try {
			final Mod mod = (Mod) super.clone();

			if(dependents instanceof ArrayList) {
				mod.dependents = (ArrayList<Integer>) ((ArrayList<Integer>) dependents).clone();
			} else {
				mod.dependents = new TRLList<>(dependents);
			}

			mod.relatedFiles = Utils.tryClone(relatedFiles);

			return mod;
		} catch(CloneNotSupportedException ignored) {}

		return null;
	}

	@Override
	public String toString() {
		return "[projectID=" + projectID + ",fileID=" + fileID + ",title=\"" + title + "\"]";
	}

	public void validate() {
		Assertions.nonEmpty(title, "title");
		CurseAPI.validateProjectID(projectID);
		CurseAPI.validateFileID(fileID);
		Assertions.nonNull(side, "side");
		Assertions.nonNull(dependents, "dependents");
		dependents.forEach(CurseAPI::validateProjectID);
		Assertions.nonNull(relatedFiles, "relatedFiles");
		Arrays.stream(relatedFiles).forEach(FileInfo::validate);
	}

	public String title() throws CurseException {
		if(title.equals(CurseProject.UNKNOWN_TITLE)) {
			try {
				title = CurseProject.fromID(projectID, true).title();
			} catch(InvalidProjectIDException ignored) {}
		}

		return title;
	}

	public String[] getRelatedFiles(Side side) {
		return FileInfo.getPaths(relatedFiles, side, false);
	}

	public static Mod fromFile(CurseFile file) throws CurseException {
		return fromFile(file, false);
	}

	public static Mod fromFileBasic(CurseFile file) {
		try {
			return fromFile(file, true);
		} catch(CurseException ignored) {}

		return null;
	}

	private static Mod fromFile(CurseFile file, boolean basic) throws CurseException {
		final Mod mod = new Mod();

		mod.projectID = file.projectID();
		mod.fileID = file.id();

		if(!basic) {
			mod.isResourcePack = file.project().type() == ProjectType.Minecraft.TEXTURE_PACKS;
			mod.downloadURL = file.downloadURL();
		}

		return mod;
	}
}
