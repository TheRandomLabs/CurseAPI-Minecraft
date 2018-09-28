package com.therandomlabs.curseapi.minecraft.comparison;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.cursemeta.CurseMeta;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.ModList;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.mpmanifest.Mod;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.Assertions;

public final class ModListComparer {
	public static final String NO_CHANGELOG_PROVIDED = "No changelog provided.";
	public static final String VIEW_CHANGELOG_AT = "View changelog at";

	private static final Set<ModSpecificChangelogHandler> handlers = new HashSet<>();

	private ModListComparer() {}

	public static ModListComparison compare(ModList oldList, ModList newList)
			throws CurseException {
		final TRLList<Mod> unchanged = new TRLList<>();
		final TRLList<VersionChange> updated = new TRLList<>();
		final TRLList<VersionChange> downgraded = new TRLList<>();
		final TRLList<Mod> removed = new TRLList<>();
		final TRLList<Mod> added = new TRLList<>();

		final MCVersion mcVersion = newList.getMCVersion();

		for(Mod oldMod : oldList) {
			boolean found = false;

			for(Mod newMod : newList) {
				if(oldMod.projectID == newMod.projectID) {
					found = true;

					if(oldMod.fileID == newMod.fileID) {
						unchanged.add(newMod);
						break;
					}

					final VersionChange vc = new VersionChange(mcVersion, oldMod, newMod);

					if(newMod.fileID > oldMod.fileID) {
						updated.add(vc);
						break;
					}

					downgraded.add(vc);
					break;
				}
			}

			if(!found) {
				removed.add(oldMod);
			}
		}

		for(Mod newMod : newList) {
			boolean found = false;

			for(Mod oldMod : oldList) {
				if(oldMod.projectID == newMod.projectID) {
					found = true;
					break;
				}
			}

			if(!found) {
				added.add(newMod);
			}
		}

		final String oldModLoaderVersion = oldList.getModLoaderVersion();
		final String newModLoaderVersion = newList.getModLoaderVersion();

		if(newModLoaderVersion.startsWith("forge-")) {
			final int compare;

			try {
				compare = MinecraftForge.compare(oldModLoaderVersion, newModLoaderVersion);
			} catch(IOException ex) {
				throw CurseException.fromThrowable(ex);
			}

			if(compare < 0) {
				updated.add(new ForgeVersionChange(
						mcVersion, oldModLoaderVersion, newModLoaderVersion, false
				));
			} else if(compare > 0) {
				downgraded.add(new ForgeVersionChange(
						mcVersion, newModLoaderVersion, oldModLoaderVersion, true
				));
			}
		} else {
			final int compare = oldModLoaderVersion.compareTo(newModLoaderVersion);

			if(compare < 0) {
				updated.add(new ModLoaderVersionChange(
						mcVersion, oldModLoaderVersion, newModLoaderVersion, false
				));
			} else if(compare > 0) {
				downgraded.add(new ModLoaderVersionChange(
						mcVersion, newModLoaderVersion, oldModLoaderVersion, true
				));
			}
		}

		return new ModListComparison(
				oldList, newList, unchanged, updated, downgraded, removed, added
		);
	}

	public static URL getChangelogURL(CurseFile file, boolean preferCurseForge)
			throws CurseException {
		final URL url = file.url();

		if(preferCurseForge && url != null) {
			return url;
		}

		if(CurseAPI.isCurseMetaEnabled() || url == null) {
			return CurseMeta.getChangelogURL(file.projectID(), file.id());
		}

		return url;
	}

	public static String getChangelogURLString(CurseFile file, boolean preferCurseForge)
			throws CurseException {
		return getChangelogURL(file, preferCurseForge).toString();
	}

	public static void registerChangelogHandler(ModSpecificChangelogHandler handler) {
		Assertions.nonNull(handler, "handler");
		handlers.add(handler);
	}

	public static void unregisterChangelogHandler(ModSpecificChangelogHandler handler) {
		Assertions.nonNull(handler, "handler");
		handlers.remove(handler);
	}

	public static Set<ModSpecificChangelogHandler> getChangelogHandlers(int projectID) {
		final Set<ModSpecificChangelogHandler> handlers = new HashSet<>(1);

		for(ModSpecificChangelogHandler handler : ModListComparer.handlers) {
			if(handler.getProjectID() == projectID) {
				handlers.add(handler);
			}
		}

		return handlers;
	}
}
