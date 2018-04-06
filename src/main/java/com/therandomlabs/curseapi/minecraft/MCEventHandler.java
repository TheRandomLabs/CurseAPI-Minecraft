package com.therandomlabs.curseapi.minecraft;

import java.net.URL;
import com.therandomlabs.curseapi.CurseException;

public interface MCEventHandler {
	default void noFilesFound(int projectID) throws CurseException {}

	default void deleting(String fileName) throws CurseException {}

	default void copying(String fileName) throws CurseException {}

	default void downloadingFile(String fileName) throws CurseException {}

	default void extracting(String fileName) throws CurseException {}

	default void downloadingFromURL(URL url) throws CurseException {}

	default void downloadingFile(String name, int count, int total) throws CurseException {}

	default void downloadedFile(String name, String fileName, int count) throws CurseException {}

	default void installingForge(String forgeVersion) throws CurseException {}
}
