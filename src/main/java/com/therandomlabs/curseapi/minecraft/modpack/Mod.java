package com.therandomlabs.curseapi.minecraft.modpack;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseForgeSite;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.CurseAPIMinecraft;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
import com.therandomlabs.curseapi.project.ProjectType;
import com.therandomlabs.curseapi.util.Utils;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.throwable.ThrowableHandling;

public final class Mod implements Cloneable, Comparable<Mod> {
	public int projectID;
	public int fileID;
	public boolean required = true;

	public String title = CurseProject.UNKNOWN_TITLE;
	public String fileTitle = CurseProject.UNKNOWN_TITLE;

	public Side side = Side.BOTH;
	public String projectType = ProjectType.Minecraft.MODS.singularName();
	public List<Integer> dependents = new TRLList<>();
	public boolean disableIfNoDependents;
	public FileInfo[] relatedFilesOnDisk = new FileInfo[0];
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
		if(this == object) {
			return true;
		}

		return object instanceof Mod && object.hashCode() == hashCode();
	}

	@Override
	public Mod clone() {
		try {
			final Mod mod = (Mod) super.clone();

			mod.dependents = new TRLList<>(dependents);
			mod.relatedFilesOnDisk = Utils.tryClone(relatedFilesOnDisk);

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
		Assertions.nonNull(projectType, "projectType");
		Assertions.nonNull(dependents, "dependents");
		dependents.forEach(CurseAPI::validateProjectID);
		Assertions.nonNull(relatedFilesOnDisk, "relatedFilesOnDisk");
		Arrays.stream(relatedFilesOnDisk).forEach(FileInfo::validate);
	}

	public String title() throws CurseException {
		if(title.equals(CurseProject.UNKNOWN_TITLE)) {
			try {
				title = CurseProject.fromID(projectID, true).title();
			} catch(InvalidProjectIDException ignored) {}
		}

		return title;
	}

	public ProjectType projectType() {
		return ProjectType.get(CurseForgeSite.MINECRAFT, projectType);
	}

	public String[] getRelatedFilesOnDisk(Side side) {
		return FileInfo.getPaths(relatedFilesOnDisk, side, false);
	}

	public boolean hasExtendedData() {
		return !CurseProject.UNKNOWN_TITLE.equals(title) &&
				!CurseProject.UNKNOWN_TITLE.equals(fileTitle) && downloadURL != null;
	}

	public void ensureExtendedData() throws CurseException {
		if(!hasExtendedData()) {
			reloadData();
		}
	}

	public void reloadData() throws CurseException {
		final CurseProject project = CurseAPIMinecraft.downloadProjectData(projectID);

		if(project == CurseProject.NULL_PROJECT && hasExtendedData()) {
			return;
		}

		if(project.reload()) {
			title = project.title();
			projectType = project.type().singularName();

			final CurseFile file = project.fileWithID(fileID);

			fileTitle = file.name();
			downloadURL = file.downloadURL();
		}
	}

	public void sort() {
		dependents.sort(null);
		Arrays.sort(relatedFilesOnDisk);
	}

	public static Mod fromFile(CurseFile file) throws CurseException {
		return fromFile(file, false);
	}

	public static TRLList<Mod> fromFiles(Collection<CurseFile> files) throws CurseException {
		final TRLList<Mod> mods = new TRLList<>(files.size());

		for(CurseFile file : files) {
			mods.add(fromFile(file));
		}

		return mods;
	}

	public static Mod fromFileBasic(CurseFile file) {
		try {
			return fromFile(file, true);
		} catch(CurseException ignored) {}

		return null;
	}

	public static TRLList<Mod> fromFilesBasic(Collection<CurseFile> files) {
		final TRLList<Mod> mods = new TRLList<>(files.size());

		for(CurseFile file : files) {
			mods.add(fromFileBasic(file));
		}

		return mods;
	}

	private static Mod fromFile(CurseFile file, boolean basic) throws CurseException {
		final Mod mod = new Mod();

		mod.projectID = file.projectID();
		mod.fileID = file.id();
		mod.fileTitle = file.name();

		if(!basic) {
			mod.title = file.projectTitle();
			mod.projectType = file.project().type().singularName();
			mod.downloadURL = file.downloadURL();
		}

		return mod;
	}
}
