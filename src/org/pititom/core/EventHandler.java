package org.pititom.core;

/**
 *
 * @author Thomas Pérennou
 */
public interface EventHandler<Source, Event extends Enum<?>, Data> {
	public void handleEvent(Source source, Event event, Data data);
}
