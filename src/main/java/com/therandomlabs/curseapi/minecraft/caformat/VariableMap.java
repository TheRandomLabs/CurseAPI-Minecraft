package com.therandomlabs.curseapi.minecraft.caformat;

import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.curseapi.file.ReleaseType;
import com.therandomlabs.curseapi.minecraft.MinecraftVersion;

public class VariableMap extends HashMap<Variable, String> {
	private static final long serialVersionUID = 4066524090845095425L;

	public VariableMap() {
		super(Variable.variables.size());
		for(Variable variable : Variable.variables) {
			put(variable, variable.defaultValue());
		}
	}

	public String get(String variable) {
		return get(Variable.fromName(variable));
	}

	public String put(String variableName, String value) {
		final Variable variable = Variable.fromName(variableName);
		if(variable == null) {
			throw new IllegalArgumentException("Invalid variable: " + variableName);
		}
		return put(variable, value);
	}

	@Override
	public String put(Variable variable, String value) {
		if(variable == null || value == null) {
			throw new NullPointerException();
		}

		if(!variable.isValid(value)) {
			throw new IllegalArgumentException("Invalid value \"" + value + "\" for variable: " +
					variable);
		}

		return super.put(variable, value);
	}

	@Override
	public void putAll(Map<? extends Variable, ? extends String> map) {
		if(!(map instanceof VariableMap)) {
			throw new UnsupportedOperationException("VariableMap.putAll only supports other " +
					"VariableMaps");
		}
		super.putAll(map);
	}

	public MinecraftVersion mcVersion() {
		return MinecraftVersion.fromString(get(Variable.MINECRAFT));
	}

	public MinecraftVersion mcVersionGroup() {
		return MinecraftVersion.groupFromString(get(Variable.MINECRAFT));
	}

	public ReleaseType minimumStability() {
		return ReleaseType.fromName(get(Variable.MINIMUM_STABILITY));
	}

	public int integer(String variable) {
		return Integer.parseInt(get(variable));
	}

	public int integer(Variable variable) {
		return Integer.parseInt(get(variable));
	}
}
