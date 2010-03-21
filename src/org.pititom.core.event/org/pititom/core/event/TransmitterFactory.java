package org.pititom.core.event;


/**
 *
 * @author Thomas Pérennou
 */
public final class TransmitterFactory {

	private TransmitterFactory() {}
	
	public static final <Event, Data extends EventData<?, Event>> Transmitter<Data> synchronous(Handler<Data> eventHandler, Event... events) {
		return new SynchronousTransmitter<Event, Data>(eventHandler, events);
	}
	public static final <Data extends EventData<?, Default>> Transmitter<Data> synchronous(Handler<Data> eventHandler) {
		return new SynchronousTransmitter<Default, Data>(eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> RegisterableTransmitter<Source, Event, Data> synchronous() {
		return new SynchronousTransmitterMultiHandlers<Source, Event, Data>();
	}
	public static final <Source, Event extends Enum<?>, Data extends HookEventData<Source, Event>> RegisterableTransmitter<Source, HookEvent<Event>, Data> synchronous(Class<Event> eventClass) {
		return new HookTransmitter<Source, Event, Data>(eventClass);
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> Transmitter<Data> asynchronous(final String threadName, final Handler<Data> eventHandler, final Event... events) {
		return new AsynchronousTransmitter<Source, Event, Data>(threadName, eventHandler, events);
	}
	public static final <Source, Data extends EventData<Source, Default>> Transmitter<Data> asynchronous(final String threadName, final Handler<Data> eventHandler) {
		return new AsynchronousTransmitter<Source, Default, Data>(threadName, eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> RegisterableTransmitter<Source, Event, Data> asynchronous(final String threadName) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName);
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> RegisterableTransmitter<Source, Event, Data> asynchronous(final String threadName, final RegisterableTransmitter<Source, Event, Data> transmitter) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName, transmitter);
	}
	public static final <Source, Event extends Enum<?>, Data extends HookEventData<Source, Event>> RegisterableTransmitter<Source, HookEvent<Event>, Data> asynchronous(final String threadName, Class<Event> eventClass) {
		return new AsynchronousTransmitterMultiHandlers<Source, HookEvent<Event>, Data>(threadName, new HookTransmitter<Source, Event, Data>(eventClass));
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> Transmitter<Data> asynchronous(final String threadName, int threadPriority, final Handler<Data> eventHandler, final Event... events) {
		return new AsynchronousTransmitter<Source, Event, Data>(threadName, threadPriority, eventHandler, events);
	}
	public static final <Source, Data extends EventData<Source, Default>> Transmitter<Data> asynchronous(final String threadName, int threadPriority, final Handler<Data> eventHandler) {
		return new AsynchronousTransmitter<Source, Default, Data>(threadName, threadPriority, eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> RegisterableTransmitter<Source, Event, Data> asynchronous(final String threadName, int threadPriority) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName, threadPriority);
	}
	public static final <Source, Event, Data extends EventData<Source, Event>> RegisterableTransmitter<Source, Event, Data> asynchronous(final String threadName, int threadPriority, final RegisterableTransmitter<Source, Event, Data> transmitter) {
		return new AsynchronousTransmitterMultiHandlers<Source, Event, Data>(threadName, transmitter, threadPriority);
	}
	public static final <Source, Event extends Enum<?>, Data extends HookEventData<Source, Event>> RegisterableTransmitter<Source, HookEvent<Event>, Data> asynchronous(final String threadName, int threadPriority, Class<Event> eventClass) {
		return new AsynchronousTransmitterMultiHandlers<Source, HookEvent<Event>, Data>(threadName, new HookTransmitter<Source, Event, Data>(eventClass), threadPriority);
	}
	
}
