package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.project.CurseProject;

public interface ModSpecificChangelogHandler {
	boolean handlesMod(CurseProject project) throws CurseException;

	default boolean isFullChangelogInNewFile(CurseProject project) {
		return false;
	}

	default List<String> getURLsToPreload(CurseFile oldFile, CurseFile newFile)
			throws CurseException {
		return Collections.emptyList();
	}

	default void filterFileList(CurseFileList files, CurseFile oldFile, CurseFile newFile)
			throws CurseException {}

	default Map<String, String> getChangelogs(CurseFile oldFile, CurseFile newFile, boolean urls)
			throws CurseException, IOException {
		return null;
	}

	default String getChangelog(CurseFile file) throws CurseException {
		return null;
	}

	default String modifyChangelog(CurseFile oldFile, CurseFile newFile, String changelog)
			throws CurseException {
		return changelog;
	}
}
