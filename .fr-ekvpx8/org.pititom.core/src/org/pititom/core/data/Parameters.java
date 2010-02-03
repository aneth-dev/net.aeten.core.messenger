package org.pititom.core.data;

import java.util.HashMap;
import java.util.Map;

public class Parameters<Enumerated> extends
        HashMap<Enum<? extends Enumerated>, String> {
	private static final long serialVersionUID = -10334770134600841L;
	private final Enum<? extends Enumerated>[] fields;

	public Parameters(Enum<? extends Enumerated>[] fields,
	        Map<String, String> parameters) throws ParameterException {
		this.fields = fields;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			Enum<? extends Enumerated> key = getKey(entry.getKey());
			if (key == null)
				throw new ParameterException("Key " + entry.getKey()
				        + " is not valid");
			super.put(key, entry.getValue());
		}
	}
	
	public Parameters(Enum<? extends Enumerated>[] fields,
	        String[][] parameters) throws ParameterException {
		this.fields = fields;
		for (String[] entry : parameters) {
			Enum<? extends Enumerated> key = getKey(entry[0]);
			if (key == null)
				throw new ParameterException("Key " + entry[0]
				        + " is not valid");
			super.put(key, entry[1]);
		}
	}

	public Enum<? extends Enumerated> getKey(String name) {
		for (Enum<? extends Enumerated> field : this.fields)
			if (buildKeyName(field.name()).equals(buildKeyName(name)))
				return field;
		return null;
	}

	public String get(String key) {
		return this.get(this.getKey(key));
	}

	private static String buildKeyName(String name) {
		return name.toLowerCase().replaceAll("_", ".");
	}

	/** @deprecated this map is read only */
	@Deprecated
	@Override
	public String put(Enum<? extends Enumerated> key, String value) {
		return null;
	}
}
