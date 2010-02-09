package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class SynchronousForwarder<Source, Event, Data> implements Forwarder<Source, Event, Data> {

	private final Handler<Source, Event, Data> eventHandler;
	private final Event[] events;

	public SynchronousForwarder(Handler<Source, Event, Data> eventHandler, Event... events) {
		this.eventHandler = eventHandler;
		this.events = events;
	}

	public void forward(Source source, Event event, Data data) {
		for (Event registredEvent : this.events) {
			if (registredEvent == event) {
				eventHandler.handleEvent(source, event, data);
			}
		}
	}
}
