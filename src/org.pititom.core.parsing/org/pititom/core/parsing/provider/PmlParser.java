package org.pititom.core.parsing.provider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pititom.core.event.Handler;
import org.pititom.core.parsing.MarkupNode;
import org.pititom.core.parsing.ParsingData;
import org.pititom.core.parsing.ParsingEvent;
import org.pititom.core.parsing.service.Parser;

/**
 *
 * @author Thomas Pérennou
 */
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
		try {
			while ((line = bufferedReader.readLine()) != null) {
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
				if ("".equals(line)) {
					continue;
				}
				String key;
				String value;
				int separatorIndex = line.indexOf(':');
				if (separatorIndex != -1) {
					key = line.substring(0, separatorIndex).trim();
					value = line.substring(separatorIndex + 1).trim();
				} else {
					key = null;
					value = line.trim();
				}
				if (currentLevel > previousLevel) {
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.LIST, null, current);
					current = new Tag(current, key);
					this.fireEvent(handler, ParsingEvent.START_NODE, MarkupNode.TAG, current.name, current.parent);
				} else if (currentLevel < previousLevel) {
					this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TAG, current.name, current.parent);
					this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.LIST, null, current);
					this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TAG, current.parent.name, current.parent.parent);
					current = new Tag(current.parent, key);
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
			
		}
		for (int i=currentLevel; i>0; i--) {
			this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.TAG, current.name, current.parent);
			if (current.parent != null)
				this.fireEvent(handler, ParsingEvent.END_NODE, MarkupNode.LIST, null, current);
			current = current.parent;
		}
	}

	public static void main(String[] args) throws Exception {
		PmlParser parser = new PmlParser();
		parser.parse(new BufferedReader(new FileReader("/home/thomas/Projects/org.pititom.core/java/org.pititom.core.eclipse.test/META-INF/provider/org.pititom.core.messenger.provider.MessengerProvider/client")), new Handler<ParsingData<MarkupNode>>() {
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
								level++;
								System.out.println(" {");
								break;
							case LIST:
								level++;
								System.out.println(" [");
								break;
							case TAG:
								this.print(data.getValue() + ":");
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
								level--;
								this.println("}");
								break;
							case LIST:
								level--;
								this.println("]");
								break;
							case TAG:
								break;
						}
						break;
				}
			}
			private void print(String text) {
				for (int i=0; i<level; i++) {
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
