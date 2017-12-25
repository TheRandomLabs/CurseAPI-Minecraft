package com.therandomlabs.curseapi.minecraft;

import static com.therandomlabs.utils.logging.Logging.getLogger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.utils.runnable.RunnableWithInputAndThrowable;
import com.therandomlabs.utils.wrapper.Wrapper;

public class MCEventHandling {
	public static final MCEventHandler DEFAULT_EVENT_HANDLER = new DefaultMCEventHandler();
	private static final List<MCEventHandler> eventHandlers = new ArrayList<>(5);

	public static class DefaultMCEventHandler implements MCEventHandler {
		@Override
		public void autosavedInstallerData() {
			getLogger().debug("Autosaved installer data.");
		}

		@Override
		public void deleting(String fileName) {
			getLogger().info("Deleting: " + fileName);
		}

		@Override
		public void copying(String fileName) {
			getLogger().info("Copying: " + fileName);
		}

		@Override
		public void downloadingFile(String fileName) {
			getLogger().info("Downloading: " + fileName);
		}

		@Override
		public void extracting(String fileName) {
			getLogger().info("Extracting: " + fileName);
		}

		@Override
		public void downloadingFromURL(URL url) {
			getLogger().info("Downloading: " + url);
		}

		@Override
		public void downloadingMod(String modName, int count, int total) {
			if(modName == Mod.UNKNOWN_NAME) {
				getLogger().info("Downloading mod %s of %s...", count, total, modName);
			} else {
				getLogger().info("Downloading mod %s of %s: %s", count, total, modName);
			}

			getLogger().flush();
		}

		@Override
		public void downloadedMod(String modName, String fileName, int count) {
			if(modName == Mod.UNKNOWN_NAME) {
				getLogger().info("Downloaded mod %s: %s", modName, fileName);
			} else {
				getLogger().info("Downloaded mod: " + fileName);
			}

			getLogger().flush();
		}

		@Override
		public void installingForge(String forgeVersion) {
			getLogger().info("Installing Forge %s...", forgeVersion);
		}
	}

	private MCEventHandling() {}

	static {
		register(DEFAULT_EVENT_HANDLER);
	}

	public static void register(MCEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	public static void unregister(MCEventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}

	public static void forEach(
			RunnableWithInputAndThrowable<MCEventHandler, CurseException> consumer)
			throws CurseException {
		final Wrapper<CurseException> exception = new Wrapper<>();

		for(MCEventHandler eventHandler : eventHandlers) {
			try {
				consumer.run(eventHandler);
			} catch(CurseException ex) {
				exception.set(ex);
			}
		}

		if(exception.hasValue()) {
			throw exception.get();
		}
	}
}
