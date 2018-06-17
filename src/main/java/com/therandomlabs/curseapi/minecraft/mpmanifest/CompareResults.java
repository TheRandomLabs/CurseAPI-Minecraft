package com.therandomlabs.curseapi.minecraft.mpmanifest;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.ThreadUtils;

public class CompareResults implements Serializable {
	private static final long serialVersionUID = 3470798086960813569L;

	private final ExtendedMPManifest oldManifest;
	private final ExtendedMPManifest newManifest;

	private final TRLList<Mod> unchanged;
	private final TRLList<VersionChange> updated;
	private final TRLList<VersionChange> downgraded;
	private final TRLList<Mod> removed;
	private final TRLList<Mod> added;

	private boolean unchangedLoaded;
	private boolean updatedLoaded;
	private boolean downgradedLoaded;
	private boolean removedLoaded;
	private boolean addedLoaded;

	CompareResults(ExtendedMPManifest oldManifest, ExtendedMPManifest newManifest,
			TRLList<Mod> unchanged, TRLList<VersionChange> updated,
			TRLList<VersionChange> downgraded, TRLList<Mod> removed, TRLList<Mod> added) {
		this.oldManifest = oldManifest;
		this.newManifest = newManifest;
		this.unchanged = unchanged;
		this.updated = updated;
		this.downgraded = downgraded;
		this.removed = removed;
		this.added = added;
	}

	public ExtendedMPManifest getOldManifest() {
		return oldManifest;
	}

	public ExtendedMPManifest getNewManifest() {
		return newManifest;
	}

	public TRLList<Mod> getUnchanged(boolean loadData) throws CurseException {
		if(!unchangedLoaded && loadData) {
			load(unchanged);
			unchangedLoaded = true;
		}

		return unchanged;
	}

	public TRLList<VersionChange> getUpdated(boolean loadData) throws CurseException {
		if(!updatedLoaded && loadData) {
			loadVersionChanges(updated);
			updatedLoaded = true;
		}

		return updated;
	}

	public TRLList<VersionChange> getDowngraded(boolean loadData) throws CurseException {
		if(!downgradedLoaded && loadData) {
			loadVersionChanges(downgraded);
			downgradedLoaded = true;
		}

		return downgraded;
	}

	public TRLList<Mod> getRemoved() throws CurseException {
		if(!removedLoaded) {
			load(removed);
			removedLoaded = true;
		}

		return removed;
	}

	public TRLList<Mod> getAdded() throws CurseException {
		if(!addedLoaded) {
			load(added);
			addedLoaded = true;
		}

		return added;
	}

	public Map<VersionChange, Map<String, String>> getUpdatedChangelogs(boolean urls,
			boolean quietly) throws CurseException, IOException {
		return VersionChange.getChangelogs(updated, urls, quietly);
	}

	public Map<VersionChange, Map<String, String>> getDowngradedChangelogs(boolean urls,
			boolean quietly) throws CurseException, IOException {
		return VersionChange.getChangelogs(downgraded, urls, quietly);
	}

	public String getOldForgeVersion() {
		return oldManifest.minecraft.getForgeVersion();
	}

	public String getNewForgeVersion() {
		return newManifest.minecraft.getForgeVersion();
	}

	public int compareForgeVersions() throws CurseException, IOException {
		return MinecraftForge.compare(getOldForgeVersion(), getNewForgeVersion());
	}

	private static void load(TRLList<Mod> mods) throws CurseException {
		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), mods.size(),
				index -> mods.get(index).title());
		mods.sort();
	}

	private static void loadVersionChanges(TRLList<VersionChange> versionChanges)
			throws CurseException {
		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), versionChanges.size(),
				index -> versionChanges.get(index).preload());
		versionChanges.sort();
	}
}
