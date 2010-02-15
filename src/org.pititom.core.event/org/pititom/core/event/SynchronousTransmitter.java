package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class SynchronousTransmitter<Source, Event, Data> implements Transmitter<Source, Event, Data> {

	private final Handler<Source, Event, Data> eventHandler;
	private final Event[] events;

	public SynchronousTransmitter(Handler<Source, Event, Data> eventHandler, Event... events) {
		this.eventHandler = eventHandler;
		this.events = events;
	}

	public void transmit(Source source, Event event, Data data) {
		for (Event registredEvent : this.events) {
			if (registredEvent == event) {
				eventHandler.handleEvent(source, event, data);
			}
		}
	}
}
