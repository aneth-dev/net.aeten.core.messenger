package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface Transmitter<Event, Data> {
	public void transmit(Event event, Data data);
}
