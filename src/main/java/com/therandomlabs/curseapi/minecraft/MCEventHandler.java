package com.therandomlabs.curseapi.minecraft;

import java.net.URL;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.project.CurseProject;
import static com.therandomlabs.utils.logging.Logging.getLogger;

public interface MCEventHandler {
	default void noFilesFound(CurseProject project) {
		getLogger().warning("No files with specified attributes found for mod with project ID: " +
				project.id() + " (" + project.title() + ")");
	}

	default void deleting(String fileName) {
		getLogger().info("Deleting: " + fileName);
	}

	default void moving(String fileName) {
		getLogger().info("Moving: " + fileName);
	}

	default void copying(String fileName) {
		getLogger().info("Copying: " + fileName);
	}

	default void downloadingFile(String fileName) {
		getLogger().info("Downloading: " + fileName);
	}

	default void extracting(String fileName) {
		getLogger().info("Extracting: " + fileName);
	}

	default void downloadingModData(int projectID) {
		getLogger().info("Downloading data for mod with project ID: " + projectID);
	}

	default void downloadedModData(CurseProject project) {
		getLogger().info("Downloaded data for mod with project ID: " + project.id() +
				" (" + project.title() + ")");
	}

	default void downloadingModFileData(CurseProject project) {
		getLogger().info("Downloading file data for mod with project ID: " + project.id() +
				" (" + project.title() + ")");
	}

	default void downloadedModFileData(CurseProject project) {
		getLogger().info("Downloaded file data for mod with project ID: " + project.id() +
				" (" + project.title() + ")");
	}

	default void downloadingChangelogData(URL url) {
		getLogger().info("Downloading changelog data from: " + url);
	}

	default void downloadedChangelogData(URL url) {
		getLogger().info("Downloaded changelog data from: " + url);
	}

	default void downloadingFromURL(URL url) {
		getLogger().info("Downloading: " + url);
	}

	default void downloadingFile(Mod mod, int count, int total) {
		if(mod.title.equals(CurseProject.UNKNOWN_TITLE)) {
			getLogger().info("Downloading file %s of %s: ", count, total, mod.projectID);
		} else {
			getLogger().info("Downloading file %s of %s: %s", count, total, mod.title);
		}

		getLogger().flush();
	}

	default void downloadedFile(String name, String fileName, int count) {
		if(name.equals(CurseProject.UNKNOWN_TITLE)) {
			getLogger().info("Downloaded mod %s: %s", name, fileName);
		} else {
			getLogger().info("Downloaded mod: " + fileName);
		}

		getLogger().flush();
	}

	default void installingForge(String forgeVersion) {
		getLogger().info("Installing Forge %s...", forgeVersion);
	}
}
