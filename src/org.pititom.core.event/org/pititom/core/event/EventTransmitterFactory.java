package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public class EventTransmitterFactory {

	public static <Source, Event extends Enum<?>, Data> EventTransmitter<Event, Data> synchronous(Source source, EventHandler<Source, Event, Data> eventHandler, Event... events) {
		return new SynchronousEventTransmitter<Source, Event, Data>(source, eventHandler, events);
	}
	public static <Source, Event extends Enum<?>, Data> RegisterableEventTransmitter<Source, Event, Data> synchronous(Source source) {
		return new SynchronousEventTransmitterMultiHandlers<Source, Event, Data>(source);
	}
	public static <Source, Event extends Enum<?>, Data> EventTransmitter<Event, Data> asynchronous(String threadName, Source source, EventHandler<Source, Event, Data> eventHandler, Event... events) {
		return new AsynchronousEventTransmitter<Source, Event, Data>(threadName, new SynchronousEventTransmitter<Source, Event, Data>(source, eventHandler, events));
	}
	public static <Source, Event extends Enum<?>, Data> RegisterableEventTransmitter<Source, Event, Data> asynchronous(String threadName, Source source) {
		return new AsynchronousEventTransmitterMultiHandler<Source, Event, Data>(threadName, new SynchronousEventTransmitterMultiHandlers<Source, Event, Data>(source));
	}
}
