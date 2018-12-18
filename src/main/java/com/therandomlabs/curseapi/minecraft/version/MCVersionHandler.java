package com.therandomlabs.curseapi.minecraft.version;

import com.therandomlabs.curseapi.game.Game;
import com.therandomlabs.curseapi.game.GameVersionHandler;
import com.therandomlabs.utils.collection.TRLList;

final class MCVersionHandler implements GameVersionHandler<MCVersion, MCVersionGroup> {
	static final MCVersionHandler INSTANCE = new MCVersionHandler();

	private MCVersionHandler() {}

	@Override
	public Game getGame() {
		return Game.MINECRAFT;
	}

	@Override
	public MCVersion getUnknownVersion() {
		return MCVersion.UNKNOWN;
	}

	@Override
	public TRLList<MCVersion> getVersions() {
		return new TRLList<>(MCVersion.values());
	}

	@Override
	public TRLList<MCVersionGroup> getGroups() {
		return new TRLList<>(MCVersionGroup.values());
	}
}
