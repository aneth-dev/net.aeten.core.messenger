package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousForwarderMultiHandlers<Source, Event extends Enum<?>, Data> extends AsynchronousForwarder<Source, Event, Data> implements RegisterableForwarder<Source, Event, Data> {

	private final RegisterableForwarder<Source, Event, Data> forwarder;

	public AsynchronousForwarderMultiHandlers(String threadName, SynchronousForwarderMultiHandlers<Source, Event, Data> forwarder) {
		super(threadName, forwarder);
		this.forwarder = forwarder;
	}
	public AsynchronousForwarderMultiHandlers(String threadName) {
		this(threadName, new SynchronousForwarderMultiHandlers());
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
