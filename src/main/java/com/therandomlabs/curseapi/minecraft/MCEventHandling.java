package com.therandomlabs.curseapi.minecraft;

import java.util.List;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.runnable.RunnableWithInput;

public class MCEventHandling {
	public static final MCEventHandler DEFAULT_EVENT_HANDLER = new MCEventHandler() {};

	private static final List<MCEventHandler> eventHandlers = new TRLList<>(5);

	static {
		register(DEFAULT_EVENT_HANDLER);
	}

	private MCEventHandling() {}

	public static void register(MCEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	public static void unregister(MCEventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}

	public static void forEach(RunnableWithInput<MCEventHandler> runnable) {
		eventHandlers.forEach(runnable::run);
	}
}
