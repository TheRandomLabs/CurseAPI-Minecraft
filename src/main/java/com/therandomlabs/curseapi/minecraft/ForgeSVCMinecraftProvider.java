package com.therandomlabs.curseapi.minecraft;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.therandomlabs.curseapi.CurseAPIProvider;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.forgesvc.ForgeSVCProvider;
import com.therandomlabs.curseapi.game.CurseGameVersion;
import com.therandomlabs.curseapi.util.RetrofitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CurseAPIProvider} that uses the API at {@code https://addons-ecs.forgesvc.net/}
 * used by the Twitch launcher to provide {@link CurseGameVersion}s for Minecraft.
 */
public final class ForgeSVCMinecraftProvider implements CurseAPIProvider {
	/**
	 * The singleton instance of {@link ForgeSVCProvider}.
	 */
	public static final ForgeSVCMinecraftProvider INSTANCE = new ForgeSVCMinecraftProvider();

	static final ForgeSVCMinecraft FORGESVC_MINECRAFT =
			RetrofitUtils.get("https://addons-ecs.forgesvc.net/").create(ForgeSVCMinecraft.class);

	static final SortedSet<MCVersion> versions = getVersions();
	static boolean failedToRetrieveVersions;

	private ForgeSVCMinecraftProvider() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SortedSet<? extends CurseGameVersion<?>> gameVersions(int id) {
		MCVersions.initialize();
		return id == CurseAPIMinecraft.MINECRAFT_ID ? new TreeSet<>(versions) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseGameVersion<?> gameVersion(int gameID, String versionString) {
		MCVersions.initialize();
		return gameID == CurseAPIMinecraft.MINECRAFT_ID ? MCVersions.get(versionString) : null;
	}

	@SuppressWarnings("PMD.ForLoopCanBeForeach")
	private static SortedSet<MCVersion> getVersions() {
		final Logger logger = LoggerFactory.getLogger(ForgeSVCMinecraftProvider.class);

		try {
			final List<MCVersion> versions =
					RetrofitUtils.execute(FORGESVC_MINECRAFT.getVersions());

			for (int i = 0; i < versions.size(); i++) {
				final MCVersion version = versions.get(i);
				//Initialize MCVersion#versionGroup so that the MCVersion adds itself to the
				//version group.
				version.versionGroup();
				//We multiply the index by 2 so that snapshots defined in MCVersions can fit in.
				version.setSortIndex((versions.size() - i - 1) * 2);
			}

			return new TreeSet<>(versions);
		} catch (CurseException ex) {
			logger.error(
					"Failed to retrieve Minecraft versions; a local copy will be used instead", ex
			);
		}

		failedToRetrieveVersions = true;
		//When MCVersions is initialized, it sees that failedToRetrieveVersions is true
		//and adds local MCVersion instances to this TreeSet.
		return new TreeSet<>();
	}
}
