package org.pititom.core.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pititom.core.Format;
import org.pititom.core.event.Handler;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.service.Provider;

/**
 * 
 * @author Thomas Pérennou
 */
@Provider(Parser.class)
@Format("pml")
public class PmlParser implements Parser<MarkupNode> {
	private class Tag {
		protected final Tag parent;
		protected final String name;

		public Tag(Tag parent, String name) {
			this.parent = parent;
			this.name = name;
		}
	}

	public void parse(Reader reader, Handler<ParsingData<MarkupNode>> handler) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line, indentation = null;
		int currentLevel = 0, previousLevel = 0;
		Tag current = null;
		this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.LIST, null, null);
		try {
			while ((line = bufferedReader.readLine()) != null) {
				String trimed = line.trim();
				if ("".equals(trimed) || trimed.startsWith("#")) {
					continue;
				}
				
				previousLevel = currentLevel;
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
				
				line = trimed;
				
				String key;
				String value;
				int separatorIndex = line.indexOf(':');
				if (separatorIndex != -1) {
					key = line.substring(0, separatorIndex).trim();
					value = line.substring(separatorIndex + 1).trim();
				} else {
					key = null;
					value = line;
				}
				if (currentLevel > previousLevel) {
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.LIST, null, current);
					current = new Tag(current, key);
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.TAG, current.name, current.parent);
				} else if (currentLevel < previousLevel) {
					Tag parent = current;
					for (int i = previousLevel; i >= currentLevel; i--) {
						this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TAG, parent.name, parent.parent);
						if (currentLevel != i) {
							this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.LIST, null, parent.parent);
						}
						parent = parent.parent;
					}
					current = new Tag((current == null) ? null : parent, key);
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.TAG, current.name, current.parent);
				} else {
					if (current != null) {
						this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TAG, current.name, current.parent);
					}
					current = new Tag((current == null) ? null : current.parent, key);
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.TAG, current.name, current.parent);
				}
				if (!"".equals(value)) {
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.TEXT, value, current.parent);
					this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TEXT, value, current.parent);
				}
			}
		} catch (IOException exception) {
			Logger.log(this, LogLevel.ERROR, exception);
		}
		while (current != null) {
			this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TAG, current.name, current.parent);
			current = current.parent;
			if (current != null) {
				this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.LIST, null, current);
			}
			
		}
		this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.LIST, null, null);
	}

	public static void main(String[] args) throws Exception {
		final Queue<String> currentTag = Collections.asLifoQueue(new ArrayDeque<String>());
		PmlParser parser = new PmlParser();
		parser.parse(new BufferedReader(new FileReader(args[0])), new Handler<ParsingData<MarkupNode>>() {
			private int level = 0;

			public void handleEvent(ParsingData<MarkupNode> data) {

				switch (data.getEvent()) {
					case START_NODE:
						switch (data.getNodeType()) {
							case TEXT:
								System.out.print(" \"" + data.getValue());
								break;
							case ANCHOR:
								break;
							case REFERENCE:
								break;
							case TYPE:
								break;
							case MAP:
								System.out.println();
								this.println("{");
								break;
							case LIST:
								System.out.println();
								this.println("[");
								break;
							case TAG:
								level++;
								this.print("« " + data.getValue() + ":");
								currentTag.add(data.getValue());
								break;
						}
						break;
					case END_NODE:
						switch (data.getNodeType()) {
							case TEXT:
								System.out.println("\"");
								break;
							case ANCHOR:
								break;
							case REFERENCE:
								break;
							case TYPE:
								break;
							case MAP:
								this.println("}");
								break;
							case LIST:
								this.println("]");
								break;
							case TAG:
								this.println(currentTag.poll() + " »");
								level--;
								break;
						}
						break;
				}
			}

			private void print(String text) {
				for (int i = 0; i < level; i++) {
					System.out.print('\t');
				}
				System.out.print(text);
			}

			private void println(String text) {
				this.print(text + '\n');
			}
		});
	}

	public String getIdentifier() {
		return PmlParser.class.getName();
	}

	private void fireEvent(Handler<ParsingData<MarkupNode>> handler, ParsingEvent event, MarkupNode nodeType, String value, Tag parent) {
		handler.handleEvent(new ParsingData<MarkupNode>(this, event, nodeType, value, (parent == null) ? null : parent.name));
	}
}
