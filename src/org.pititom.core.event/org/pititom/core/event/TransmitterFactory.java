package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public class TransmitterFactory {

	private TransmitterFactory() {}

	public static <Source, Event, Data> Transmitter<Event, Data> synchronous(Source source, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new SynchronousTransmitter<Source, Event, Data>(source, eventHandler, events);
	}
	public static <Source, Data> Transmitter<Default, Data> synchronous(Source source, Handler<Source, Default, Data> eventHandler) {
		return new SynchronousTransmitter<Source, Default, Data>(source, eventHandler, Default.SINGLE_EVENT);
	}
	public static <Data> Transmitter<Default, Data> synchronous(Handler<Default, Default, Data> eventHandler) {
		return new SynchronousTransmitter<Default, Default, Data>(Default.ANONYMOUS_SOURCE, eventHandler, Default.SINGLE_EVENT);
	}
	public static <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> synchronous(Source source) {
		return new SynchronousTransmitterMultiHandlers<Source, Event, Data>(source);
	}
	public static <Source, Event, Data> Transmitter<Event, Data> asynchronous(String threadName, Source source, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new AsynchronousTransmitter<Source, Event, Data>(threadName, source, eventHandler, events);
	}
	public static <Source, Data> Transmitter<Default, Data> asynchronous(String threadName, Source source, Handler<Source, Default, Data> eventHandler) {
		return new AsynchronousTransmitter<Source, Default, Data>(threadName, source, eventHandler, Default.SINGLE_EVENT);
	}
	public static <Data> Transmitter<Default, Data> asynchronous(String threadName, Handler<Default, Default, Data> eventHandler) {
		return new AsynchronousTransmitter<Default, Default, Data>(threadName, Default.ANONYMOUS_SOURCE, eventHandler, Default.SINGLE_EVENT);
	}
	public static <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> asynchronous(String threadName, Source source) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName, source);
	}
}
