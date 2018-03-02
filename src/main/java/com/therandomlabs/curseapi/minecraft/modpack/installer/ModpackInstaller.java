package com.therandomlabs.curseapi.minecraft.modpack.installer;

import static com.therandomlabs.utils.logging.Logging.getLogger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.curseforge.CurseForge;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.MCEventHandler;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.Minecraft;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.CurseManifest;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.util.DocumentUtils;
import com.therandomlabs.curseapi.util.MiscUtils;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.concurrent.ThreadUtils;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.misc.Assertions;
import com.therandomlabs.utils.misc.Timer;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

//https://github.com/google/gson/issues/395 may occur
//TODO iterateModSources, installOptiFine, installForge, createEULAAndServerStarters
//TODO update modpack data AFTER file is copied/deleted - AtomicBoolean?
public final class ModpackInstaller {
	public static final URL LIGHTCHOCOLATE;

	private static final List<Path> temporaryFiles = new TRLList<>();

	private Path installDir = Minecraft.getDirectory();
	private Path installerData = Paths.get("ca_modpack_data.json");

	private final HashSet<String> preferredGroups = new HashSet<>();
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

	private boolean shouldCopyOverrides = true;
	private boolean shouldFinish = true;

	private boolean running;

	static {
		URL lightchocolate = null;
		try {
			lightchocolate =
					new URL("https://github.com/TheRandomLabs/LightChocolate/archive/master.zip");
		} catch(MalformedURLException ex) {}
		LIGHTCHOCOLATE = lightchocolate;

		Runtime.getRuntime().addShutdownHook(new Thread(ModpackInstaller::deleteTemporaryFiles));
	}

	{
		extensionsWithVariables.add("cfg");
		extensionsWithVariables.add("json");
		extensionsWithVariables.add("txt");
	}

	public ModpackInstaller installTo(String directory) {
		return installTo(Paths.get(directory));
	}

	public ModpackInstaller installTo(Path directory) {
		ensureNotRunning();
		installDir = directory;
		return this;
	}

	public Path getInstallDir() {
		return installDir;
	}

	public ModpackInstaller writeInstallerDataTo(String data) {
		return writeInstallerDataTo(Paths.get(data));
	}

	public ModpackInstaller writeInstallerDataTo(Path data) {
		ensureNotRunning();
		installerData = data;
		return this;
	}

	public Path getInstallerData() {
		return installerData;
	}

	public ModpackInstaller withPreferredGroups(String... groups) {
		ensureNotRunning();
		preferredGroups.addAll(new ImmutableList<>(groups));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<String> getPreferredGroups() {
		return (Set<String>) preferredGroups.clone();
	}

	public ModpackInstaller withModSource(String... source) {
		return withModSource(ArrayUtils.convert(new Path[0], source, Paths::get));
	}

	public ModpackInstaller withModSource(Path... source) {
		ensureNotRunning();
		modSources.addAll(new ImmutableList<>(source));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<Path> getModSources() {
		return (Set<Path>) modSources.clone();
	}

	public ModpackInstaller withExtensionsWithVariables(String... extensions) {
		ensureNotRunning();
		extensionsWithVariables.addAll(new ImmutableList<>(extensions));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<String> getExtensionsWithVariables() {
		return (Set<String>) extensionsWithVariables.clone();
	}

	public ModpackInstaller excludeProject(int... projectID) {
		ensureNotRunning();
		excludedProjects.addAll(new ImmutableList<>(ArrayUtils.toBoxedArray(projectID)));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<Integer> getExcludedProjects() {
		return (Set<Integer>) excludedProjects.clone();
	}

	public ModpackInstaller excludePath(String... path) {
		return excludePath(ArrayUtils.convert(new Path[0], path, Paths::get));
	}

	public ModpackInstaller excludePath(Path... path) {
		ensureNotRunning();
		excludedPaths.addAll(new ImmutableList<>(path));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Set<Path> getExcludedPaths() {
		return (Set<Path>) excludedPaths.clone();
	}

	public ModpackInstaller redownloadMods(boolean flag) {
		ensureNotRunning();
		redownloadMods = flag;
		return this;
	}

	public boolean doesRedownloadMods() {
		return redownloadMods;
	}

	public ModpackInstaller side(Side side) {
		ensureNotRunning();
		this.side = side;
		return this;
	}

	public Side getSide() {
		return side;
	}

	public ModpackInstaller installForge(boolean flag) {
		ensureNotRunning();
		installForge = flag;
		return this;
	}

	public boolean doesInstallForge() {
		return installForge;
	}

	public ModpackInstaller deleteOldForgeVersion(boolean flag) {
		ensureNotRunning();
		deleteOldForgeVersion = flag;
		return this;
	}

	public boolean doesDeleteOldForgeVersion() {
		return deleteOldForgeVersion;
	}

	public ModpackInstaller createEULA(boolean flag) {
		ensureNotRunning();
		createEULA = flag;
		return this;
	}

	public boolean doesCreateEULA() {
		return createEULA;
	}

	public ModpackInstaller createServerStarters(boolean flag) {
		ensureNotRunning();
		createServerStarters = flag;
		return this;
	}

	public boolean doesCreateServerStarters() {
		return createServerStarters;
	}

	public ModpackInstaller threads(int threads) {
		ensureNotRunning();
		this.threads = threads;
		return this;
	}

	public int getNumberOfThreads() {
		return threads;
	}

	public ModpackInstaller dataAutosaveInterval(long interval) {
		ensureNotRunning();
		dataAutosaveInterval = interval;
		return this;
	}

	public long getDataAutosaveInterval() {
		return dataAutosaveInterval;
	}

	public void install(int projectID) throws CurseException, IOException, ZipException {
		ensureNotRunning();
		install(CurseProject.fromID(projectID).files().get(0));
	}

	public void install(int projectID, int fileID)
			throws CurseException, IOException, ZipException {
		ensureNotRunning();
		install(CurseProject.fromID(projectID).fileFromID(fileID));
	}

	public void install(CurseProject project) throws CurseException, IOException, ZipException {
		ensureNotRunning();
		install(project.files().get(0));
	}

	public void install(CurseFile file) throws CurseException, IOException, ZipException {
		install(file.fileURL());
	}

	public void install(String url) throws CurseException, IOException, ZipException {
		install(new URL(url));
	}

	public void install(URL url) throws CurseException, IOException, ZipException {
		ensureNotRunning();

		final Path downloaded = tempPath();

		MCEventHandling.forEach(handler -> handler.downloadingFromURL(url));

		NIOUtils.download(url, downloaded);

		installFromZip(downloaded);
	}

	public void installFromZip(Path zip) throws CurseException, IOException, ZipException {
		Assertions.file(zip);
		installFromZip(new ZipFile(zip.toFile()));
	}

	public void installFromZip(ZipFile zip) throws CurseException, IOException, ZipException {
		ensureNotRunning();

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

		shouldCopyOverrides = false;

		installFromDirectory(extracted);
	}

	public void installFromDirectory(String modpack) throws CurseException, IOException {
		installFromDirectory(Paths.get(modpack));
	}

	public void installFromDirectory(Path modpack) throws CurseException, IOException {
		initialize();

		final Path manifestPath = modpack.resolve("manifest.json");

		if(!Files.exists(manifestPath)) {
			throw new CurseException("manifest.json not found in modpack directory: " + modpack);
		}

		final ExtendedCurseManifest manifest = ExtendedCurseManifest.from(manifestPath);

		shouldFinish = false;
		installFromManifest(manifest);
		shouldFinish = true;

		copyFiles(modpack.resolve(manifest.overrides), manifest);

		finish();
	}

	public void installFromManifest(URL url) throws CurseException, IOException {
		final ExtendedCurseManifest manifest =
				new Gson().fromJson(DocumentUtils.read(url), ExtendedCurseManifest.class);
		installFromManifest(manifest);
	}

	public void installFromManifest(Path manifest) throws CurseException, IOException {
		installFromManifest(ExtendedCurseManifest.from(manifest));
	}

	public void installFromManifest(CurseManifest manifest) throws CurseException, IOException {
		installFromManifest(manifest.toExtendedManifest());
	}

	public void installFromManifest(ExtendedCurseManifest manifest)
			throws CurseException, IOException {
		initialize();

		data.minecraftVersion = manifest.minecraft.version.toString();
		data.forgeVersion = manifest.minecraft.getForgeVersion();

		excludedPaths.addAll(manifest.getExcludedPaths(side));

		if(dataAutosaveInterval > 0L) {
			autosaver = new Timer(() -> {
				try {
					NIOUtils.write(installerData, new Gson().toJson(data));
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

		//TODO test
		manifest.preferGroups(preferredGroups);

		deleteOldFiles(manifest);
		downloadMods(manifest);

		finish();
	}

	private void deleteOldFiles(ExtendedCurseManifest manifest)
			throws CurseException, IOException {
		if(!Files.exists(installerData)) {
			if(autosaver != null) {
				autosaver.start();
			}
			return;
		}

		final InstallerData oldData = MiscUtils.fromJson(installerData, InstallerData.class);
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

			MCEventHandling.forEach(handler -> handler.deleting(toString(oldForge)));

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
						resolve(NIOUtils.getName(modLocation)));
			}

			//Deleting related files
			for(String relatedFile : mod.relatedFiles) {
				//Related files are the only files here that can be directories
				final Path toDelete = installDir.resolve(relatedFile);
				if(Files.isDirectory(toDelete)) {
					NIOUtils.deleteDirectory(toDelete);
				} else {
					Files.deleteIfExists(toDelete);
				}
			}
		}

		//Deleting old installedFiles - if they're needed, they'll be copied back anyway
		for(String file : oldData.installedFiles) {
			MCEventHandling.forEach(handler -> handler.deleting(file));
			Files.deleteIfExists(installDir.resolve(file));
		}
	}

	private void getModsToKeep(InstallerData oldData, ExtendedCurseManifest manifest) {
		//Don't keep any mods
		if(redownloadMods) {
			return;
		}

		final List<InstallerData.ModData> modsToKeep = new TRLList<>();
		final List<InstallerData.ModData> deletedMods = new TRLList<>();

		for(InstallerData.ModData mod : oldData.mods) {
			if(excludedProjects.contains(mod.projectID)) {
				continue;
			}

			if(manifest.containsMod(mod.projectID, mod.fileID)) {
				Path modLocation = installDir.resolve(mod.location);

				final String fileName = NIOUtils.getName(modLocation);

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
				} else {
					deletedMods.add(mod);
				}
			}
		}

		//Remove from oldData so all the old mods can be safely removed
		oldData.mods.removeAll(modsToKeep);
		//Remove deleted mods from oldData so deleteOldFiles doesn't try to delete them
		oldData.mods.removeAll(deletedMods);
		//Remove from modpack so they aren't redownloaded
		removeModsFromManifest(manifest, modsToKeep);
	}

	private void downloadMods(ExtendedCurseManifest manifest) throws CurseException, IOException {
		if(manifest.files.length == 0) {
			return;
		}

		ensureDirectoryExists(installDir.resolve("mods"));

		final AtomicInteger count = new AtomicInteger();
		final int threads = this.threads > 0 ? this.threads : CurseAPI.getMaximumThreads();

		try {
			ThreadUtils.splitWorkload(threads, manifest.files.length, index -> {
				downloadMod(manifest.files[index], count.incrementAndGet(), manifest.files.length);
			});
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

	private void downloadMod(Mod mod, int count, int total) throws CurseException, IOException {
		MCEventHandling.forEach(handler -> handler.downloadingMod(mod.title, count, total));

		final URL url = CurseForge.getFileURL(mod.projectID, mod.fileID);
		final Path downloaded = NIOUtils.downloadToDirectory(url, installDir.resolve("mods"));
		final Path relativizedLocation = installDir.relativize(downloaded);

		final InstallerData.ModData modData = new InstallerData.ModData();

		modData.projectID = mod.projectID;
		modData.fileID = mod.fileID;
		modData.location = toString(relativizedLocation);
		modData.relatedFiles = mod.getRelatedFiles(side);

		MCEventHandling.forEach(handler -> {
			handler.downloadedMod(mod.title, downloaded.getFileName().toString(), count);
		});

		data.mods.add(modData);
	}

	private void copyFiles(Path overrides, ExtendedCurseManifest manifest) throws IOException {
		Files.walkFileTree(overrides, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
					throws IOException {
				copyFile(overrides, file, manifest);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path directory,
					BasicFileAttributes attributes) throws IOException {
				visitDirectory(overrides, directory);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	void copyFile(Path overrides, Path file, ExtendedCurseManifest manifest)
			throws IOException {
		final Path relativized = overrides.relativize(file);
		if(isExcluded(relativized)) {
			return;
		}

		final Path newFile = installDir.resolve(relativized);
		if(Files.isDirectory(newFile)) {
			NIOUtils.deleteDirectory(newFile);
		}

		try {
			MCEventHandling.forEach(handler -> handler.copying(toString(relativized)));
		} catch(CurseException ex) {} //It's just autosaving; it doesn't matter

		final String name = NIOUtils.getName(file);

		boolean shouldReplaceVariables = false;
		for(String extension : extensionsWithVariables) {
			if(name.endsWith('.' + extension)) {
				shouldReplaceVariables = true;
				break;
			}
		}

		if(shouldReplaceVariables) {
			replaceVariablesAndCopy(file, newFile, manifest);
		} else {
			if(shouldCopyOverrides) {
				Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		data.installedFiles.add(toString(relativized));
	}

	private boolean isExcluded(Path path) {
		path = installDir.resolve(path);
		for(Path excludedPath : excludedPaths) {
			final Path resolved = installDir.resolve(excludedPath);
			if(path.equals(resolved) || NIOUtils.isParent(resolved, path)) {
				return true;
			}
		}
		return false;
	}

	void visitDirectory(Path overrides, Path directory) throws IOException {
		directory = overrides.relativize(directory);
		if(isExcluded(directory)) {
			return;
		}

		ensureDirectoryExists(installDir.resolve(directory.toString()));
	}

	private void initialize() throws IOException {
		if(running) {
			return;
		}

		Assertions.nonNull(installDir, "installDir");
		Assertions.nonNull(installerData, "installerData");

		if(installerData.getRoot() == null) {
			installerData = installDir.resolve(installerData).toAbsolutePath();
		}

		NIOUtils.ensureParentExists(installerData);

		if(Files.isDirectory(installerData)) {
			NIOUtils.deleteDirectory(installerData);
		}

		for(Path modSource : modSources) {
			Assertions.nonNull(modSource, "modSource");
			Assertions.directory(modSource);
		}

		for(String extensionWithVariable : extensionsWithVariables) {
			Assertions.nonNull(extensionWithVariable, "extensionWithVariable");
			Assertions.nonEmpty(extensionWithVariable, "extensionWithVariable");
		}

		for(Path excludedPath : excludedPaths) {
			Assertions.nonNull(excludedPath, "excludedPath");
			if(excludedPath.getRoot() != null) {
				throw new IllegalArgumentException("\"" + excludedPath +
						"\" has a root component");
			}
		}

		Assertions.nonNull(side, "side");

		if(shouldFinish && running) {
			throw new IllegalStateException("This ModpackInstaller is already running");
		}

		running = true;

		ensureDirectoryExists(installDir);
	}

	private void ensureNotRunning() {
		if(running) {
			throw new IllegalStateException("This ModpackInstaller is already running");
		}
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
		autosaver = null;
		NIOUtils.write(installerData, new Gson().toJson(data));
		data = null;

		deleteTemporaryFiles();

		shouldCopyOverrides = true;
		shouldFinish = true;

		running = false;
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

	private static void ensureDirectoryExists(Path directory) throws IOException {
		if(Files.exists(directory) && !Files.isDirectory(directory)) {
			Files.delete(directory);
		}

		if(!Files.exists(directory)) {
			Files.createDirectory(directory);
		}
	}

	static Path tempPath() {
		final Path path =
				NIOUtils.TEMP_DIRECTORY.get().resolve("CurseAPI_Modpack" + System.nanoTime());
		temporaryFiles.add(path);
		return path;
	}

	private static String toString(Path path) {
		return NIOUtils.toStringWithUnixPathSeparators(path);
	}

	public static boolean replaceVariablesAndCopy(Path file, Path newFile,
			ExtendedCurseManifest manifest) throws IOException {
		try {
			final String toWrite = NIOUtils.readFile(file).
					replaceAll("::MINECRAFT_VERSION::", manifest.minecraft.version.toString()).
					replaceAll("::MODPACK_NAME::", manifest.name).
					replaceAll("::MODPACK_VERSION::", manifest.version).
					replaceAll("::FULL_MODPACK_NAME::", manifest.name + ' ' + manifest.version).
					replaceAll("::MODPACK_AUTHOR::", manifest.author);

			NIOUtils.write(newFile, toWrite, true);
		} catch(MalformedInputException ex) {
			ex.printStackTrace();
			getLogger().error("This exception was caused by the file: " + file);
			getLogger().error("Make sure the file is encoded in UTF-8!");
			getLogger().error("Variables in this file will not be processed.");

			return false;
		}

		return true;
	}

	public static void deleteTemporaryFiles() {
		for(int i = 0; i < temporaryFiles.size(); i++) {
			try {
				if(Files.isDirectory(temporaryFiles.get(i))) {
					NIOUtils.deleteDirectory(temporaryFiles.get(i));
				} else {
					Files.deleteIfExists(temporaryFiles.get(i));
				}
				temporaryFiles.remove(i--);
			} catch(IOException ex) {} //Doesn't matter, probably still in use
		}
	}
}
