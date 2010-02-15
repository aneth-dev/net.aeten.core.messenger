package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface Transmitter<Source, Event, Data> {
	public void transmit(Source source, Event event, Data data);
}
