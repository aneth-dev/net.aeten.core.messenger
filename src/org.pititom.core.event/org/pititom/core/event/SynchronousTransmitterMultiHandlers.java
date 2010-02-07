package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class SynchronousTransmitterMultiHandlers<Source, Event extends Enum<?>, Data> implements RegisterableTransmitter<Source, Event, Data> {

	private final Source source;
	private final SynchronousForwarderMultiHandlers<Source, Event, Data> forwarder;

	public SynchronousTransmitterMultiHandlers(Source source) {
		this.source = source;
		this.forwarder = new SynchronousForwarderMultiHandlers<Source, Event, Data>();
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
