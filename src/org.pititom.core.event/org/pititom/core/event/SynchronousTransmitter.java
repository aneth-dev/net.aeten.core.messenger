package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class SynchronousTransmitter<Source, Event, Data> implements Transmitter<Event, Data> {

	private final Source source;
	private final SynchronousForwarder<Source, Event, Data> forwarder;

	public SynchronousTransmitter(Source source, Handler<Source, Event, Data> eventHandler, Event... events) {
		this.source = source;
		this.forwarder = new SynchronousForwarder<Source, Event, Data>(eventHandler, events);
	}

	@Override
	public void transmit(Event event, Data data) {
		this.forwarder.forward(this.source, event, data);
	}
}
