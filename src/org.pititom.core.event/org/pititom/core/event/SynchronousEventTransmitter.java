package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class SynchronousEventTransmitter<Source, Event extends Enum<?>, Data> implements EventTransmitter<Event, Data> {

	private final Source source;
	private final EventHandler<Source, Event, Data> eventHandler;
	private final Event[] events;

	public SynchronousEventTransmitter(Source source, EventHandler<Source, Event, Data> eventHandler, Event... events) {
		this.source = source;
		this.eventHandler = eventHandler;
		this.events = events;
	}

	@Override
	public void transmit(Event event, Data data) {
		for (Event registredEvent : this.events) {
			if (registredEvent == event) {
				eventHandler.handleEvent(this.source, event, data);
			}
		}
	}
}
