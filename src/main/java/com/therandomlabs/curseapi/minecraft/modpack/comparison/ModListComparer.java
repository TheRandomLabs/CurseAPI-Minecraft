package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.cursemeta.CurseMeta;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.minecraft.modpack.ModList;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.Assertions;

public final class ModListComparer {
	public static final String NO_CHANGELOG_PROVIDED = "No changelog provided.";
	public static final String VIEW_CHANGELOG_AT = "View changelog at";

	private static final Set<ModSpecificChangelogHandler> handlers = new HashSet<>();

	private ModListComparer() {}

	public static ModListComparison compare(ModList oldList, ModList newList) {
		oldList = oldList.clone();
		newList = newList.clone();

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

		final ModLoaderHandler handler =
				ModLoaderVersionChange.getModLoaderHandler(newList.getModLoaderName());
		final int compare = handler.compare(oldModLoaderVersion, newModLoaderVersion);

		if(compare < 0) {
			updated.add(handler.getVersionChange(
					mcVersion, oldModLoaderVersion, newModLoaderVersion, true
			));
		} else {
			downgraded.add(handler.getVersionChange(
					mcVersion, oldModLoaderVersion, newModLoaderVersion, false
			));
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

	public static Set<ModSpecificChangelogHandler> getChangelogHandlers(CurseProject project,
			int projectID) throws CurseException {
		if(project == null) {
			project = CurseProject.nullProject(projectID);
		}

		final Set<ModSpecificChangelogHandler> handlers = new HashSet<>(1);

		for(ModSpecificChangelogHandler handler : ModListComparer.handlers) {
			if(handler.handlesMod(project)) {
				handlers.add(handler);
			}
		}

		return handlers;
	}
}
