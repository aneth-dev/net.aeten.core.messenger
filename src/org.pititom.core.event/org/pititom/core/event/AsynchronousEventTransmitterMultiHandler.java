package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousEventTransmitterMultiHandler<Source, Event extends Enum<?>, Data> extends AsynchronousEventTransmitter<Source, Event, Data> implements RegisterableEventTransmitter<Source, Event, Data>, Runnable {

	private final RegisterableEventTransmitter<Source, Event, Data> eventTransmitter;

	public AsynchronousEventTransmitterMultiHandler(String threadName, RegisterableEventTransmitter<Source, Event, Data> eventTransmitter) {
		super(threadName, eventTransmitter);
		this.eventTransmitter = eventTransmitter;
	}

	public void addEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList) {
		this.eventTransmitter.addEventHandler(eventHandler, eventList);
	}

	public void removeEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList) {
		this.eventTransmitter.removeEventHandler(eventHandler, eventList);
	}

}
