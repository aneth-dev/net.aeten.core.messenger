package org.pititom.core.event.signal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pititom.core.Service;
import org.pititom.core.event.signal.service.Slot;

class Register {
	public static final Map<Object, Collection<Slot<Object, Event<?>, Object>>> SLOTS_MAP;

	static {
		SLOTS_MAP = new HashMap<Object, Collection<Slot<Object, Event<?>, Object>>>();

		for (Slot<Object, Event<?>, Object> slot : Service.getProviders(Slot.class)) {
			for (Event<?> event : slot.getEvents()) {
				Register.addEvent(event);
			}
			Register.addSlot(slot);
		}
	}

	private static void addEvent(Event<?>... events) {
		for (Object event : events) {
			SLOTS_MAP.put(event, new ArrayList<Slot<Object, Event<?>, Object>>());
		}
	}

	private static void addSlot(Slot<Object, Event<?>, Object> slot) {
		for (Object event : slot.getEvents()) {
			Register.SLOTS_MAP.get(event).add(slot);
		}
	}

}
