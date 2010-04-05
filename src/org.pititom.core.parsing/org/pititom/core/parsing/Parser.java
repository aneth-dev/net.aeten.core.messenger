package org.pititom.core.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import org.pititom.core.Service;
import org.pititom.core.event.Handler;

/**
 *
 * @author Thomas Pérennou
 */
public class Parser {
	private Parser() {}

	public static <NodeType extends Enum<?>> void parse(String parserIdentifier, InputStream input, Handler<ParsingData<NodeType>> handler) {
		parse(parserIdentifier, new InputStreamReader(input), handler);
	}
	public static <NodeType extends Enum<?>> void parse(String parserIdentifier, String input, Handler<ParsingData<NodeType>> handler) {
		parse(parserIdentifier, new StringReader(input), handler);
	}
	public static <NodeType extends Enum<?>> void parse(String parserIdentifier, File input, Handler<ParsingData<NodeType>> handler) throws FileNotFoundException {
		parse(parserIdentifier, new FileReader(input), handler);
	}
	public static <NodeType extends Enum<?>> void parse(String parserIdentifier, Reader input, Handler<ParsingData<NodeType>> handler) {
		org.pititom.core.parsing.service.Parser<NodeType> parser;
		parser = Service.getProvider(org.pititom.core.parsing.service.Parser.class, parserIdentifier);
		parser.parse(new BufferedReader(input), handler);
	}
}
