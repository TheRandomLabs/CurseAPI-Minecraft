package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CurseFileList;
import com.therandomlabs.curseapi.CurseProject;
import com.therandomlabs.curseapi.ReleaseType;
import com.therandomlabs.curseapi.minecraft.MCEventHandling;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.modpack.FileInfo;
import com.therandomlabs.curseapi.minecraft.modpack.FileSide;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.CurseManifest;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest.GroupInfo;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.MinecraftInfo;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.TRLCollectors;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.concurrent.ThreadUtils;
import com.therandomlabs.utils.io.IOUtils;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.misc.StringUtils;
import com.therandomlabs.utils.number.NumberUtils;
import com.therandomlabs.utils.wrapper.BooleanWrapper;

public class CAManifest {
	// [ Primary Mod Group ] [ Some Other Mod That Does Something Similar ]
	public static final char GROUP_DEFINER_OPENER = '[';
	public static final char GROUP_DEFINER_CLOSER = ']';
	public static final char GROUP_MARKER = ';';

	public static final String COMMENT = "//";
	public static final char COMMENT_CHARACTER = ':';

	private final Map<Variable, String> variables = new HashMap<>();
	private final Map<Preprocessor, String> preprocessors = new HashMap<>();
	private final Map<Postprocessor, String> postprocessors = new HashMap<>();
	private final TRLList<GroupInfo> groups = new TRLList<>();
	private final TRLList<Mod> mods = new TRLList<>();
	private final TRLList<FileInfo> additionalFiles = new TRLList<>();

	private CAManifest() {}

	public Map<Variable, String> getVariables() {
		return variables;
	}

	public Map<Preprocessor, String> getPreprocessors() {
		return preprocessors;
	}

	public Map<Postprocessor, String> getPostprocessors() {
		return postprocessors;
	}

	public TRLList<Mod> getMods() {
		return mods;
	}

	public TRLList<FileInfo> getAdditionalFiles() {
		return additionalFiles;
	}

	public CurseManifest toCurseManifest() throws CurseException {
		return toExtendedCurseManifest().toCurseManifest();
	}

	public ExtendedCurseManifest toExtendedCurseManifest() throws CurseException {
		final ExtendedCurseManifest manifest = new ExtendedCurseManifest();

		manifest.name = variables.get(Variable.NAME);
		manifest.version = variables.get(Variable.VERSION);
		manifest.author = variables.get(Variable.AUTHOR);
		manifest.description = variables.get(Variable.DESCRIPTION);
		manifest.files = mods.toArray(new Mod[0]);
		manifest.alternativeMods = new Mod[0]; //TODO mods should already have group data,
												//it just needs to be split now
		manifest.groups = groups.toArray(new GroupInfo[0]);
		manifest.additionalFiles = additionalFiles.toArray(new FileInfo[0]);
		manifest.minecraft = new MinecraftInfo(variables.get(Variable.MINECRAFT),
				MinecraftForge.get(manifest.version, variables.get(Variable.FORGE)));
		manifest.optifineVersion = variables.get(Variable.OPTIFINE);
		manifest.minimumRam = Double.parseDouble(variables.get(Variable.MINIMUM_RAM));
		manifest.recommendedRam = Double.parseDouble(variables.get(Variable.RECOMMENDED_RAM));

		return manifest;
	}

	public void writeTo(Path path) throws CurseException, IOException {
		NIOUtils.write(path, toExtendedCurseManifest().toPrettyJsonWithTabs(), true);
	}

	public static CAManifest parse(List<String> lines)
			throws CurseException, IOException, ManifestParseException {
		final TRLList<String> pruned = prune(lines);

		final CAManifest manifest = new CAManifest();

		parseVariables(pruned, manifest.variables);
		parsePreprocessors(pruned, manifest.preprocessors);
		parseGroups(pruned, manifest.groups);
		parseMods(pruned, manifest.mods, manifest.additionalFiles, manifest.variables);
		parsePostprocessors(pruned, manifest);
		retrieveModInfo(manifest.mods, manifest.variables);

		return manifest;
	}

	public static TRLList<String> prune(List<String> lines) {
		final TRLList<String> pruned = new TRLList<>(lines.size());

		for(String line : lines) {
			line = line.trim();
			if(!line.isEmpty() && !line.startsWith(COMMENT) &&
					line.charAt(0) != COMMENT_CHARACTER) {
				line = StringUtils.split(line, COMMENT_CHARACTER)[0];
				pruned.add(line);
			}
		}

		return pruned;
	}

	public static CAManifest from(Path path)
			throws CurseException, IOException, ManifestParseException {
		return parse(Files.readAllLines(path));
	}

	private static void parseVariables(TRLList<String> lines,
			Map<Variable, String> variables) throws ManifestParseException {
		final Map<Integer, String> filtered = filter(lines, Variable.CHARACTER);

		for(String line : filtered.values()) {
			final String[] data = getData(line);
			final Variable variable = Variable.fromName(data[0]);
			if(variable == null) {
				throw new ManifestParseException("Invalid variable: " + data[0]);
			}

			final String value = join(data, 1);

			if(!variable.isValid(value)) {
				throw new ManifestParseException("Invalid value \"%s\" for variable: %s",
						value, variable);
			}

			variables.put(variable, value);
		}

		//TODO make sure all variables are declared
	}

	private static void parsePreprocessors(TRLList<String> lines,
			Map<Preprocessor, String> preprocessors)
			throws CurseException, IOException, ManifestParseException {
		final Map<Integer, String> filtered = filter(lines, Preprocessor.CHARACTER);

		for(Map.Entry<Integer, String> line : filtered.entrySet()) {
			final String[] data = getData(line);
			final Preprocessor preprocessor = Preprocessor.fromName(data[0]);
			if(preprocessor == null) {
				throw new ManifestParseException("Invalid preprocessor: " + data[0]);
			}

			final String value = join(data, 1);
			final String[] args = getData(value);

			if(!preprocessor.isValid(value, args)) {
				throw new ManifestParseException("Invalid value \"%s\" for preprocessor: %s",
						value, preprocessor);
			}

			preprocessor.apply(lines, line.getKey(), value, args);

			preprocessors.put(preprocessor, value);
		}
	}

	private static void parseGroups(List<String> lines, List<GroupInfo> groups)
			throws ManifestParseException {
		final Map<Integer, String> filtered = filter(lines, GROUP_DEFINER_OPENER);

		for(String line : filtered.values()) {
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
						groupNames.add(StringUtils.removeLastChar(groupName.toString()));
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

			final TRLList<String> list = new TRLList<>(groupNames);
			final GroupInfo group =
					new GroupInfo(list.get(0), list.subList(1).toArray(new String[0]));
			groups.add(group);
		}
	}

	private static void parseMods(List<String> lines, List<Mod> mods,
			List<FileInfo> additionalFiles, Map<Variable, String> variables)
			throws ManifestParseException {
		lines = lines.stream().filter(line -> !line.startsWith(Postprocessor.CHARACTER + " ")).
				collect(TRLCollectors.toArrayList());

		String group = "";
		for(String line : lines) {
			String[] data = getData(line);

			if(data[0].equals(String.valueOf(GROUP_MARKER))) {
				group = join(data, 1);
				continue;
			}

			final BooleanWrapper client = new BooleanWrapper();
			final BooleanWrapper server = new BooleanWrapper();
			final BooleanWrapper both = new BooleanWrapper();
			final BooleanWrapper optional = new BooleanWrapper();

			int i = 0;
			for(; i < data.length; i++) {
				if(data[i].charAt(0) != Marker.CHARACTER) {
					break;
				}

				if(data[i].length() != 2) {
					throw new ManifestParseException("Invalid marker: " + data[i].substring(1));
				}

				parseMarker(line, data[i].charAt(1), client, server, both, optional);
			}

			final FileSide side = FileSide.fromBooleans(client.get(), server.get(), both.get());

			data = ArrayUtils.subArray(data, i);

			parseModData(line, data, side, group, optional.get(), mods, additionalFiles,
					variables);
		}
	}

	private static void parseMarker(String line, char marker, BooleanWrapper client,
			BooleanWrapper server, BooleanWrapper both, BooleanWrapper optional)
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
		default:
			throw new ManifestParseException("Invalid marker: " + marker);
		}
	}

	private static void parseModData(String line, String[] data, FileSide side, String group,
			boolean optional, List<Mod> mods, List<FileInfo> additionalFiles,
			Map<Variable, String> variables) throws ManifestParseException {
		if(data.length == 0) {
			throw new ManifestParseException("A project ID or file path must be defined: " + line);
		}

		final int projectID = NumberUtils.parseInt(data[0], 0);
		if(projectID >= CurseAPI.MIN_PROJECT_ID) {
			final int relatedFilesIndex;

			int fileID = data.length > 1 ? NumberUtils.parseInt(data[1], 0) : 0;
			if(fileID < CurseAPI.MIN_PROJECT_ID) {
				fileID = -1;
				relatedFilesIndex = 1;
			} else {
				relatedFilesIndex = 2;
			}

			final Mod mod = new Mod();

			mod.projectID = projectID;
			mod.fileID = fileID;
			mod.side = side;
			mod.optional = optional;
			mod.relatedFiles =
					getRelatedFiles(side, ArrayUtils.subArray(data, relatedFilesIndex), line);
			mod.group = group;

			mods.add(mod);
		} else {
			final String path = ArrayUtils.join(data, " ");

			if(!IOUtils.isValidPath(path)) {
				throw new ManifestParseException("Invalid path: " + path);
			}

			additionalFiles.add(new FileInfo(path, side));
		}
	}

	private static FileInfo[] getRelatedFiles(FileSide side, String[] data, String line)
			throws ManifestParseException {
		final List<FileInfo> relatedFiles = new TRLList<>();

		final BooleanWrapper client = new BooleanWrapper();
		final BooleanWrapper server = new BooleanWrapper();
		final BooleanWrapper both = new BooleanWrapper();
		final BooleanWrapper optional = new BooleanWrapper();

		for(String element : data) {
			if(element.length() > 1 && element.charAt(0) == Marker.CHARACTER) {
				if(element.length() != 2) {
					throw new ManifestParseException("Invalid marker: " + element.substring(1));
				}

				parseMarker(line, element.charAt(1), client, server, both, optional);

				if(optional.get()) {
					throw new ManifestParseException("A related file may not be defined as " +
							"optional: " + line);
				}

				side = FileSide.fromBooleans(client.get(), server.get(), both.get());

				continue;
			}

			//TODO support spaces

			if(!IOUtils.isValidPath(element)) {
				throw new ManifestParseException("Invalid path: " + element);
			}

			relatedFiles.add(new FileInfo(element, side));

			client.set(false);
			server.set(false);
			both.set(false);
			optional.set(false);
		}

		return relatedFiles.toArray(new FileInfo[0]);
	}

	private static void parsePostprocessors(List<String> lines, CAManifest manifest)
			throws ManifestParseException {
		final Map<Integer, String> filtered = filter(lines, Postprocessor.CHARACTER);

		for(String line : filtered.values()) {
			final String[] data = getData(line);
			final Postprocessor postprocessor =  Postprocessor.fromName(data[0]);
			if(postprocessor == null) {
				throw new ManifestParseException("Invalid postprocessor: " + data[0]);
			}

			final String value = join(data, 1);
			final String[] args = getData(value);

			if(!postprocessor.isValid(value, args)) {
				throw new ManifestParseException("Invalid value \"%s\" for postprocessor: %s",
						value, postprocessor);
			}

			parseMods(postprocessor.apply(manifest, value, args), manifest.mods,
					manifest.additionalFiles, manifest.variables);

			manifest.postprocessors.put(postprocessor, value);
		}
	}

	private static void retrieveModInfo(List<Mod> mods, Map<Variable, String> variables)
			throws CurseException {
		ThreadUtils.splitWorkload(CurseAPI.getMaximumThreads(), mods.size(), index -> {
			final Mod mod = mods.get(index);
			final CurseProject project = CurseProject.fromID(mod.projectID);

			final CurseFileList list = project.files().
					filterMCVersionGroup(variables.get(Variable.MINECRAFT)).
					filterMinimumStability(
							ReleaseType.fromName(variables.get(Variable.MINIMUM_STABILITY)));
			if(list.isEmpty()) {
				MCEventHandling.forEach(handler -> handler.noFilesFound(mod.projectID));
				return;
			}

			mod.title = project.title();
			mod.fileID = list.get(0).id();
		});
	}

	private static Map<Integer, String> filter(List<String> lines, char character) {
		final Map<Integer, String> filtered = new HashMap<>();
		for(int i = 0, j = 0; i < lines.size(); i++, j++) {
			if(lines.get(i).charAt(0) == character && lines.get(i).charAt(1) == ' ') {
				filtered.put(j, lines.get(i).substring(2));
				lines.remove(i--);
			}
		}
		return filtered;
	}

	private static String[] getData(String line) {
		return StringUtils.splitWhitespace(line);
	}

	private static String[] getData(Map.Entry<Integer, String> line) {
		return getData(line.getValue());
	}

	private static String join(String[] strings, int index) {
		return ArrayUtils.join(ArrayUtils.subArray(strings, index), " ");
	}
}
