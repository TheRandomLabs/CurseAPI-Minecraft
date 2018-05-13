package com.therandomlabs.curseapi.minecraft.mpmanifest.compare;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.mpmanifest.ExtendedMPManifest;
import com.therandomlabs.curseapi.project.InvalidProjectIDException;
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

	public TRLList<Mod> getUnchanged() throws CurseException {
		if(!unchangedLoaded) {
			load(unchanged);
			unchangedLoaded = true;
		}

		return unchanged;
	}

	private void load(TRLList<Mod> mods) throws CurseException {
		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), mods.size(), index -> {
			try {
				mods.get(index).title();
			} catch(InvalidProjectIDException ex) {
				mods.set(index, null);
			}
		});
		mods.removeIf(Objects::isNull);
		mods.sort();
	}

	public TRLList<VersionChange> getUpdated() {
		return updated;
	}

	public Map<VersionChange, Map<String, String>> getUpdatedChangelogs(boolean urls)
			throws CurseException, IOException {
		return VersionChange.getChangelogs(updated, urls, false);
	}

	public Map<VersionChange, Map<String, String>> getUpdatedChangelogsQuietly(boolean urls)
			throws CurseException, IOException {
		return VersionChange.getChangelogs(updated, urls, true);
	}

	public TRLList<VersionChange> getDowngraded() {
		return downgraded;
	}

	public Map<VersionChange, Map<String, String>> getDowngradedChangelogs(boolean urls)
			throws CurseException, IOException {
		return VersionChange.getChangelogs(downgraded, urls, false);
	}

	public Map<VersionChange, Map<String, String>> getDowngradedChangelogsQuietly(
			boolean urls) throws CurseException, IOException {
		return VersionChange.getChangelogs(downgraded, urls, true);
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

	public boolean hasForgeVersionChanged() {
		return !getOldForgeVersion().equals(getNewForgeVersion());
	}

	public String getOldForgeVersion() {
		return oldManifest.minecraft.getForgeVersion();
	}

	public String getNewForgeVersion() {
		return newManifest.minecraft.getForgeVersion();
	}
}
