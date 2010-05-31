package org.pititom.core.args4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.pititom.core.event.Handler;
import org.pititom.core.parsing.MarkupNode;
import org.pititom.core.parsing.ParsingData;
import org.pititom.core.parsing.service.Parser;

public class Yaml2Args {
	private Yaml2Args() {
	}

	public static String convert(Reader reader, Parser<MarkupNode> parser) {
		final StringBuffer configuration = new StringBuffer();
		
		parser.parse(reader,  new Handler<ParsingData<MarkupNode>>() {
			private int level = 0, previousTagSize = 0;

			public void handleEvent(ParsingData<MarkupNode> data) {

				switch (data.getEvent()) {
					case START_NODE:
						switch (data.getNodeType()) {
							case TEXT:
								if ("false".equals(data.getValue())) {
									configuration.delete(configuration.length() - this.previousTagSize, configuration.length());
								} else if (!"true".equals(data.getValue())) {
									String quote = data.getValue().matches(".*[\\s\\r\\n].*") ? ((level == 0) ? "\"" : "\\\"") : "";
									configuration.append(" " + quote + data.getValue() + quote);
								}
								break;
							case MAP:
							case LIST:
								configuration.append((this.level == 0) ? " \"" : " \\\"");
								this.level++;
								break;
							case TAG:
								String option = " --" + data.getValue().replaceAll(" ", "-");
								this.previousTagSize = option.length();
								configuration.append(option);
								break;
						}
						break;
					case END_NODE:
						switch (data.getNodeType()) {
							case MAP:
							case LIST:
								this.level--;
								configuration.append((this.level == 0) ? "\"" : "\\\"");
								break;
						}
						break;
				}
			}
		});
		return configuration.toString();
	}

	public static String convert(String configuration, Parser<MarkupNode> parser) {
		return convert(new StringReader(configuration), parser);
	}

	public static String convert(File file, Parser<MarkupNode> parser) throws FileNotFoundException {
		return convert(new FileReader(file), parser);
	}


}
