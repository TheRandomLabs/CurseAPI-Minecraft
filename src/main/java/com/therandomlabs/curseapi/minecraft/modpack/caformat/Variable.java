package com.therandomlabs.curseapi.minecraft.modpack.caformat;

import java.util.List;
import java.util.function.Predicate;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.ReleaseType;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;
import com.therandomlabs.curseapi.minecraft.forge.MinecraftForge;
import com.therandomlabs.utils.collection.TRLList;
import com.therandomlabs.utils.number.NumberUtils;

public class Variable {
	public static final char CHARACTER = '$';

	public static final String LATEST = "latest";
	public static final String RECOMMENDED = "recommended";

	public static final Variable NAME = new Variable("name",
			"My Modpack",
			name -> true);

	public static final Variable MINECRAFT = new Variable("minecraft",
			MinecraftVersion.latest().toString(),
			version -> MinecraftVersion.fromString(version) != null);

	public static final Variable VERSION = new Variable("version",
			MINECRAFT.defaultValue() + "-1.0.0.0",
			version -> true);

	public static final Variable FORGE = new Variable("forge",
			"latest",
			version -> {
				if(version.equals(LATEST) || version.equals(RECOMMENDED)) {
					return true;
				}

				try {
					return MinecraftForge.isValidVersion(version);
				} catch(CurseException ex) {}

				return false;
			});

	public static final Variable MINIMUM_STABILITY = new Variable("minimum_stability",
			ReleaseType.ALPHA.toString(),
			releaseType -> ReleaseType.fromName(releaseType) != null);

	public static final Variable DESCRIPTION = new Variable("description",
			"The coolest modpack!",
			description -> true);

	public static final Variable OPTIFINE = new Variable("optifine",
			LATEST,
			version -> true); //TODO

	public static final Variable MINIMUM_RAM = new Variable("minimum_ram",
			"3",
			NumberUtils::isDouble);

	public static final Variable RECOMMENDED_RAM = new Variable("recommended_ram",
			"4",
			NumberUtils::isDouble);

	private static final List<Variable> variables = new TRLList<>();

	private final String name;
	private final String defaultValue;
	private final Predicate<String> validator;

	public Variable(String name, String defaultValue, Predicate<String> validator) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.validator = validator;

		variables.add(this);
	}

	@Override
	public String toString() {
		return name;
	}

	public String defaultValue() {
		return defaultValue;
	}

	public boolean isValid(String value) {
		return validator.test(value);
	}

	public static Variable fromName(String name) {
		for(Variable variable : variables) {
			if(variable.toString().equalsIgnoreCase(name)) {
				return variable;
			}
		}

		return null;
	}
}
