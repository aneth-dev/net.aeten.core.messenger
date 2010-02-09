package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface Handler<Source, Event, Data> {
	public void handleEvent(Source source, Event event, Data data);
}
