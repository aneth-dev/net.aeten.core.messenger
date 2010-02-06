package org.pititom.core.event;

public interface EventTransmitter<Event extends Enum<?>, Data> {
	public void transmit(Event event, Data data);
}
