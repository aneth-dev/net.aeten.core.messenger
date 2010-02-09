package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousTransmitterMultiHandlers<Source, Event, Data> implements RegisterableTransmitter<Source, Event, Data> {

	private final Source source;
	private final AsynchronousForwarderMultiHandlers<Source, Event, Data> forwarder;

	public AsynchronousTransmitterMultiHandlers(String threadName, Source source) {
		this.source = source;
		this.forwarder = new AsynchronousForwarderMultiHandlers<Source, Event, Data>(threadName, new SynchronousForwarderMultiHandlers<Source, Event, Data>());
	}

	@Override
	public void transmit(Event event, Data data) {
		this.forwarder.forward(this.source, event, data);
	}

	@Override
	public void addEventHandler(Handler<Source, Event, Data> eventHandler, Event... eventList) {
		this.forwarder.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Source, Event, Data> eventHandler, Event... eventList) {
		this.forwarder.removeEventHandler(eventHandler, eventList);
	}

}
