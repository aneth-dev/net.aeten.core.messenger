package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousTransmitter<Source, Event, Data> implements Transmitter<Event, Data> {

	private final Source source;
	private final AsynchronousForwarder<Source, Event, Data> forwarder;

	public AsynchronousTransmitter(String threadName, Source source, Handler<Source, Event, Data> eventHandler, Event... events) {
		this.source = source;
		this.forwarder = new AsynchronousForwarder<Source, Event, Data>(threadName, new SynchronousForwarder<Source, Event, Data>(eventHandler, events));
	}

	@Override
	public void transmit(Event event, Data data) {
		this.forwarder.forward(this.source, event, data);
	}

}
