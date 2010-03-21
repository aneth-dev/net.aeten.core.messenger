package org.pititom.core.event;

import org.pititom.core.event.HookEvent.Hook;

class HookTransmitter<Source, Event extends Enum<?>, Data extends HookEventData<Source, Event>>
		implements
		RegisterableTransmitter<Source, HookEvent<Event>, Data> {

	private HookEventGroup<Event> events;
	private RegisterableTransmitter<Source, HookEvent<Event>, Data> hookTransmitter;

	public HookTransmitter(Class<Event> eventClass) {
		this.events = new HookEventGroup<Event>(eventClass);
		this.hookTransmitter = TransmitterFactory.synchronous();
	}

	public void transmit(Data eventData) {
		Event event = eventData.getEvent().getSourceEvent();
		eventData.setEvent(this.events.getEvent(event, Hook.PRE));
		this.hookTransmitter.transmit(eventData);
		if (eventData.doIt()) {
			eventData.setEvent(this.events.getEvent(event));
			this.hookTransmitter
					.transmit(eventData);
			eventData.setEvent(this.events.getEvent(event, Hook.POST));
			this.hookTransmitter
					.transmit(eventData);
		}
	}

	@Override
	public void addEventHandler(
			Handler<Data> eventHandler,
			HookEvent<Event>... eventList) {
		for (HookEvent<Event> event : eventList) {
			this.hookTransmitter.addEventHandler(eventHandler, this.events
					.getEvent(event.getSourceEvent(), event.getHook()));
		}
	}

	@Override
	public void removeEventHandler(
			Handler<Data> eventHandler,
			HookEvent<Event>... eventList) {
		for (HookEvent<Event> event : eventList) {
			this.hookTransmitter.removeEventHandler(eventHandler, this.events
					.getEvent(event.getSourceEvent(), event.getHook()));
		}
	}

}
