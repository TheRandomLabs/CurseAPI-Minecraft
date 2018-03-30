package com.therandomlabs.curseapi.minecraft.caformat;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.ReleaseType;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.MinecraftInfo;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.number.NumberUtils;

public class Variable {
	public static final char CHARACTER = '$';

	static final List<Variable> variables = new TRLList<>();

	public static final String LATEST = "latest";
	public static final String RECOMMENDED = "recommended";

	public static final Variable NAME = new Variable("name",
			"My Modpack",
			name -> true,
			(manifest, variables, value) -> manifest.name = value
	);
	public static final Variable MINECRAFT = new Variable("minecraft",
			MinecraftVersion.latest().toString(),
			version -> MinecraftVersion.fromString(version) != null,
			null //FORGE should set the Minecraft version as well
	);
	public static final Variable VERSION = new Variable("version",
			MINECRAFT.defaultValue() + "-1.0.0.0",
			version -> true,
			(manifest, variables, value) -> manifest.version = value
	);
	public static final Variable FORGE = new Variable("forge",
			"latest",
			version -> {
				if(version.equals(LATEST) || version.equals(RECOMMENDED)) {
					return true;
				}

				try {
					return MinecraftForge.isValidVersion(version);
				} catch(CurseException | IOException ignored) {}

				return false;
			},
			(manifest, variables, value) -> {
				final String mcVersion = variables.get(MINECRAFT);
				try {
					manifest.minecraft =
							new MinecraftInfo(mcVersion, MinecraftForge.get(mcVersion, value));
				} catch(IOException ex) {
					throw new CurseException(ex);
				}
			}
	);
	public static final Variable MINIMUM_STABILITY = new Variable("minimum_stability",
			ReleaseType.ALPHA.toString(),
			releaseType -> ReleaseType.fromName(releaseType) != null,
			null
	);
	public static final Variable AUTHOR = new Variable("author",
			"Me",
			author -> true,
			(manifest, variables, value) -> manifest.author = value
	);
	public static final Variable DESCRIPTION = new Variable("description",
			"The coolest modpack!",
			description -> true,
			(manifest, variables, value) -> manifest.description = value
	);
	public static final Variable PROJECT_ID = new Variable("project_id",
			"0",
			string -> {
				final int id = NumberUtils.parseInt(string, 0);
				return id == 0 || id >= CurseAPI.MIN_PROJECT_ID;
			},
			(manifest, variables, value) -> manifest.projectID = Integer.parseInt(value)
	);
	public static final Variable OPTIFINE = new Variable("optifine",
			LATEST,
			version -> true,
			(manifest, variables, value) -> manifest.optifineVersion = value
	);
	public static final Variable MINIMUM_RAM = new Variable("minimum_ram",
			"3072",
			NumberUtils::isInteger,
			(manifest, variables, value) -> manifest.minimumRam = Integer.parseInt(value)
	);
	public static final Variable RECOMMENDED_RAM = new Variable("recommended_ram",
			"4096",
			NumberUtils::isInteger,
			(manifest, variables, value) -> manifest.recommendedRam = Integer.parseInt(value)
	);
	public static final Variable MINIMUM_SERVER_RAM = new Variable("minimum_server_ram",
			"2048",
			NumberUtils::isInteger,
			(manifest, variables, value) -> manifest.minimumServerRam = Integer.parseInt(value)
	);
	public static final Variable RECOMMENDED_SERVER_RAM = new Variable("recommended_server_ram",
			"3072",
			NumberUtils::isInteger,
			(manifest, variables, value) -> manifest.recommendedServerRam = Integer.parseInt(value)
	);

	private final String name;
	private final String defaultValue;
	private final Predicate<String> validator;
	private final ApplyToManifest applyToManifest;

	public Variable(String name, String defaultValue, Predicate<String> validator,
			ApplyToManifest applyToManifest) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.validator = validator;
		this.applyToManifest = applyToManifest;

		variables.add(this);
	}

	public static Variable fromName(String name) {
		for(Variable variable : variables) {
			if(variable.toString().equalsIgnoreCase(name)) {
				return variable;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public static Variable[] getVariables() {
		return variables.toArray(new Variable[0]);
	}

	public String defaultValue() {
		return defaultValue;
	}

	public boolean isValid(String value) {
		return validator.test(value);
	}

	public void apply(ExtendedCurseManifest manifest, VariableMap variables,
			String value) throws CurseException {
		if(applyToManifest != null) {
			applyToManifest.apply(manifest, variables, value);
		}
	}

	@FunctionalInterface
	public interface ApplyToManifest {
		void apply(ExtendedCurseManifest manifest, VariableMap variables, String value)
				throws CurseException;
	}
}
