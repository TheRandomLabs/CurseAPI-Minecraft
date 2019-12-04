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

	static final SortedSet<MCVersion> versions;

	static {
		try {
			final List<MCVersion> versionList =
					RetrofitUtils.execute(FORGESVC_MINECRAFT.getVersions());

			for (int i = 0; i < versionList.size(); i++) {
				versionList.get(i).setIndex(i);
			}

			versions = ImmutableSortedSet.copyOf(versionList);
		} catch (CurseException ex) {
			throw new IllegalStateException("Failed to retrieve Minecraft versions", ex);
		}
	}

	private ForgeSVCMinecraftProvider() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SortedSet<? extends CurseGameVersion<?>> gameVersions(int id) {
		return id == CurseAPIMinecraft.MINECRAFT_ID ? new TreeSet<>(versions) : null;
	}
}
