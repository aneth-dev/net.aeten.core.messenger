package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public class ForwarderFactory {

	public static <Source, Event extends Enum<?>, Data> Forwarder<Source, Event, Data> synchronous(Handler<Source, Event, Data> eventHandler, Event... events) {
		return new SynchronousForwarder<Source, Event, Data>(eventHandler, events);
	}
	public static <Source, Event extends Enum<?>, Data> RegisterableForwarder<Source, Event, Data> synchronous() {
		return new SynchronousForwarderMultiHandlers<Source, Event, Data>();
	}
	public static <Source, Event extends Enum<?>, Data> Forwarder<Source, Event, Data> asynchronous(String threadName, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new AsynchronousForwarder<Source, Event, Data>(threadName, eventHandler, events);
	}
	public static <Source, Event extends Enum<?>, Data> RegisterableForwarder<Source, Event, Data> asynchronous(String threadName) {
		return new AsynchronousForwarderMultiHandlers<Source, Event, Data>(threadName);
	}
}
