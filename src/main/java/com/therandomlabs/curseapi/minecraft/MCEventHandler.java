package com.therandomlabs.curseapi.minecraft;

import java.net.URL;
import static com.therandomlabs.utils.logging.Logging.getLogger;

public interface MCEventHandler {
	default void noFilesFound(int projectID) {
		getLogger().warning("No files with specified attributes found for project with ID: " +
				projectID);
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

	default void downloadingFromURL(URL url) {
		getLogger().info("Downloading: " + url);
	}

	default void downloadingFile(String name, int count, int total) {
		if(name.equals(Mod.UNKNOWN_NAME)) {
			getLogger().info("Downloading file %s of %s...", count, total, name);
		} else {
			getLogger().info("Downloading file %s of %s: %s", count, total, name);
		}

		getLogger().flush();
	}

	default void downloadedFile(String name, String fileName, int count) {
		if(name.equals(Mod.UNKNOWN_NAME)) {
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
