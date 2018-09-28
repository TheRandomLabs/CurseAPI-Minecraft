package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.cursemeta.CurseMeta;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.version.MCVersion;
import com.therandomlabs.utils.collection.TRLList;

public final class ManifestComparer {
	public static final String NO_CHANGELOG_PROVIDED = "No changelog provided.";
	public static final String VIEW_CHANGELOG_AT = "View changelog at";

	static final Set<ModSpecificHandler> handlers = new HashSet<>(2);

	private ManifestComparer() {}

	public static void registerModSpecificHandler(ModSpecificHandler handler) {
		handlers.add(handler);
	}

	public static void removeModSpecificHandler(ModSpecificHandler handler) {
		handlers.remove(handler);
	}

	public static CompareResults compare(ExtendedMPManifest oldManifest,
			ExtendedMPManifest newManifest) throws CurseException, IOException {
		oldManifest.enableAll();
		newManifest.enableAll();

		final TRLList<Mod> unchanged = new TRLList<>();
		final TRLList<VersionChange> updated = new TRLList<>();
		final TRLList<VersionChange> downgraded = new TRLList<>();
		final TRLList<Mod> removed = new TRLList<>();
		final TRLList<Mod> added = new TRLList<>();

		final MCVersion mcVersion = newManifest.minecraft.version;

		for(Mod oldMod : oldManifest.files) {
			boolean found = false;

			for(Mod newMod : newManifest.files) {
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

		for(Mod newMod : newManifest.files) {
			boolean found = false;

			for(Mod oldMod : oldManifest.files) {
				if(oldMod.projectID == newMod.projectID) {
					found = true;
					break;
				}
			}

			if(!found) {
				added.add(newMod);
			}
		}

		final String oldForge = oldManifest.minecraft.getForgeVersion();
		final String newForge = newManifest.minecraft.getForgeVersion();

		final int compare = MinecraftForge.compare(oldForge, newForge);

		if(compare < 0) {
			updated.add(new ForgeVersionChange(mcVersion, oldForge, newForge, false));
		} else if(compare > 0) {
			downgraded.add(new ForgeVersionChange(mcVersion, newForge, oldForge, true));
		}

		return new CompareResults(oldManifest, newManifest, unchanged, updated, downgraded,
				removed,
				added);
	}

	public static String getCurseForgeURL(CurseFile file) throws CurseException {
		final String url = file.urlString();
		return url == null ? CurseMeta.getChangelogURLString(file.projectID(), file.id()) : url;
	}

	static URL getChangelogURL(CurseFile file) throws CurseException {
		if(CurseAPI.isCurseMetaEnabled() || file.url() == null) {
			return CurseMeta.getChangelogURL(file.projectID(), file.id());
		}

		return file.url();
	}

	static String getChangelogURLString(CurseFile file) throws CurseException {
		return getChangelogURL(file).toString();
	}
}
