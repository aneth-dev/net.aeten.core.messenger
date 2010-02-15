package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public final class TransmitterFactory {

	public static final <Source, Event, Data> Transmitter<Source, Event, Data> synchronous(Handler<Source, Event, Data> eventHandler, Event... events) {
		return new SynchronousTransmitter<Source, Event, Data>(eventHandler, events);
	}
	public static final <Source, Data> Transmitter<Source, Default, Data> synchronous(Handler<Source, Default, Data> eventHandler) {
		return new SynchronousTransmitter<Source, Default, Data>(eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> synchronous() {
		return new SynchronousTransmitterMultiHandlers<Source, Event, Data>();
	}
	public static final <Source, Event, Data> Transmitter<Source, Event, Data> asynchronous(String threadName, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new AsynchronousTransmitter<Source, Event, Data>(threadName, eventHandler, events);
	}
	public static final <Source, Data> Transmitter<Source, Default, Data> asynchronous(String threadName, Handler<Source, Default, Data> eventHandler) {
		return new AsynchronousTransmitter<Source, Default, Data>(threadName, eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> asynchronous(String threadName) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName);
	}
}
