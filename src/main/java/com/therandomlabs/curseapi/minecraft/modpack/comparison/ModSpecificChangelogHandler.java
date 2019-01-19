package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.project.CurseProject;

public abstract class ModSpecificChangelogHandler {
	public abstract boolean handlesMod(CurseProject project) throws CurseException;

	public boolean isFullChangelogInNewFile(CurseProject project) {
		return false;
	}

	public List<String> getURLsToPreload(CurseFile oldFile, CurseFile newFile)
			throws CurseException {
		return Collections.emptyList();
	}

	public void filterFileList(CurseFileList files, CurseFile oldFile, CurseFile newFile)
			throws CurseException {}

	public Map<String, String> getChangelogs(Object cacheKey, CurseFile oldFile, CurseFile newFile,
			boolean urls) throws CurseException, IOException {
		return null;
	}

	public String getChangelog(CurseFile file) throws CurseException {
		return null;
	}

	public String modifyChangelog(CurseFile oldFile, CurseFile newFile, String changelog)
			throws CurseException {
		return changelog;
	}
}
