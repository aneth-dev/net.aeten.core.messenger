package org.pititom.core.event.signal;

import java.util.Collection;

import org.pititom.core.event.ForwarderFactory;
import org.pititom.core.event.RegisterableForwarder;
import org.pititom.core.event.signal.service.Slot;

public class Signal<Data> {
	private static RegisterableForwarder<Object, Event<?>, Object> FORWARDER = ForwarderFactory.asynchronous("Event loop");

	private final Object source;
	private final Event<Data> event;

	public Signal(Object source, Event<Data> event) {
		this.source = source;
		this.event = event;

		Collection<Slot<Object, Event<?>, Object>> registredSlots = Register.SLOTS_MAP.get(this.event);
		if (registredSlots != null) {
			for (Slot<Object, Event<?>, Object> slot : registredSlots) {
				FORWARDER.addEventHandler(slot, this.event);
			}
		}
	}

	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return the event
	 */
	public Event<Data> getEvent() {
		return event;
	}

	/**
	 * Emit signal with null data
	 */
	public void emit() {
		this.emit(null);
	}

	/**
	 * Emit signal
	 * 
	 * @param data
	 */
	public void emit(Data data) {
		FORWARDER.forward(this.source, this.event, data);
	}

}
