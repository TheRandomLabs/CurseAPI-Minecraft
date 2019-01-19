package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.io.NetUtils;

public abstract class ModSpecificChangelogHandler {
	protected String read(String url) throws IOException {
		return read(new URL(url));
	}

	protected String read(URL url) throws IOException {
		MCEventHandling.forEach(eventHandler -> eventHandler.downloadingChangelogData(url));

		final String string = NetUtils.read(url);

		MCEventHandling.forEach(eventHandler -> eventHandler.downloadedChangelogData(url));

		return string;
	}

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

	public Map<String, String> getChangelogs(CurseFile oldFile, CurseFile newFile, boolean urls)
			throws CurseException, IOException {
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
