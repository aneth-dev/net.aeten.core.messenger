package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public class TransmitterFactory {

	public static <Source, Event extends Enum<?>, Data> Transmitter<Event, Data> synchronous(Source source, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new SynchronousTransmitter<Source, Event, Data>(source, eventHandler, events);
	}
	public static <Source, Event extends Enum<?>, Data> RegisterableTransmitter<Source, Event, Data> synchronous(Source source) {
		return new SynchronousTransmitterMultiHandlers<Source, Event, Data>(source);
	}
	public static <Source, Event extends Enum<?>, Data> Transmitter<Event, Data> asynchronous(String threadName, Source source, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new AsynchronousTransmitter<Source, Event, Data>(threadName, source, eventHandler, events);
	}
	public static <Source, Event extends Enum<?>, Data> RegisterableTransmitter<Source, Event, Data> asynchronous(String threadName, Source source) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName, source);
	}
}
