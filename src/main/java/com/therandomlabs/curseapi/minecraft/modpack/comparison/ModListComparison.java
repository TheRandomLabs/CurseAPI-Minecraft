package com.therandomlabs.curseapi.minecraft.modpack.comparison;

import java.util.Map;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.minecraft.modpack.ModList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.ThreadUtils;

public class ModListComparison {
	private final ModList oldList;
	private final ModList newList;

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

	ModListComparison(ModList oldList, ModList newList, TRLList<Mod> unchanged,
			TRLList<VersionChange> updated, TRLList<VersionChange> downgraded, TRLList<Mod> removed,
			TRLList<Mod> added) {
		this.oldList = oldList;
		this.newList = newList;
		this.unchanged = unchanged;
		this.updated = updated;
		this.downgraded = downgraded;
		this.removed = removed;
		this.added = added;
	}

	public ModList getOldList() {
		return oldList.clone();
	}

	public ModList getNewList() {
		return newList.clone();
	}

	public TRLList<Mod> getUnchanged() {
		return unchanged;
	}

	public TRLList<Mod> loadInfoAndGetUnchanged() throws CurseException {
		if(!unchangedLoaded) {
			loadModInfo(unchanged);
			unchangedLoaded = true;
		}

		return unchanged;
	}

	public TRLList<VersionChange> getUpdated() {
		return updated;
	}

	public TRLList<VersionChange> loadInfoAndGetUpdated() throws CurseException {
		if(!updatedLoaded) {
			loadVersionChangeInfo(updated);
			updatedLoaded = true;
		}

		return updated;
	}

	public TRLList<VersionChange> getDowngraded() {
		return downgraded;
	}

	public TRLList<VersionChange> loadInfoAndGetDowngraded() throws CurseException {
		if(!downgradedLoaded) {
			loadVersionChangeInfo(downgraded);
			downgradedLoaded = true;
		}

		return downgraded;
	}

	public TRLList<Mod> getRemoved() {
		return removed;
	}

	public TRLList<Mod> loadInfoAndGetRemoved() throws CurseException {
		if(!removedLoaded) {
			loadModInfo(removed);
			removedLoaded = true;
		}

		return removed;
	}

	public TRLList<Mod> getAdded() {
		return added;
	}

	public TRLList<Mod> loadInfoAndGetAdded() throws CurseException {
		if(!addedLoaded) {
			loadModInfo(added);
			addedLoaded = true;
		}

		return added;
	}

	public Map<VersionChange, Map<String, String>> getUpdatedChangelogs(boolean urls,
			boolean quietly) throws CurseException {
		return VersionChange.getChangelogs(updated, urls, quietly);
	}

	public Map<VersionChange, Map<String, String>> getDowngradedChangelogs(boolean urls,
			boolean quietly) throws CurseException {
		return VersionChange.getChangelogs(downgraded, urls, quietly);
	}

	public String getOldModLoaderVersion() {
		return oldList.getModLoaderVersion();
	}

	public String getNewModLoaderVersion() {
		return newList.getModLoaderVersion();
	}

	private static void loadModInfo(TRLList<Mod> mods) throws CurseException {
		ThreadUtils.splitWorkload(
				CurseAPI.getMaximumThreads(),
				mods.size(),
				index -> mods.get(index).title()
		);

		mods.sort();
	}

	private static void loadVersionChangeInfo(TRLList<VersionChange> versionChanges)
			throws CurseException {
		ThreadUtils.splitWorkload(
				CurseAPI.getMaximumThreads(),
				versionChanges.size(),
				index -> versionChanges.get(index).loadChangelogFiles()
		);

		versionChanges.sort();
	}
}
