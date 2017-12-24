package com.therandomlabs.curseapi.minecraft;

import java.net.URL;
import com.therandomlabs.curseapi.CurseException;

@SuppressWarnings("unused")
public interface MCEventHandler {
	default void autosavedInstallerData() throws CurseException {}

	default void deleting(String fileName) throws CurseException {}

	default void copying(String fileName) throws CurseException {}

	default void downloadingFile(String fileName) throws CurseException {}

	default void extracting(String fileName) throws CurseException {}

	default void downloadingFromURL(URL url) throws CurseException {}

	default void downloadingMod(String modName, int count, int total) throws CurseException {}

	default void downloadedMod(String modName, String fileName, int count) throws CurseException {}

	default void installingForge(String forgeVersion) throws CurseException {}
}
