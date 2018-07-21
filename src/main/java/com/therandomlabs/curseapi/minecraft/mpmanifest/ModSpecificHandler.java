package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.project.CurseProject;

public interface ModSpecificHandler {
	boolean shouldPreloadOnlyNewFile(CurseProject project);

	List<String> getURLsToPreload(int projectID, CurseFile oldFile, CurseFile newFile)
			throws CurseException;

	void filterFileList(int projectID, CurseFileList files, CurseFile oldFile,
			CurseFile newFile) throws CurseException;

	Map<String, String> getChangelogs(int projectID, CurseFile oldFile, CurseFile newFile,
			boolean urls) throws CurseException, IOException;

	String getChangelog(CurseFile file) throws CurseException;

	String modifyChangelog(int projectID, CurseFile oldFile, CurseFile newFile,
			String changelog) throws CurseException;
}
