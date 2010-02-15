package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousTransmitterMultiHandlers<Source, Event, Data> extends AsynchronousTransmitter<Source, Event, Data> implements RegisterableTransmitter<Source, Event, Data> {

	private final RegisterableTransmitter<Source, Event, Data> transmitter;

	public AsynchronousTransmitterMultiHandlers(String threadName, SynchronousTransmitterMultiHandlers<Source, Event, Data> transmitter) {
		super(threadName, transmitter);
		this.transmitter = transmitter;
	}
	public AsynchronousTransmitterMultiHandlers(String threadName) {
		this(threadName, new SynchronousTransmitterMultiHandlers<Source, Event, Data>());
	}

	@Override
	public void addEventHandler(Handler<Source, Event, Data> eventHandler, Event... eventList) {
		this.transmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Source, Event, Data> eventHandler, Event... eventList) {
		this.transmitter.removeEventHandler(eventHandler, eventList);
	}
}
