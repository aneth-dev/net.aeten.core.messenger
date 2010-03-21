package org.pititom.core.args4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yaml2Args {
	private Yaml2Args() {
	}
	private static class Entries extends ArrayList<Entry> {
		private Entries parent;
		public Entries(Entries parent) {
			this.parent = parent;
		}
		public Entries getParent() {
			return this.parent;
		}
	}
	private static class Entry {
		private final String key;
		private Object value;
		private List<Entry> parent;

		public Entry(/*List<Entry> parent, */String key, Object value) {
			this.key = key;
			this.value = value;
//			this.parent = parent;
		}

		/**
		 * @return the value
		 */
		protected Object getValue() {
			return this.value;
		}

		/**
		 * @param value
		 *            the value to set
		 */
		protected void setValue(Object value) {
			this.value = value;
		}

		/**
		 * @return the key
		 */
		protected String getKey() {
			return this.key;
		}

		/**
		 * @return the key
		 */
		protected List<Entry> getParent() {
			return this.parent;
		}

		@Override
		public String toString() {
			return this.key + " : " + this.value;
		}
	}

	public static String convert(Reader reader) {
		ArrayList<Entry> conf = loadYamlConfiguration(new BufferedReader(reader));
		return editConfiguration(conf, true);
	}

	public static String convert(String configuration) {
		return convert(new StringReader(configuration));
	}

	public static String convert(File file) throws FileNotFoundException {
		return convert(new FileReader(file));
	}

	private static String editConfiguration(ArrayList<Entry> nodes, boolean isRoot) {
		String args = "";
		
		for (Entry entry : nodes) {
			args += " --" + entry.getKey().trim().replace(' ', '-') + " ";

			if (entry.getValue() instanceof ArrayList) {
				String quote = isRoot ? "\"" : "\\\"";
				args += quote + editConfiguration((ArrayList<Entry>) entry.getValue(), false) + quote;
			} else {
				String value = entry.getValue().toString();
				if (!"true".equals(value)) {
					String quote = value.matches(".*[\\s\\r\\n].*") ? (isRoot ? "\"" : "\\\"") : "";
					args += quote + value + quote;
				}
			}
		}
		return args.trim();
	}

	private static ArrayList<Entry> loadYamlConfiguration(BufferedReader reader) {
		String indentation = null;
		Entries configuration = new Entries(null);
		Entries currentLevelEntries = configuration;
		Entry previousEntry = new Entry("root", currentLevelEntries);
		Entry previousLevelEntry = previousEntry;
		Entry currentLevelEntry = previousEntry;
		int currentLevel = 0, previousLevel = 0;
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				currentLevel = 0;
				if ((indentation == null) && line.matches("^\\s.*")) {
					Pattern pattern = Pattern.compile("^\\s+");
					Matcher matcher = pattern.matcher(line);
					matcher.find();
					indentation = matcher.group();
				}
				if (indentation != null) {
					while (line.startsWith(indentation)) {
						currentLevel++;
						line = line.substring(indentation.length());
					}
				}
				if ("".equals(line)) {
					continue;
				}
				int separatorIndex = line.indexOf(':');
				Entry entry = new Entry(line.substring(0, separatorIndex).trim(), line.substring(separatorIndex + 1).trim());
				if (currentLevel > previousLevel) {
					previousLevelEntry = currentLevelEntry;
					currentLevelEntry = previousEntry;
					currentLevelEntries = new Entries(currentLevelEntries);
					currentLevelEntries.add(entry);
					currentLevelEntry.setValue(currentLevelEntries);
					currentLevelEntry = entry;
					previousLevel++;
				} else if (currentLevel < previousLevel) {
					currentLevelEntry = previousLevelEntry;
					currentLevelEntries = currentLevelEntries.getParent();
					previousLevel--;
//					((ArrayList<Entry>) currentLevelEntry.getValue()).add(entry);
					currentLevelEntries.add(entry);
				} else {
					currentLevelEntries.add(entry);
				}
				previousEntry = entry;
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return configuration;
	}
}
