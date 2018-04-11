package com.therandomlabs.curseapi.minecraft;

import java.net.URL;

public interface MCEventHandler {
	default void noFilesFound(int projectID) {}

	default void deleting(String fileName) {}

	default void copying(String fileName) {}

	default void downloadingFile(String fileName) {}

	default void extracting(String fileName) {}

	default void downloadingFromURL(URL url) {}

	default void downloadingFile(String name, int count, int total) {}

	default void downloadedFile(String name, String fileName, int count) {}

	default void installingForge(String forgeVersion) {}
}
