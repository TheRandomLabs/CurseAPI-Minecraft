package com.therandomlabs.curseapi.minecraft.modpack.old;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseFileList;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.modpack.installer.InstallerData;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.MinecraftInfo;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ModLoaderInfo;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLCollectors;
import com.therandomlabs.utils.collection.TRLList;

public final class Modpack {
	private final String name;
	private final String version;
	private final String author;
	private final String description;
	private final String overrides;
	private final MinecraftVersion minecraftVersion;
	private final String forgeVersion;
	private final String optifineVersion;
	private final double minimumRam;
	private final double recommendedRam;

	private TRLList<ModInfo> mods;
	private final TRLList<ModInfo> originalMods;
	private final TRLList<ModInfo> clientMods;
	private final TRLList<ModInfo> serverMods;

	private final TRLList<String> clientOnlyFiles;
	private final TRLList<String> serverOnlyFiles;

	public Modpack(String name, String version, String author, String description,
			MinecraftVersion minecraftVersion, String forgeVersion, ModInfo[] files,
			String optifineVersion, double minimumRam, double recommendedRam)
			throws CurseException {
		this(name, version, author, description, "Overrides", minecraftVersion, forgeVersion,
				files, optifineVersion, minimumRam, recommendedRam);
	}

	public Modpack(String name, String version, String author, String description,
			String overrides, MinecraftVersion minecraftVersion, String forgeVersion,
			ModInfo[] files, String optifineVersion, double minimumRam,
			double recommendedRam) throws CurseException {
		this.name = name;
		this.version = version;
		this.author = author;
		this.description = description;
		this.overrides = overrides;
		this.minecraftVersion = minecraftVersion;

		if(forgeVersion.equals("latest")) {
			this.forgeVersion = MinecraftForge.getLatestVersion(minecraftVersion);
		} else if(forgeVersion.equals("recommended")) {
			this.forgeVersion = MinecraftForge.getRecommendedVersion(minecraftVersion);
		} else {
			this.forgeVersion = forgeVersion;
		}

		this.optifineVersion = optifineVersion;
		this.minimumRam = minimumRam;
		this.recommendedRam = recommendedRam;

		mods = new TRLList<>(files);
		originalMods = mods.toImmutableList();
		clientMods = mods.stream().
				filter(file -> file.type != FileType.SERVER_ONLY).
				collect(TRLCollectors.toArrayList());
		serverMods = new ImmutableList<>(files).stream().
				filter(file -> file.type != FileType.CLIENT_ONLY).
				collect(TRLCollectors.toArrayList());

		final TRLList<String> clientOnlyFiles = new TRLList<>();
		clientMods.stream().filter(file -> file.type == FileType.CLIENT_ONLY).
				forEach(file -> clientOnlyFiles.addAll(file.relatedFiles));
		this.clientOnlyFiles = clientOnlyFiles.toImmutableList();

		final TRLList<String> serverOnlyFiles = new TRLList<>();
		serverMods.stream().filter(file -> file.type == FileType.SERVER_ONLY).
				forEach(file -> serverOnlyFiles.addAll(file.relatedFiles));
		this.serverOnlyFiles = serverOnlyFiles.toImmutableList();
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getFullName() {
		return name + " " + version;
	}

	public String getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public String getOverrides() {
		return overrides;
	}

	public MinecraftVersion getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getMinecraftVersionString() {
		return minecraftVersion.toString();
	}

	public String getForgeVersion() {
		return forgeVersion;
	}

	public String getOptifineVersion() {
		return optifineVersion;
	}

	public double getMinimumRam() {
		return minimumRam;
	}

	public double getRecommendedRam() {
		return recommendedRam;
	}

	public String getModLoader() {
		return "forge-" + forgeVersion.split("-")[1];
	}

	public TRLList<ModInfo> getMods() {
		return mods;
	}

	public CurseFileList getCurseFileList() throws CurseException {
		return ModInfo.toCurseFileList(mods.toArray(new ModInfo[0]));
	}

	public TRLList<ModInfo> getOriginalMods() {
		return originalMods;
	}

	public TRLList<ModInfo> getClientMods() {
		return clientMods;
	}

	public TRLList<ModInfo> getServerMods() {
		return serverMods;
	}

	public void removeServerOnlyMods() {
		removeMods(FileType.SERVER_ONLY);
	}

	public void removeClientOnlyMods() {
		removeMods(FileType.CLIENT_ONLY);
	}

	private void removeMods(FileType typeToRemove) {
		final TRLList<ModInfo> mods = new TRLList<>(this.mods.size());

		for(ModInfo mod : this.mods) {
			if(mod.type != typeToRemove) {
				mods.add(mod);
			}
		}

		this.mods = mods;
	}

	public void removeMods(Collection<ModInfo> mods) {
		this.mods.removeAll(mods);
	}

	public void removeInstallerDataMods(Collection<InstallerData.ModData> mods) {
		for(int i = 0; i < this.mods.size(); i++) {
			for(InstallerData.ModData mod : mods) {
				if(this.mods.get(i).fileID == mod.fileID) {
					this.mods.remove(i--);
					break;
				}
			}
		}
	}

	public TRLList<String> getClientOnlyFiles() {
		return clientOnlyFiles;
	}

	public TRLList<String> getServerOnlyFiles() {
		return serverOnlyFiles;
	}

	public String toJson() {
		return new Gson().toJson(toModpackInfo());
	}

	public String toPrettyJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(toModpackInfo());
	}

	public String toPrettyJsonWithTabs() {
		return toPrettyJson().replaceAll("  ", "\t");
	}

	public ModpackManifest toModpackInfo() {
		final ModpackManifest info = new ModpackManifest();

		info.manifestType = "minecraftModpack";
		info.manifestVersion = 1;
		info.name = name;
		info.version = version;
		info.author = author;
		info.description = description;
		info.files = mods.toArray(new ModInfo[0]);
		info.overrides = overrides;
		info.minecraft = toMinecraftInfo();
		info.optifineVersion = optifineVersion;
		info.minimumRam = minimumRam;
		info.recommendedRam = recommendedRam;

		return info;
	}

	public MinecraftInfo toMinecraftInfo() {
		final MinecraftInfo info = new MinecraftInfo();

		info.version = minecraftVersion;
		info.libraries = "libraries";
		info.modLoaders = toModLoaderInfos();

		return info;
	}

	public ModLoaderInfo[] toModLoaderInfos() {
		final ModLoaderInfo info = new ModLoaderInfo();

		info.id = getModLoader();
		info.primary = true;

		return new ModLoaderInfo[] {info};
	}

	public static Modpack from(Path manifest) throws CurseException, IOException {
		return ModpackManifest.from(manifest).toModpack();
	}
}
