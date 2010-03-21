package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousTransmitterMultiHandlers<Source, Event, Data extends EventData<Source, Event>> extends AsynchronousTransmitter<Source, Event, Data> implements RegisterableTransmitter<Source, Event, Data> {

	private final RegisterableTransmitter<Source, Event, Data> transmitter;

	public AsynchronousTransmitterMultiHandlers(String threadName, RegisterableTransmitter<Source, Event, Data> transmitter, int threadPriority) {
		super(threadName, transmitter, threadPriority);
		this.transmitter = transmitter;
	}
	
	public AsynchronousTransmitterMultiHandlers(String threadName, RegisterableTransmitter<Source, Event, Data> transmitter) {
		this(threadName, transmitter, Thread.NORM_PRIORITY);
	}
	public AsynchronousTransmitterMultiHandlers(String threadName, int threadPriority) {
		this(threadName, new SynchronousTransmitterMultiHandlers<Source, Event, Data>(), threadPriority);
	}

	public AsynchronousTransmitterMultiHandlers(String threadName) {
		this(threadName, new SynchronousTransmitterMultiHandlers<Source, Event, Data>(), Thread.NORM_PRIORITY);
	}

	@Override
	public void addEventHandler(Handler<Data> eventHandler, Event... eventList) {
		this.transmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Data> eventHandler, Event... eventList) {
		this.transmitter.removeEventHandler(eventHandler, eventList);
	}
}
