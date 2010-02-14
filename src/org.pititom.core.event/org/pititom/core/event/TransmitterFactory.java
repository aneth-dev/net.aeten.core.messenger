package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public final class TransmitterFactory {

<<<<<<< HEAD:src/org.pititom.core.event/org/pititom/core/event/TransmitterFactory.java
	public static final <Source, Event, Data> Transmitter<Source, Event, Data> synchronous(Handler<Source, Event, Data> eventHandler, Event... events) {
		return new SynchronousTransmitter<Source, Event, Data>(eventHandler, events);
	}
	public static final <Source, Data> Transmitter<Source, Default, Data> synchronous(Handler<Source, Default, Data> eventHandler) {
		return new SynchronousTransmitter<Source, Default, Data>(eventHandler, Default.SINGLE_EVENT);
=======
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
>>>>>>> 9b73eb501070e29128831137d20b5558c8631ae2:src/org.pititom.core.event/org/pititom/core/event/TransmitterFactory.java
	}
	public static final <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> synchronous() {
		return new SynchronousTransmitterMultiHandlers<Source, Event, Data>();
	}
<<<<<<< HEAD:src/org.pititom.core.event/org/pititom/core/event/TransmitterFactory.java
	public static final <Source, Event, Data> Transmitter<Source, Event, Data> asynchronous(String threadName, Handler<Source, Event, Data> eventHandler, Event... events) {
		return new AsynchronousTransmitter<Source, Event, Data>(threadName, eventHandler, events);
	}
	public static final <Source, Data> Transmitter<Source, Default, Data> asynchronous(String threadName, Handler<Source, Default, Data> eventHandler) {
		return new AsynchronousTransmitter<Source, Default, Data>(threadName, eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> asynchronous(String threadName) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName);
=======
	public static <Source, Data> Transmitter<Default, Data> asynchronous(String threadName, Source source, Handler<Source, Default, Data> eventHandler) {
		return new AsynchronousTransmitter<Source, Default, Data>(threadName, source, eventHandler, Default.SINGLE_EVENT);
	}
	public static <Data> Transmitter<Default, Data> asynchronous(String threadName, Handler<Default, Default, Data> eventHandler) {
		return new AsynchronousTransmitter<Default, Default, Data>(threadName, Default.ANONYMOUS_SOURCE, eventHandler, Default.SINGLE_EVENT);
	}
	public static <Source, Event, Data> RegisterableTransmitter<Source, Event, Data> asynchronous(String threadName, Source source) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName, source);
>>>>>>> 9b73eb501070e29128831137d20b5558c8631ae2:src/org.pititom.core.event/org/pititom/core/event/TransmitterFactory.java
	}
}
