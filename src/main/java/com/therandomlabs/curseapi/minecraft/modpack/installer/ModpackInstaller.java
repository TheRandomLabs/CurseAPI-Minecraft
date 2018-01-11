package com.therandomlabs.curseapi.minecraft.modpack.installer;

import static com.therandomlabs.utils.logging.Logging.getLogger;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.Gson;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseFile;
import com.therandomlabs.curseapi.CurseProject;
import com.therandomlabs.curseapi.minecraft.MCEventHandler;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.modpack.Side;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.CurseManifest;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest;
import com.therandomlabs.curseapi.util.CurseEventHandling;
import com.therandomlabs.curseapi.util.MiscUtils;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.misc.Timer;
import com.therandomlabs.utils.network.NetworkUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

//https://github.com/google/gson/issues/395 may occur
//TODO a way to choose alternative mod groups and whether to install optional mods
public final class ModpackInstaller {
	private static final List<Path> temporaryFiles = new TRLList<>();

	private Path installDir = Paths.get(".");
	private Path installerData = Paths.get("ca_modpack_data.json");

	private final HashSet<Path> modSources = new HashSet<>();
	private final HashSet<String> extensionsWithVariables = new HashSet<>();
	private final HashSet<Integer> excludedProjects = new HashSet<>();
	private final HashSet<Path> excludedPaths = new HashSet<>();

	private boolean redownloadMods;

	private Side side = Side.CLIENT;

	private boolean installForge = true;
	private boolean deleteOldForgeVersion = true;

	private boolean createEULA = true;
	private boolean createServerStarters = true;

	private int threads;
	private long dataAutosaveInterval = 3000L;

	private InstallerData data = new InstallerData();
	private Timer autosaver;

	private boolean shouldFinish = true;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(ModpackInstaller::deleteTemporaryFiles));
	}

	{
		extensionsWithVariables.add("cfg");
		extensionsWithVariables.add("json");
		extensionsWithVariables.add("txt");
	}

	public ModpackInstaller installTo(Path directory) {
		Assertions.nonNull(directory, "directory");
		installDir = directory;
		return this;
	}

	public Path getInstallDir() {
		return installDir;
	}

	public ModpackInstaller writeInstallerDataTo(Path data) {
		installerData = data;
		return this;
	}

	public Path getInstallerData() {
		return installerData;
	}

	public ModpackInstaller withModSource(Path... source) {
		modSources.addAll(new ImmutableList<>(source));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<Path> getModSources() {
		return (Set<Path>) modSources.clone();
	}

	public ModpackInstaller withExtensionsWithVariables(String... extensions) {
		extensionsWithVariables.addAll(new ImmutableList<>(extensions));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<String> getExtensionsWithVariables() {
		return (Set<String>) extensionsWithVariables.clone();
	}

	public ModpackInstaller excludeProject(int... projectID) {
		excludedProjects.addAll(new ImmutableList<>(ArrayUtils.toBoxedArray(projectID)));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<Integer> getExcludedProjects() {
		return (Set<Integer>) excludedProjects.clone();
	}

	public ModpackInstaller excludePath(Path... path) {
		excludedPaths.addAll(new ImmutableList<>(path));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<Path> getExcludedPaths() {
		return (Set<Path>) excludedPaths.clone();
	}

	public ModpackInstaller redownloadMods(boolean flag) {
		redownloadMods = flag;
		return this;
	}

	public boolean doesRedownloadMods() {
		return redownloadMods;
	}

	public Side getSide() {
		return side;
	}

	public ModpackInstaller installForge(boolean flag) {
		installForge = flag;
		return this;
	}

	public boolean doesInstallForge() {
		return installForge;
	}

	public ModpackInstaller deleteOldForgeVersion(boolean flag) {
		deleteOldForgeVersion = flag;
		return this;
	}

	public boolean doesDeleteOldForgeVersion() {
		return deleteOldForgeVersion;
	}

	public ModpackInstaller createEULA(boolean flag) {
		createEULA = flag;
		return this;
	}

	public boolean doesCreateEULA() {
		return createEULA;
	}

	public ModpackInstaller createServerStarters(boolean flag) {
		createServerStarters = flag;
		return this;
	}

	public boolean doesCreateServerStarters() {
		return createServerStarters;
	}

	public ModpackInstaller threads(int threads) {
		this.threads = threads;
		return this;
	}

	public int getNumberOfThreads() {
		return threads;
	}

	public ModpackInstaller dataAutosaveInterval(long interval) {
		dataAutosaveInterval = interval;
		return this;
	}

	public long getDataAutosaveInterval() {
		return dataAutosaveInterval;
	}

	public void install(int projectID) throws CurseException, IOException, ZipException {
		install(CurseProject.fromID(projectID).files().get(0));
	}

	public void install(int projectID, int fileID)
			throws CurseException, IOException, ZipException {
		install(CurseProject.fromID(projectID).fileFromID(fileID));
	}

	public void install(CurseProject project) throws CurseException, IOException, ZipException {
		install(project.files().get(0));
	}

	public void install(CurseFile file) throws CurseException, IOException, ZipException {
		install(file.fileURL());
	}

	public void install(URL url) throws CurseException, IOException, ZipException {
		final Path downloaded = tempPath();

		MCEventHandling.forEach(handler -> handler.downloadingFromURL(url));

		NIOUtils.download(url, downloaded);

		installFromZip(downloaded);
	}

	public void installFromManifest(URL url) throws CurseException, IOException {
		CurseEventHandling.forEach(handler -> handler.preDownloadDocument(url.toString()));
		final ExtendedCurseManifest manifest =
				new Gson().fromJson(NetworkUtils.read(url), ExtendedCurseManifest.class);
		CurseEventHandling.forEach(handler -> handler.postDownloadDocument(url.toString()));
		installFromManifest(manifest);
	}

	public void installFromManifest(Path manifest) throws CurseException, IOException {
		installFromManifest(MiscUtils.fromJson(manifest, ExtendedCurseManifest.class));
	}

	public void installFromManifest(CurseManifest manifest) throws CurseException, IOException {
		installFromManifest(manifest.toExtendedManifest());
	}

	public void installFromManifest(ExtendedCurseManifest manifest)
			throws CurseException, IOException {
		if(dataAutosaveInterval > 0L) {
			autosaver = new Timer(() -> {
				try {
					NIOUtils.write(installerData(), new Gson().toJson(data));
					MCEventHandling.forEach(MCEventHandler::autosavedInstallerData);
				} catch(CurseException | IOException ex) {
					//Doesn't matter; this is just autosave anyway
					ex.printStackTrace();
				}
			}, dataAutosaveInterval);
		}

		manifest = ExtendedCurseManifest.ensureExtended(manifest);

		switch(side) {
		case CLIENT:
			manifest.client();
			break;
		case BOTH:
			manifest.both();
			break;
		case SERVER:
			manifest.server();
			break;
		}

		deleteOldFiles(manifest);

		//TODO download mods

		finish();
	}

	public void installFromZip(Path zip) throws CurseException, IOException, ZipException {
		Assertions.file(zip);
		installFromZip(new ZipFile(zip.toFile()));
	}

	public void installFromZip(ZipFile zip) throws CurseException, IOException, ZipException {
		if(!zip.isValidZipFile()) {
			throw new CurseException("Invalid zip file");
		}

		Path extracted = tempPath();
		zip.extractAll(extracted.toString());

		//Mainly to support modpacks downloaded directly from GitHub,
		//where all files are in a parent folder with the name of the repository
		final List<Path> files = NIOUtils.list(extracted);
		if(files.size() == 1 && Files.isDirectory(files.get(0))) {
			extracted = files.get(0);
		}

		installFromDirectory(extracted);
	}

	public void installFromDirectory(Path modpack) throws CurseException, IOException {
		final Path manifest = modpack.resolve("manifest.json");

		if(!Files.exists(manifest)) {
			throw new CurseException("manifest.json not found in modpack directory: " + modpack);
		}

		shouldFinish = false;
		installFromManifest(MiscUtils.fromJson(manifest, ExtendedCurseManifest.class));
		shouldFinish = true;

		//TODO copy new files

		finish();
	}

	private void deleteOldFiles(ExtendedCurseManifest manifest)
			throws CurseException, IOException {
		final Path path = installerData();
		if(!Files.exists(path)) {
			if(autosaver != null) {
				autosaver.start();
			}
			return;
		}

		final InstallerData oldData = MiscUtils.fromJson(path, InstallerData.class);
		if(autosaver != null) {
			autosaver.start();
		}

		if(oldData.forgeVersion == null) {
			getLogger().warning("Invalid data file!");
			return;
		}

		if(oldData.forgeVersion.equals(manifest.minecraft.getForgeVersion())) {
			//Forge should not be installed because it is already on the correct version
			installForge = false;
		} else if(side == Side.CLIENT && deleteOldForgeVersion) {
			//Delete old Forge
			final Path oldForge = installDir.resolve("versions").resolve(
					MinecraftForge.getInstalledDirectoryName(
							oldData.minecraftVersion,
							oldData.forgeVersion
					)
			);

			MCEventHandling.forEach(handler -> handler.deleting(oldForge.toString()));

			NIOUtils.deleteDirectoryIfExists(oldForge);
		}

		getModsToKeep(oldData, manifest);

		//Deleting old mods that are no longer needed
		for(InstallerData.ModData mod : oldData.mods) {
			MCEventHandling.forEach(handler -> handler.deleting(mod.location));

			final Path modLocation = installDir.resolve(mod.location);
			if(!Files.deleteIfExists(modLocation)) {
				//Some mods are moved to mods/<version>/mod.jar
				Files.deleteIfExists(installDir.resolve("mods").
						resolve(oldData.minecraftVersion).
						resolve(modLocation.getFileName().toString()));
			}

			//Deleting related files
			for(String relatedFile : mod.relatedFiles) {
				Files.deleteIfExists(installDir.resolve(relatedFile));
			}
		}

		//Deleting old installedFiles - if they're needed, they'll be copied back anyway
		for(String file : oldData.installedFiles) {
			MCEventHandling.forEach(handler -> handler.deleting(file));
			Files.deleteIfExists(installDir.resolve(file));
		}
	}

	private void getModsToKeep(InstallerData oldData, ExtendedCurseManifest manifest) {
		//Keep no mods
		if(redownloadMods) {
			return;
		}

		final List<InstallerData.ModData> modsToKeep = new ArrayList<>();
		for(InstallerData.ModData mod : oldData.mods) {
			if(excludedProjects.contains(mod.projectID)) {
				continue;
			}

			if(manifest.containsMod(mod.projectID, mod.fileID)) {
				Path modLocation = installDir.resolve(mod.location);

				final String fileName = modLocation.getFileName().toString();

				final Path altModLocation = installDir.resolve("mods").
						resolve(oldData.minecraftVersion).
						resolve(fileName);

				boolean modExists = Files.exists(altModLocation);

				if(modExists) {
					mod.location = "mods/" + oldData.minecraftVersion + "/" + fileName;
				} else {
					modExists = Files.exists(modLocation);
				}

				if(modExists) {
					modsToKeep.add(mod);
					data.mods.add(mod);
				}
			}
		}

		//Remove from oldData so all the old mods can be safely removed
		oldData.mods.removeAll(modsToKeep);
		//Remove from modpack so they aren't redownloaded
		removeModsFromManifest(manifest, modsToKeep);
	}

	private Path installerData() {
		return installDir.resolve(installerData);
	}

	private void finish() throws IOException {
		if(!shouldFinish) {
			return;
		}

		//Remove empty directories - most of them are probably left over from previous
		//modpack versions
		NIOUtils.deleteDirectory(installDir, NIOUtils.DELETE_EMPTY_DIRECTORIES);

		//Last save
		autosaver.stop();
		NIOUtils.write(installerData(), new Gson().toJson(data));

		deleteTemporaryFiles();
	}

	public static void deleteTemporaryFiles() {
		for(int i = 0; i < temporaryFiles.size(); i++) {
			try {
				if(Files.isDirectory(temporaryFiles.get(i))) {
					NIOUtils.deleteDirectoryIfExists(temporaryFiles.get(i));
				} else {
					Files.deleteIfExists(temporaryFiles.get(i));
				}
				temporaryFiles.remove(i--);
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void removeModsFromManifest(ExtendedCurseManifest manifest,
			List<InstallerData.ModData> mods) {
		manifest.removeModsIf(mod -> {
			for(InstallerData.ModData modToRemove : mods) {
				if(mod.projectID == modToRemove.projectID && mod.fileID == modToRemove.fileID) {
					return true;
				}
			}
			return false;
		});
	}

	private static Path tempPath() {
		final Path path =
				NIOUtils.TEMP_DIRECTORY.get().resolve("CurseAPI_Modpack" + System.nanoTime());
		temporaryFiles.add(path);
		return path;
	}

	/*private static void copyNewFiles(Path modpackLocation, InstallerConfig config,
			InstallerData data, Modpack modpack)
			throws IOException {
		final Path overrides = modpackLocation.resolve(modpack.getOverrides());
		final Path installTo = Paths.get(config.installTo);

		TRLList<String> filesToIgnore =
				config.isServer ? modpack.getClientOnlyFiles() : modpack.getServerOnlyFiles();
		filesToIgnore = filesToIgnore.toArrayList();
		filesToIgnore.addAll(config.excludeFiles);

		final List<String> excludedFiles = filesToIgnore;

		Files.walkFileTree(overrides, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
					throws IOException {
				copyFile(overrides, installTo, excludedFiles, config, data, modpack, file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path directory,
					BasicFileAttributes attributes) throws IOException {
				visitDirectory(overrides, installTo, excludedFiles, directory);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	static void copyFile(Path overrides, Path installTo, List<String> excludedFiles,
			InstallerConfig config, InstallerData data, Modpack modpack, Path file)
			throws IOException {
		final Path relativized = relativize(overrides, file);

		if(shouldSkip(excludedFiles, relativized)) {
			return;
		}

		final Path newFile = installTo(config, relativized);

		if(Files.isDirectory(newFile)) {
			NIOUtils.deleteDirectory(newFile);
		}

		final String name = name(file);

		try {
			MCEventHandling.forEach(handler -> handler.copying(toString(relativized)));
		} catch(CurseException ex) {
			//It's just event handling, shouldn't matter too much ATM
		}

		boolean variablesReplaced = shouldReplaceVariables(config.variableFileExtensions, name);
		if(variablesReplaced) {
			variablesReplaced = replaceVariablesAndCopy(file, newFile, modpack);
		}

		if(!variablesReplaced) {
			Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
		}

		if(!variablesReplaced) {
			if(config.shouldKeepModpack) {
				Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		data.installedFiles.add(toString(relativized));
	}

	private static boolean shouldReplaceVariables(String[] variableFileExtensions, String name) {
		for(String extension : variableFileExtensions) {
			if(name.endsWith("." + extension)) {
				return true;
			}
		}
		return false;
	}

	static void visitDirectory(Path overrides, Path installTo, List<String> excludedFiles,
			Path directory) throws IOException {
		directory = relativize(overrides, directory);

		if(shouldSkip(excludedFiles, directory)) {
			return;
		}

		final Path installToPath = installTo.resolve(directory.toString());

		//Make sure installToPath is a directory that exists

		if(Files.exists(installToPath) && !Files.isDirectory(installToPath)) {
			Files.delete(installToPath);
		}

		if(!Files.exists(installToPath)) {
			Files.createDirectory(installToPath);
		}
	}

	private static boolean replaceVariablesAndCopy(Path file, Path newFile, Modpack modpack)
			throws IOException {
		try {
			final String toWrite = NIOUtils.readFile(file).
					replaceAll(MINECRAFT_VERSION, modpack.getMinecraftVersionString()).
					replaceAll(MODPACK_NAME, modpack.getName()).
					replaceAll(MODPACK_VERSION, modpack.getVersion()).
					replaceAll(FULL_MODPACK_NAME, modpack.getFullName()).
					replaceAll(MODPACK_AUTHOR, modpack.getAuthor()) +
					System.lineSeparator();

			NIOUtils.write(newFile, toWrite);
		} catch(MalformedInputException ex) {
			ex.printStackTrace();
			getLogger().error("This exception was caused by the file: " + file);
			getLogger().error("Make sure the file is encoded in UTF-8!");
			getLogger().error("Variables in this file will not be processed.");

			return false;
		}

		return true;
	}

	private static Path relativize(Path overrides, Path path) {
		return overrides.relativize(path).normalize();
	}

	private static boolean shouldSkip(List<String> excludedFiles, Path path) {
		for(String fileName : excludedFiles) {
			final Path excludedFile = Paths.get("config", fileName).normalize();
			if(path.equals(excludedFile) || NIOUtils.isParent(excludedFile, path)) {
				return true;
			}
		}
		return false;
	}

	private static void downloadMods(InstallerConfig config, InstallerData data, Modpack modpack)
			throws CurseException, IOException {
		if(modpack.getMods().isEmpty()) {
			return;
		}

		final AtomicInteger count = new AtomicInteger();

		final int threads = config.threads > 0 ? config.threads : CurseAPI.getMaximumThreads();

		final int size = modpack.getMods().size();
		try {
			ThreadUtils.splitWorkload(threads, size, index ->
					downloadMod(config, data, modpack.getMods().get(index),
							count.incrementAndGet(), size));
		} catch(Exception ex) {
			if(ex instanceof CurseException) {
				throw (CurseException) ex;
			}

			if(ex instanceof IOException) {
				throw (IOException) ex;
			}

			throw (RuntimeException) ex;
		}
	}

	private static void downloadMod(InstallerConfig config, InstallerData data,
			ModInfo mod, int count, int total) throws CurseException, IOException {
		MCEventHandling.forEach(handler -> handler.downloadingMod(mod.title, count, total));

		final URL url = CurseForge.getFileURL(mod.projectID, mod.fileID);

		final Path downloaded = NIOUtils.downloadToDirectory(url, installTo(config, "mods"));

		final Path relativizedLocation = Paths.get(config.installTo).relativize(downloaded);

		final ModData modData = new ModData();

		modData.projectID = mod.projectID;
		modData.fileID = mod.fileID;
		modData.location = toString(relativizedLocation);
		modData.relatedFiles = mod.relatedFiles;

		MCEventHandling.forEach(
				handler -> handler.downloadedMod(mod.title, name(downloaded), count));

		data.mods.add(modData);
	}*/

	//TODO iterateModSources installForge createEULAAndServerStarters
}
