package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface Handler<Source, Event extends Enum<?>, Data> {
	public void handleEvent(Source source, Event event, Data data);
}
