package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.modpack.FileSide;
import com.therandomlabs.curseapi.minecraft.modpack.Mod;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest.GroupInfo;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.TRLCollectors;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.misc.StringUtils;
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

	public static CAManifest parse(List<String> lines)
			throws CurseException, IOException, ManifestParseException {
		final TRLList<String> pruned = prune(lines);

		final CAManifest manifest = new CAManifest();

		parseVariables(pruned, manifest.variables);
		parsePreprocessors(pruned, manifest.preprocessors);
		parseGroups(pruned, manifest.groups);
		parseMods(pruned, manifest.mods);
		parsePostprocessors(pruned, manifest);

		return null;
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

	private static void parseVariables(TRLList<String> lines,
			Map<Variable, String> variables) throws ManifestParseException {
		final Map<Integer, String> filtered = filter(lines, Preprocessor.CHARACTER);

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

			if(!preprocessor.isValid(value)) {
				throw new ManifestParseException("Invalid value \"%s\" for preprocessor: %s",
						value, preprocessor);
			}

			preprocessor.apply(lines, line.getKey(), value);

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

	private static void parseMods(List<String> lines, List<Mod> mods)
			throws CurseException, ManifestParseException {
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

			parseModData(line, data, side, group, optional.get(), mods);
		}
	}

	private static void parseMarker(String line, char marker, BooleanWrapper client,
			BooleanWrapper server, BooleanWrapper both, BooleanWrapper optional)
			throws ManifestParseException {
		switch(marker) {
		case Marker.CLIENT:
			if(client.get() || server.get() || both.get()) {
				throw new ManifestParseException(
						"A mod definition may only have one side marker: " + line);
			}

			client.set(true);
			break;
		case Marker.SERVER:
			if(client.get() || server.get() || both.get()) {
				throw new ManifestParseException(
						"A mod definition may only have one side marker: " + line);
			}

			server.set(true);
			break;
		case Marker.BOTH:
			if(client.get() || server.get() || both.get()) {
				throw new ManifestParseException(
						"A mod definition may only have one side marker: " + line);
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
		}
	}

	private static void parseModData(String line, String[] data, FileSide side, String group,
			boolean optional, List<Mod> mods) throws CurseException, ManifestParseException {
		if(data.length == 0) {
			throw new ManifestParseException("A project ID or file path must be defined: " + line);
		}

		//TODO
	}

	private static void parsePostprocessors(List<String> lines, CAManifest manifest)
			throws CurseException, ManifestParseException {
		final Map<Integer, String> filtered = filter(lines, Postprocessor.CHARACTER);

		for(String line : filtered.values()) {
			final String[] data = getData(line);
			final Postprocessor postprocessor =  Postprocessor.fromName(data[0]);
			if(postprocessor == null) {
				throw new ManifestParseException("Invalid postprocessor: " + data[0]);
			}

			final String value = join(data, 1);

			if(!postprocessor.isValid(value)) {
				throw new ManifestParseException("Invalid value \"%s\" for postprocessor: %s",
						value, postprocessor);
			}

			parseMods(postprocessor.apply(manifest, value), manifest.mods);

			manifest.postprocessors.put(postprocessor, value);
		}
	}

	private static Map<Integer, String> filter(List<String> lines, char character) {
		final Map<Integer, String> filtered = new HashMap<>();
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).startsWith(character + " ")) {
				filtered.put(i, lines.get(i).substring(2));
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
