package net.aeten.core.args4j;

import java.io.Reader;

import net.aeten.core.Format;
import net.aeten.core.event.Handler;
import net.aeten.core.parsing.MarkupConverter;
import net.aeten.core.parsing.MarkupNode;
import net.aeten.core.parsing.Parser;
import net.aeten.core.parsing.ParsingData;
import net.aeten.core.service.Provider;

@Provider(MarkupConverter.class)
@Format("args")
public class Markup2Args4j implements MarkupConverter<String> {

	public String convert(Reader reader, Parser<MarkupNode> parser) {
		final StringBuffer configuration = new StringBuffer();

		parser.parse(reader, new Handler<ParsingData<MarkupNode>>() {
			private int level, previousTagSize;

			public void handleEvent(ParsingData<MarkupNode> data) {

				switch (data.getEvent()) {
					case START_NODE:
						switch (data.getNodeType()) {
							case DOCUMENT:
								level = 0;
								previousTagSize = 0;
								break;
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
								switch (level) {
									case 0:
										break;
									case 1:
										configuration.append(" \"");
										break;
									default:
										configuration.append("\\\"");
										break;
								}
								break;
							case TAG:
								this.level++;
								String option = " --" + data.getValue().replace(' ', '-');
								this.previousTagSize = option.length();
								configuration.append(option);
								break;
							case ANCHOR:
							case REFERENCE:
							case TYPE:
							default:
								break;
						}
						break;
					case END_NODE:
						switch (data.getNodeType()) {
							case TAG:
								this.level--;
								break;
							case MAP:
							case LIST:
								switch (level) {
									case 0:
										break;
									case 1:
										configuration.append("\"");
										break;

									default:
										configuration.append("\\\"");
										break;
								}

								break;
							case DOCUMENT:
							case ANCHOR:
							case REFERENCE:
							case TEXT:
							case TYPE:
							default:
								break;
						}
						break;
				}
			}
		});
		return configuration.toString();
	}

	public String getIdentifier() {
		return Markup2Args4j.class.getName();
	}

}
