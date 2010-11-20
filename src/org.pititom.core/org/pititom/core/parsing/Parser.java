package org.pititom.core.parsing;

import java.io.Reader;

import org.pititom.core.Identifiable;
import org.pititom.core.event.Handler;

/**
 *
 * @author Thomas Pérennou
 */
public interface Parser<NodeType extends Enum<?>> extends Identifiable {
	public void parse(Reader reader, Handler<ParsingData<NodeType>> handler);
}
