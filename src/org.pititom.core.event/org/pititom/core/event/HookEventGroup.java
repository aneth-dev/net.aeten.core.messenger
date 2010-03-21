package org.pititom.core.event;

import java.util.Arrays;

import org.pititom.core.event.HookEvent.Hook;

public class HookEventGroup<Event extends Enum<?>> {
	private HookEvent<Event>[] hookEvents;
	
	public HookEventGroup(Class<Event> eventClass) {
		super();
		try {
			Event[] events = (Event[]) eventClass.getMethod("values").invoke(
					null);
			this.hookEvents = new HookEvent[events.length * (Hook.values().length + 1)];
			for (int hookIndex = 0, eventIndex = 0; hookIndex < this.hookEvents.length; hookIndex++, eventIndex++) {
				for (Hook hook : Hook.values()) {
					this.hookEvents[hookIndex] = HookEvent.get(events[eventIndex], hook);
					hookIndex++;
				}
				this.hookEvents[hookIndex] = HookEvent.get(events[eventIndex]);
			}
		} catch (Exception exception) {
			// This should not append
			this.hookEvents =  new HookEvent[0];
		}
	}

	public HookEvent<Event> getEvent(Event event, Hook hook) {
		return this.hookEvents[(event.ordinal() * (Hook.values().length + 1)) + ((hook == null) ? Hook.values().length : hook.ordinal())];
	}

	public HookEvent<Event> getPreEvent(Event event) {
		return this.hookEvents[(event.ordinal() * (Hook.values().length + 1)) + Hook.PRE.ordinal()];
	}
	
	public HookEvent<Event> getPostEvent(Event event) {
		return this.hookEvents[(event.ordinal() * (Hook.values().length + 1)) + Hook.POST.ordinal()];
	}

	public HookEvent<Event> getEvent(Event event) {
		return this.hookEvents[(event.ordinal() * (Hook.values().length + 1)) + Hook.values().length];
	}

	public HookEvent<Event>[] values() {
		return Arrays.copyOf(this.hookEvents, this.hookEvents.length);
	}
}
