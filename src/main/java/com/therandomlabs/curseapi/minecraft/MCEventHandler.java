package com.therandomlabs.curseapi.minecraft;

import java.net.URL;
import com.therandomlabs.curseapi.project.CurseProject;
import static com.therandomlabs.utils.logging.Logging.getLogger;

public interface MCEventHandler {
	default void noFilesFound(CurseProject project) {
		getLogger().warning(
				"No files with the specified attributes found for %s (%s)",
				project.title(),
				project.id()
		);
	}

	default void downloadingProjectData(int projectID) {
		getLogger().info("Downloading data for project with ID: " + projectID);
	}

	default void projectNotFound(int projectID) {
		getLogger().warning("Project with ID %s not found", projectID);
	}

	default void downloadedProjectData(CurseProject project) {
		getLogger().info("Downloaded data for %s (%s)", project.title(), project.id());
	}

	default void downloadingModFileData(CurseProject project) {
		getLogger().info("Downloading file data for %s (%s)", project.title(), project.id());
	}

	default void downloadedModFileData(CurseProject project) {
		getLogger().info("Downloaded file data for %s (%s)", project.title(), project.id());
	}

	default void downloadingChangelogData(URL url) {
		getLogger().info("Downloading changelog data from: " + url);
	}

	default void downloadedChangelogData(URL url) {
		getLogger().info("Downloaded changelog data from: " + url);
	}
}
