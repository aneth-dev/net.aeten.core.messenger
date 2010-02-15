package org.pititom.core.event.signal;


public class SignalGroup<Event extends Enum<?>, Data> {
	private final Signal<Data>[] signals;
	
	@SuppressWarnings("unchecked")
	public SignalGroup(Object source, Class<Event> enumClass) {
		Event[] events;
		try {
			events = (Event[]) enumClass.getDeclaredMethod("values").invoke(null);
		} catch (Exception exception) {
			// This should not append
			events = (Event[]) new Enum<?>[0];
		}
		this.signals = new Signal[events.length];
		for (int i=0; i<events.length; i++) {
			this.signals[i] = new Signal<Data>(source, events[i]);
		}
	}
	
	/**
	 * Emit signal asynchronously in event loop with null data 
	 */
	public void emit(Event event) {
		this.emit(event, null);
	}

	/**
	 * Emit signal asynchronously in event loop
	 * 
	 * @param data
	 */
	public void emit(Event event, Data data) {
		this.signals[event.ordinal()].emit(data);
	}
	
	/**
	 * Emit signal synchronously with null data
	 * 
	 * @param data
	 */
	public void emitSync(Event event) {
		this.emitSync(event, null);
	}

	/**
	 * Emit signal synchronously
	 * 
	 * @param data
	 */
	public void emitSync(Event event, Data data) {
		this.signals[event.ordinal()].emitSync(data);
	}

}
