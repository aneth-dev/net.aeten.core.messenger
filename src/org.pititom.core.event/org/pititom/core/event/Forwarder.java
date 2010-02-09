package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface Forwarder<Source, Event, Data> {
	public void forward(Source source, Event event, Data data);
}
