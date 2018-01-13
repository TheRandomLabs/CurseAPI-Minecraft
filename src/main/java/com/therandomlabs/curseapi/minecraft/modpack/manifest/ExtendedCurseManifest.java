package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.FileInfo;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.util.CloneException;
import com.therandomlabs.curseapi.util.MiscUtils;
import com.therandomlabs.utils.collection.CollectionUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;

public class ExtendedCurseManifest implements Cloneable {
	public String manifestType = "minecraftModpack";
	public int manifestVersion = 1;
	public String name;
	public String version;
	public String author;
	public String description;
	public Mod[] files;
	public Mod[] serverOnlyMods = new Mod[0];
	public Mod[] alternativeMods = new Mod[0];
	public GroupInfo[] groups = new GroupInfo[0];
	public FileInfo[] additionalFiles = new FileInfo[0];
	public String overrides = "Overrides";
	public MinecraftInfo minecraft;
	//OptiFine version can be null so it's easier to check whether a manifest is actually
	//extended
	public String optifineVersion;
	public int minimumRam = 3072;
	public int recommendedRam = 4096;
	public int minimumServerRam = 2048;
	public int recommendedServerRam = 3072;

	@Override
	public ExtendedCurseManifest clone() {
		final ExtendedCurseManifest manifest = new ExtendedCurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CloneException.tryClone(files);
		manifest.serverOnlyMods = CloneException.tryClone(serverOnlyMods);
		manifest.alternativeMods = CloneException.tryClone(alternativeMods);
		manifest.groups = CloneException.tryClone(groups);
		manifest.additionalFiles = CloneException.tryClone(additionalFiles);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();
		manifest.optifineVersion = optifineVersion;
		manifest.minimumRam = minimumRam;
		manifest.recommendedRam = recommendedRam;

		return manifest;
	}

	public boolean isActuallyExtended() {
		return optifineVersion != null;
	}

	public CurseManifest toCurseManifest() {
		final CurseManifest manifest = new CurseManifest();

		manifest.manifestType = manifestType;
		manifest.manifestVersion = manifestVersion;
		manifest.name = name;
		manifest.version = version;
		manifest.author = author;
		manifest.description = description;
		manifest.files = CurseManifest.CurseMod.fromMods(files);
		manifest.overrides = overrides;
		manifest.minecraft = minecraft.clone();

		return manifest;
	}

	public void sort() {
		Arrays.sort(files, (mod1, mod2) -> mod1.title.compareTo(mod2.title));
		Arrays.sort(serverOnlyMods, (mod1, mod2) -> mod1.title.compareTo(mod2.title));
		Arrays.sort(alternativeMods, (mod1, mod2) -> mod1.title.compareTo(mod2.title));
		Arrays.sort(additionalFiles, (file1, file2) -> file1.path.compareTo(file2.path));
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public String toPrettyJsonWithTabs() {
		return toPrettyJson().replaceAll("  ", "\t");
	}

	public boolean containsMod(int projectID, int fileID) {
		for(Mod mod : files) {
			if(mod.projectID == projectID && mod.fileID == fileID) {
				return true;
			}
		}
		return false;
	}

	public void client() {
		removeModsIf(mod -> mod.side == Side.SERVER);
	}

	public void both() {
		moveServerOnlyModsToFiles();
	}

	public void server() {
		removeModsIf(mod -> mod.side == Side.CLIENT);
		moveServerOnlyModsToFiles();
	}

	public void preferGroups(String... groups) {
		preferGroups(new ImmutableList<>(groups));
	}

	public Map<String, GroupInfo> getGroups(Collection<String> groupNames) {
		final Map<String, GroupInfo> groups = new HashMap<>(groupNames.size());
		for(String name : groupNames) {
			final GroupInfo group = GroupInfo.getGroup(this.groups, name);
			if(group != null) {
				groups.put(name, group);
			}
		}
		return groups;
	}

	public void preferGroups(Collection<String> groupNames) {
		final Map<String, GroupInfo> groups = getGroups(groupNames);

		final List<Mod> moveFiles = new TRLList<>();
		final Set<String> moveFilesGroups = new HashSet<>();
		final List<Mod> moveAlternatives = new TRLList<>();
		final Set<String> moveAlternativesGroups = new HashSet<>();

		for(Mod mod : files) {
			if(mod.group.isEmpty() || groupNames.contains(mod.group)) {
				continue;
			}

			final Map.Entry<String, GroupInfo> group = GroupInfo.getGroup(groups, mod.group);
			if(group == null) {
				continue;
			}

			final List<String> otherGroupNames =
					group.getValue().getOtherGroupNames(group.getKey());
			if(otherGroupNames.contains(mod.group)) {
				moveFiles.add(mod);
				moveFilesGroups.add(group.getKey());
			}
		}

		for(Mod mod : alternativeMods) {
			if(mod.group.isEmpty() || groupNames.contains(mod.group)) {
				continue;
			}

			if(moveFilesGroups.contains(mod.group)) {
				moveAlternatives.add(mod);
				continue;
			}

			final Map.Entry<String, GroupInfo> group = GroupInfo.getGroup(groups, mod.group);
			if(group == null) {
				continue;
			}

			final List<String> otherGroupNames =
					group.getValue().getOtherGroupNames(group.getKey());
			if(otherGroupNames.contains(mod.group)) {
				moveAlternatives.add(mod);
				moveAlternativesGroups.add(group.getKey());
			}
		}

		for(Mod mod : files) {
			if(moveAlternativesGroups.contains(mod.group)) {
				moveFiles.add(mod);
			}
		}

		final List<Mod> fileList = new TRLList<>(files);
		fileList.addAll(moveAlternatives);
		fileList.removeAll(moveFiles);
		files = fileList.toArray(new Mod[0]);

		final List<Mod> alternativesList = new TRLList<>(alternativeMods);
		alternativesList.addAll(moveFiles);
		alternativesList.removeAll(moveAlternatives);
		alternativeMods = alternativesList.toArray(new Mod[0]);
	}

	public TRLList<Mod> getOptionalMods() {
		final TRLList<Mod> mods = new TRLList<>();
		for(Mod mod : files) {
			if(mod.optional) {
				mods.add(mod);
			}
		}
		return mods;
	}

	public void moveServerOnlyModsToFiles() {
		final TRLList<Mod> files = new TRLList<>(this.files);
		files.addAll(serverOnlyMods);
		this.files = files.toArray(new Mod[0]);
	}

	public void removeModsIf(Predicate<Mod> predicate) {
		final List<Mod> newMods = new TRLList<>(files.length);
		for(Mod mod : files) {
			if(!predicate.test(mod)) {
				newMods.add(mod);
			}
		}
		files = newMods.toArray(new Mod[0]);
	}

	public TRLList<Path> getExcludedPaths(Side side) {
		return CollectionUtils.convert(FileInfo.getExcludedPaths(additionalFiles, side),
				Paths::get);
	}

	public static ExtendedCurseManifest ensureExtended(ExtendedCurseManifest manifest)
			throws CurseException {
		return manifest.isActuallyExtended() ?
				manifest : manifest.toCurseManifest().toExtendedManifest();
	}

	public static ExtendedCurseManifest from(String path) throws IOException {
		return from(Paths.get(path));
	}

	public static ExtendedCurseManifest from(Path path) throws IOException {
		return MiscUtils.fromJson(path, ExtendedCurseManifest.class);
	}
}
