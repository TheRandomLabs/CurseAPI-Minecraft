package com.therandomlabs.curseapi.minecraft;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSortedSet;
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
		if (failedToRetrieveVersions && versions.isEmpty()) {
			//This initializes the MCVersion constants, which create local MCVersion instances
			//if failedToRetrieveVersions is true.
			MCVersions.getAll();
		}

		return id == CurseAPIMinecraft.MINECRAFT_ID ? new TreeSet<>(versions) : null;
	}

	@SuppressWarnings("PMD.ForLoopCanBeForeach")
	private static SortedSet<MCVersion> getVersions() {
		final Logger logger = LoggerFactory.getLogger(ForgeSVCMinecraftProvider.class);

		try {
			final List<MCVersion> versions =
					RetrofitUtils.execute(FORGESVC_MINECRAFT.getVersions());

			for (int i = 0; i < versions.size(); i++) {
				final MCVersion version = versions.get(i);
				version.setIndex(versions.size() - i - 1);
				//Initialize MCVersionGroup instance.
				version.versionGroup();
			}

			return ImmutableSortedSet.copyOf(versions);
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
