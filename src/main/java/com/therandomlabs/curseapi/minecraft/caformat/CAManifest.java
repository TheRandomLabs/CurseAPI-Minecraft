package com.therandomlabs.curseapi.minecraft.caformat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.curseforge.CurseForge;
import com.therandomlabs.curseapi.file.CurseFileList;
import com.therandomlabs.curseapi.minecraft.FileInfo;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.Mod;
import com.therandomlabs.curseapi.minecraft.Side;
import com.therandomlabs.curseapi.minecraft.caformat.post.Postprocessor;
import com.therandomlabs.curseapi.minecraft.caformat.pre.Preprocessor;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.GroupInfo;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.MinecraftInfo;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.Relation;
import com.therandomlabs.curseapi.project.RelationType;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.ImmutableList;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.concurrent.ThreadUtils;
import com.therandomlabs.utils.io.IOUtils;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.number.NumberUtils;
import com.therandomlabs.utils.wrapper.BooleanWrapper;

/*
 * Lines are pruned
 * - Comments and empty lines are removed
 * - Comments at the ends of lines
 *
 * Variables are parsed
 * - Custom variables can set different values in the manifests
 *
 * Preprocessors are parsed
 * - Preprocessors can directly modify the code
 * - Keep parsing preprocessors until there are none left
 *
 * Lines are pruned again
 *
 * Groups are parsed
 *
 * Mods and additional files are parsed
 * - Filter out all of the other characters - variable, preprocessor, group, remove project,
 *     postprocessor
 * - Mod data stored in a private ModData class
 *   - Boolean 'isAlternative' - defines whether it should be stored in alternativeMods or files
 *     - This is calculated later
 *   - Side should determine whether it should be stored in files or serverOnlyMods
 * - Additional files are stored as FileInfos
 *
 * URL ModDatas are converted to normal ModDatas
 * - Multithreaded
 *
 * Mods are pruned and checked
 * - Removed mods are parsed - only one is removed for each removal
 *   - e.g. LC has LunatriusCore, DC removes it, but another pack using DC might want it again
 * - Duplicates are removed
 * - Dependencies are retrieved
 * - Groups are checked
 *
 * Postprocessors are run
 *
 * Mods and additional files are parsed, pruned and checked from the lines that
 * the postprocessors returned
 *
 * ModDatas are converted to Mods
 * - Multithreaded
 * - Gets titles, file URLs
 *
 * Manifest is created
 * - Variable.apply is called for each variable
 * - Mods are put into files, serverOnlyMods and alternativeMods
 * - Additional files
 * - ExtendedCurseManifest.sort
 */
public class CAManifest {
	// [ Primary Mod Group ] [ Some Other Mod That Does Something Similar ]
	public static final char GROUP_DEFINER_OPENER = '[';
	public static final char GROUP_DEFINER_CLOSER = ']';
	public static final char GROUP_MARKER = ';';

	public static final String COMMENT = "//";
	public static final char COMMENT_CHARACTER = ':';

	public static final char REMOVE_PROJECT = '-';

	public static final char SPACE_PLACEHOLDER = '|';

	public static final TRLList<Character> NON_MOD_CHARACTERS = new ImmutableList<>(
			Variable.CHARACTER,
			Preprocessor.CHARACTER,
			GROUP_DEFINER_OPENER,
			Postprocessor.CHARACTER,
			REMOVE_PROJECT
	);

	private List<String> lines;
	private final VariableMap variables = new VariableMap();
	private final List<GroupInfo> groups = new TRLList<>();
	private final List<ModData> mods = new TRLList<>();
	private final List<Mod> files = new TRLList<>();
	private final List<Mod> serverOnlyMods = new TRLList<>();
	private final List<Mod> alternativeMods = new TRLList<>();
	private final List<FileInfo> additionalFiles = new TRLList<>();

	public static class ModData {
		public int projectID;
		public int fileID;
		public Side side;
		public boolean required;
		public FileInfo[] relatedFiles;
		public String[] groups;
		public URL url;
		public boolean isAlternative;
		public boolean getDependencies;

		public ModData() {}
	}

	private CAManifest(List<String> lines) {
		this.lines = lines;
	}

	private ExtendedCurseManifest parse() throws CurseException, IOException {
		pruneLines();

		parseVariables();
		parsePreprocessors();
		parseGroups();

		parseModsAndFiles(lines);

		convertURLs();
		parseRemovedMods(lines);
		removeDuplicateMods();
		removeDuplicateFiles(additionalFiles);

		retrieveDependencies();

		checkGroups();

		parsePostprocessors(lines);

		convertURLs();
		removeDuplicateMods();
		removeDuplicateFiles(additionalFiles);

		convertModDatas();

		return toManifest();
	}

	private void parseVariables() throws CurseException {
		try {
			for(String line : getLines(Variable.CHARACTER)) {
				final String[] data = getData(line);
				variables.put(data[1], join(data, 2));
			}
		} catch(IllegalArgumentException ex) {
			throw new ManifestParseException(ex.getMessage());
		}
	}

	private void parsePreprocessors() throws CurseException, IOException {
		Map<Integer, String> lines;
		while(!(lines = getPreprocessorLines()).isEmpty()) {
			for(Map.Entry<Integer, String> line : lines.entrySet()) {
				final String[] data = getData(line.getValue());
				final Preprocessor preprocessor = Preprocessor.fromName(data[1]);
				if(preprocessor == null) {
					throw new ManifestParseException("Invalid preprocessor: " + data[1]);
				}

				final String value = join(data, 2);
				final String[] args = getData(value);

				if(!preprocessor.isValid(value, args)) {
					throw new ManifestParseException("Invalid value \"%s\" for preprocessor: %s",
							value, preprocessor);
				}

				final List<String> toAdd = preprocessor.apply(value, args);
				if(toAdd != null) {
					this.lines.addAll(line.getKey() + 1, toAdd);
				}

				this.lines.remove((int) line.getKey());
			}
		}

		pruneLines();
	}

	private Map<Integer, String> getPreprocessorLines() {
		final Map<Integer, String> filtered = new HashMap<>();
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).startsWith(Preprocessor.CHARACTER + " ")) {
				filtered.put(i, lines.get(i));
			}
		}
		return filtered;
	}

	private void parseGroups() throws CurseException {
		for(String line : getLines(GROUP_DEFINER_OPENER)) {
			final String[] data = getData(line);

			final Set<String> groupNames = new LinkedHashSet<>();
			final StringBuilder groupName = new StringBuilder();

			boolean needsOpener = false;

			for(String element : data) {
				if(needsOpener) {
					if(element.equals(String.valueOf(GROUP_DEFINER_OPENER))) {
						needsOpener = false;
					}
					continue;
				}

				if(element.equals(String.valueOf(GROUP_DEFINER_CLOSER))) {
					if(groupName.length() != 0) {
						groupNames.add(StringUtils.removeLastChar(groupName.toString()).trim());
						groupName.setLength(0);
					}

					needsOpener = true;
				}

				groupName.append(element).append(' ');
			}

			if(groupName.length() != 0) {
				throw new ManifestParseException("Group definition closer (%s) is required: %s",
						GROUP_DEFINER_CLOSER, line);
			}

			if(groupNames.size() < 2) {
				throw new ManifestParseException("At least one alternative must be defined for " +
						"each group: " + line);
			}

			for(String name : groupNames) {
				if(GroupInfo.hasGroup(groups, name)) {
					throw new ManifestParseException("Duplicate group definition: " + name);
				}
			}

			final TRLList<String> list = new TRLList<>(groupNames);
			final GroupInfo group =
					new GroupInfo(list.get(0), list.subList(1).toArray(new String[0]));
			groups.add(group);
		}
	}

	private void parseModsAndFiles(List<String> lines) throws CurseException {
		lines = getModAndFileLines(lines);
		String[] groups = new String[0];

		for(String line : lines) {
			String[] data = getData(line);

			//Group marker
			if(data[0].equals(String.valueOf(GROUP_MARKER))) {
				groups = StringUtils.split(join(data, 1), (GROUP_MARKER));
				for(int i = 0; i < groups.length; i++) {
					groups[i] = groups[i].trim();
				}
				continue;
			}

			//Parse markers

			final BooleanWrapper client = new BooleanWrapper();
			final BooleanWrapper server = new BooleanWrapper();
			final BooleanWrapper both = new BooleanWrapper();
			final BooleanWrapper optional = new BooleanWrapper();
			final BooleanWrapper getDependencies = new BooleanWrapper();

			int i = 0;
			for(; i < data.length; i++) {
				if(data[i].charAt(0) != Marker.CHARACTER) {
					break;
				}

				if(data[i].length() != 2) {
					throw new ManifestParseException("Invalid marker: " + data[i].substring(1));
				}

				parseMarker(line, data[i].charAt(1), client, server, both, optional,
						getDependencies);
			}

			final Side side = Side.fromBooleans(client.get(), server.get(), both.get());

			data = ArrayUtils.subArray(data, i);

			//Parse mod/file data
			parseModOrFileData(line, data, side, groups, optional.get(), getDependencies.get());
		}
	}

	private static List<String> getModAndFileLines(List<String> lines) {
		final List<String> filtered = new TRLList<>();
		for(String line : lines) {
			boolean shouldAdd = true;
			for(char character : NON_MOD_CHARACTERS) {
				if(line.startsWith(character + " ")) {
					shouldAdd = false;
				}
			}

			if(shouldAdd) {
				filtered.add(line);
			}
		}
		return filtered;
	}

	private static void parseMarker(String line, char marker, BooleanWrapper client,
			BooleanWrapper server, BooleanWrapper both, BooleanWrapper optional,
			BooleanWrapper getDependencies)
			throws ManifestParseException {
		switch(marker) {
		case Marker.CLIENT:
			if(client.get() || server.get() || both.get()) {
				throw new ManifestParseException(
						"A mod or file definition may only have one side marker: " + line);
			}

			client.set(true);
			break;
		case Marker.SERVER:
			if(client.get() || server.get() || both.get()) {
				throw new ManifestParseException(
						"A mod or file definition may only have one side marker: " + line);
			}

			server.set(true);
			break;
		case Marker.BOTH:
			if(client.get() || server.get() || both.get()) {
				throw new ManifestParseException(
						"A mod or file definition may only have one side marker: " + line);
			}

			both.set(true);
			break;

		case Marker.OPTIONAL:
			if(optional.get()) {
				throw new ManifestParseException(
						"A mod definition may only be marked as optional once: " + line);
			}

			optional.set(true);
			break;
		case Marker.GET_DEPENDENCIES:
			if(getDependencies.get()) {
				throw new ManifestParseException(
						"A mod definition may only be marked to get dependencies once: " + line);
			}

			getDependencies.set(true);
			break;
		default:
			throw new ManifestParseException("Invalid marker: " + marker);
		}
	}

	private void parseModOrFileData(String line, String[] data, Side side, String[] groups,
			boolean optional, boolean getDependencies) throws CurseException {
		if(data.length == 0) {
			throw new ManifestParseException("A project ID, URL or file path must be specified: " +
					line);
		}

		URL url = null;
		try {
			url = new URL(data[0]);
		} catch(MalformedURLException ex) {}

		final int projectID = url == null ? NumberUtils.parseInt(data[0], 0) : 0;
		if(url != null || projectID != 0) {
			int fileID = data.length > 1 ? NumberUtils.parseInt(data[1], 0) : 0;
			final int relatedFilesIndex = fileID == 0 ? 1 : 2;

			final ModData mod = new ModData();

			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.side = side;
			mod.required = !optional;
			mod.relatedFiles =
					parseRelatedFiles(side, ArrayUtils.subArray(data, relatedFilesIndex), line);
			mod.groups = groups;
			mod.url = url;
			mod.getDependencies = getDependencies;

			mods.add(mod);

			return;
		}

		if(optional || getDependencies) {
			throw new ManifestParseException("Files cannot be defined as optional or " +
					"marked to get dependencies: " + line);
		}

		final String path = StringUtils.replaceAll(ArrayUtils.join(data, " "),
				SPACE_PLACEHOLDER, ' ');

		if(!IOUtils.isValidPath(path)) {
			throw new ManifestParseException("Invalid path: " + path);
		}

		additionalFiles.add(new FileInfo(path, side));
	}

	private static FileInfo[] parseRelatedFiles(Side side, String[] data, String line)
			throws ManifestParseException {
		final List<FileInfo> relatedFiles = new TRLList<>();

		final BooleanWrapper client = new BooleanWrapper();
		final BooleanWrapper server = new BooleanWrapper();
		final BooleanWrapper both = new BooleanWrapper();
		final BooleanWrapper optional = new BooleanWrapper();
		final BooleanWrapper getDependencies = new BooleanWrapper();

		for(String element : data) {
			if(element.length() > 1 && element.charAt(0) == Marker.CHARACTER) {
				if(element.length() != 2) {
					throw new ManifestParseException("Invalid marker: " + element.substring(1));
				}

				parseMarker(line, element.charAt(1), client, server, both, optional,
						getDependencies);

				if(optional.get() || getDependencies.get()) {
					throw new ManifestParseException("Files cannot be defined as optional or " +
							"marked to get dependencies: " + line);
				}

				side = Side.fromBooleans(client.get(), server.get(), both.get());

				continue;
			}

			element = StringUtils.replaceAll(element, SPACE_PLACEHOLDER, ' ');

			if(!IOUtils.isValidPath(element)) {
				throw new ManifestParseException("Invalid path: " + element);
			}

			relatedFiles.add(new FileInfo(element, side));

			client.set(false);
			server.set(false);
			both.set(false);
			optional.set(false);
		}

		removeDuplicateFiles(relatedFiles);
		return relatedFiles.toArray(new FileInfo[0]);
	}

	private void convertURLs() throws CurseException {
		final List<ModData> urlMods = mods.stream().filter(mod -> mod.url != null).
				collect(Collectors.toList());

		if(urlMods.isEmpty()) {
			return;
		}

		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), urlMods.size(), index -> {
			final ModData mod = urlMods.get(index);
			final boolean isProject = CurseForge.isProject(mod.url);
			final boolean isFile = CurseForge.isFile(mod.url);

			if(!isProject && !isFile) {
				throw new ManifestParseException("The following URL is neither a CurseForge " +
						"project nor file: " + mod.url);
			}

			if(isProject) {
				mod.projectID = CurseForge.getID(mod.url);
			} else {
				mod.projectID = CurseForge.getID(CurseForge.getProjectURLFromFile(mod.url));
				mod.fileID = CurseForge.getFileID(mod.url);
			}

			mod.url = null;
		});
	}

	private void parseRemovedMods(List<String> lines) throws CurseException {
		for(String line : getLines(lines, REMOVE_PROJECT)) {
			final String[] data = getData(line);

			int projectID = 0;
			try {
				//This can potentially slow down parsing because it's not multithreaded
				projectID = CurseProject.fromURL(new URL(data[1])).id();
			} catch(MalformedURLException ex) {}

			projectID = NumberUtils.parseInt(data[1], 0);

			if(projectID < CurseAPI.MIN_PROJECT_ID) {
				throw new ManifestParseException("\"%s\" is not a valid project ID or URL",
						data[1]);
			}

			boolean removed = false;
			for(ModData mod : mods) {
				if(mod.projectID == projectID) {
					removed = true;
					mods.remove(mod);
					break;
				}
			}

			if(!removed) {
				throw new ManifestParseException("Could not remove project: " + data[1]);
			}
		}
	}

	private void removeDuplicateMods() {
		for(int i = 0; i < mods.size(); i++) {
			final ModData mod = mods.get(i);

			for(int j = 0; j < mods.size(); j++) {
				if(i == j) {
					continue;
				}

				final ModData mod2 = mods.get(j);

				if(mod.projectID == mod2.projectID) {
					//Prefer whichever one has a group, then the newer version, then the latest
					//definition
					if(mod.groups.length != 0 && mod2.groups.length == 0) {
						mods.remove(j--);
						continue;
					}

					if(mod.groups.length == 0 && mod2.groups.length != 0) {
						mods.remove(i--);
						break;
					}

					if(mod.fileID > mod2.fileID) {
						mods.remove(j--);
						continue;
					}

					if(mod2.fileID > mod.fileID) {
						mods.remove(i--);
						break;
					}

					if(i > j) {
						mods.remove(j--);
						continue;
					}

					mods.remove(i--);
					break;
				}
			}
		}
	}

	private static void removeDuplicateFiles(List<FileInfo> files) {
		for(int i = 0; i < files.size(); i++) {
			final FileInfo file = files.get(i);

			for(int j = 0; j < files.size(); j++) {
				if(i == j) {
					continue;
				}

				final FileInfo file2 = files.get(j);

				final Path path = Paths.get(file.path);
				final Path path2 = Paths.get(file2.path);

				if(path.equals(path2)) {
					if(i > j) {
						files.remove(j--);
						continue;
					}

					files.remove(i--);
					break;
				}
			}
		}
	}

	private void retrieveDependencies() throws CurseException {
		final List<ModData> dependents =
				mods.stream().filter(mod -> mod.getDependencies).collect(Collectors.toList());

		if(dependents.isEmpty()) {
			return;
		}

		final Map<Integer, ModData> ids = new HashMap<>();

		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), dependents.size(), index -> {
			final ModData dependent = dependents.get(index);
			final List<Relation> dependencies = CurseProject.fromID(dependent.projectID).
					dependencies(RelationType.REQUIRED_LIBRARY);

			for(Relation dependency : dependencies) {
				final int id = CurseForge.getID(dependency.url());

				for(ModData mod : CAManifest.this.mods) {
					if(mod.projectID != id) {
						continue;
					}

					//If it doesn't have a group, it should always be in files, not alternativeMods
					if(mod.groups.length == 0) {
						continue;
					}

					final Set<String> newGroups = new HashSet<>();
					for(String group : mod.groups) {
						newGroups.add(group);
					}
					for(String group : dependent.groups) {
						newGroups.add(group);
					}
					mod.groups = newGroups.toArray(new String[0]);

					if(dependent.side != mod.side) {
						mod.side = Side.BOTH;
					}

					if(dependent.required != mod.required) {
						mod.required = true;
					}
				}

				ModData mod = ids.get(id);
				if(mod == null) {
					mod = new ModData();
					mod.projectID = id;
					mod.side = dependent.side;
					mod.required = dependent.required;
					ids.put(id, mod);
				}

				final Set<String> newGroups = new HashSet<>();
				for(String group : dependent.groups) {
					newGroups.add(group);
				}
				if(mod.groups != null) {
					for(String group : mod.groups) {
						newGroups.add(group);
					}
				}
				mod.groups = newGroups.toArray(new String[0]);

				if(mod.side != dependent.side) {
					mod.side = Side.BOTH;
				}

				if(mod.required != dependent.required) {
					mod.required = dependent.required;
				}
			}
		});

		mods.addAll(ids.values());
	}

	private void checkGroups() throws CurseException {
		for(ModData mod : mods) {
			for(String group : mod.groups) {
				if(group.isEmpty()) {
					throw new ManifestParseException("Group may not be empty");
				}

				if(!GroupInfo.hasGroup(groups, group)) {
					throw new ManifestParseException("Invalid group: " + group);
				}

				if(!GroupInfo.isPrimary(groups, group)) {
					mod.isAlternative = true;
				}
			}
		}
	}

	private void parsePostprocessors(List<String> lines) throws CurseException {
		for(String line : getLines(lines, Postprocessor.CHARACTER)) {
			final String[] data = getData(line);
			final Postprocessor postprocessor = Postprocessor.fromName(data[1]);
			if(postprocessor == null) {
				throw new ManifestParseException("Invalid postprocessor: " + data[1]);
			}

			final String value = join(data, 2);
			final String[] args = getData(value);

			if(!postprocessor.isValid(value, args)) {
				throw new ManifestParseException("Invalid value \"%s\" for postprocessor: %s",
						value, postprocessor);
			}

			final List<String> toParse = postprocessor.apply(variables, mods, value, args);
			parseModsAndFiles(toParse);
			parseRemovedMods(toParse);
			parsePostprocessors(toParse);
		}
	}

	private void convertModDatas() throws CurseException {
		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), mods.size(), index -> {
			final ModData modData = mods.get(index);
			final Mod mod = new Mod();

			mod.projectID = modData.projectID;
			mod.fileID = modData.fileID;
			mod.side = modData.side;
			mod.required = modData.required;
			mod.relatedFiles = modData.relatedFiles;
			mod.groups = modData.groups;

			final CurseProject project = CurseProject.fromID(mod.projectID);

			if(mod.fileID == 0) {
				final CurseFileList files = project.files();

				files.filterVersions(variables.mcVersionGroup());
				files.filterMinimumStability(variables.minimumStability());

				if(files.isEmpty()) {
					MCEventHandling.forEach(handler -> handler.noFilesFound(mod.projectID));
					return;
				}

				mod.fileID = files.get(0).id();
			}

			mod.title = project.title();
			mod.url = project.fileWithID(mod.fileID).fileURL();

			if(modData.isAlternative) {
				alternativeMods.add(mod);
			} else {
				if(mod.side == Side.SERVER) {
					serverOnlyMods.add(mod);
				} else {
					files.add(mod);
				}
			}
		});
	}

	private ExtendedCurseManifest toManifest() throws CurseException, IOException {
		final ExtendedCurseManifest manifest = new ExtendedCurseManifest();

		manifest.name = variables.get("name");
		manifest.version = variables.get("version");
		manifest.author = variables.get("author");
		manifest.description = variables.get("description");
		manifest.files = files.toArray(new Mod[0]);
		manifest.serverOnlyMods = serverOnlyMods.toArray(new Mod[0]);
		manifest.alternativeMods = alternativeMods.toArray(new Mod[0]);
		manifest.groups = groups.toArray(new GroupInfo[0]);
		manifest.additionalFiles = additionalFiles.toArray(new FileInfo[0]);

		final MinecraftVersion mcVersion = variables.mcVersion();
		final String forgeVersion = MinecraftForge.get(mcVersion, variables.get("forge"));
		manifest.minecraft = new MinecraftInfo(mcVersion, forgeVersion);

		manifest.projectID = variables.integer("project_id");
		manifest.optifineVersion = variables.get("optifine");
		manifest.minimumRam = variables.integer("minimum_ram");
		manifest.recommendedRam = variables.integer("recommended_ram");
		manifest.minimumServerRam = variables.integer("minimum_server_ram");
		manifest.recommendedServerRam = variables.integer("recommended_server_ram");

		manifest.sort();

		return manifest;
	}

	private void pruneLines() {
		final List<String> pruned = new TRLList<>(lines.size());
		for(String line : lines) {
			line = line.trim();
			if(!line.isEmpty() && !line.startsWith(COMMENT) &&
					line.charAt(0) != COMMENT_CHARACTER) {
				//Variables can have colons in their values
				if(!line.startsWith(Variable.CHARACTER + " ")) {
					line = StringUtils.split(line, COMMENT_CHARACTER)[0];
				}

				pruned.add(line);
			}
		}
		lines = pruned;
	}

	private List<String> getLines(char character) {
		return getLines(lines, character);
	}

	private static List<String> getLines(List<String> lines, char character) {
		final List<String> filtered = new TRLList<>();
		for(String line : lines) {
			if(line.startsWith(character + " ")) {
				filtered.add(line);
			}
		}
		return filtered;
	}

	private static String[] getData(String line) {
		return StringUtils.splitWhitespace(line);
	}

	private static String join(String[] strings, int index) {
		return ArrayUtils.join(ArrayUtils.subArray(strings, index), " ");
	}

	public static ExtendedCurseManifest parse(String manifest)
			throws CurseException, IOException {
		return parse(Paths.get(manifest));
	}

	public static ExtendedCurseManifest parse(Path manifest)
			throws CurseException, IOException {
		return new CAManifest(Files.readAllLines(manifest)).parse();
	}

	public static ExtendedCurseManifest parse(List<String> lines)
			throws CurseException, IOException {
		return new CAManifest(lines).parse();
	}
}
