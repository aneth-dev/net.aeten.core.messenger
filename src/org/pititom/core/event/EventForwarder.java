package org.pititom.core.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class EventForwarder<Source, Event extends Enum<?>, Data> implements EventHandler<Source, Event, Data> {
	
	private final Map<Event, Set<EventHandler<Source, Event, Data>>> eventHandlerMap;

	public EventForwarder() {
		this.eventHandlerMap = new HashMap<Event, Set<EventHandler<Source, Event, Data>>>();
	}

	public EventForwarder(Map<Event, Set<EventHandler<Source, Event, Data>>> eventHandlers) {
		this.eventHandlerMap = eventHandlers;
	}

	public void addEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList) {
		synchronized (this.eventHandlerMap) {
			for (Event event : eventList) {
				Set<EventHandler<Source, Event, Data>> set = this.eventHandlerMap.get(event);
				if (set == null) {
					set = new HashSet<EventHandler<Source, Event, Data>>();
					this.eventHandlerMap.put(event, set);
				}
				set.add(eventHandler);
			}
		}
	}
	
	public void removeEventHandler(EventHandler<?, Event, Data> eventHandler, Event... eventList) {
		synchronized (this.eventHandlerMap) {
			for (Event event : eventList) {
				final Set<EventHandler<Source, Event, Data>> set = this.eventHandlerMap.get(event);
				if (set != null) {
					set.remove(eventHandler);
				}
			}
		}
	}
	
	@Override
	public void handleEvent(Source source, Event event, Data data) {
		this.forward(source, event, data);		
	}

	public void forward(Source source, Event event, Data data) {
		for (EventHandler<Source, Event, Data> eventHandler : this.getEventHandlers(event)) {
			eventHandler.handleEvent(source, event, data);
		}
	}

	
	@SuppressWarnings("unchecked")
	private EventHandler<Source, Event, Data>[] getEventHandlers(Event event) {
		synchronized (this.eventHandlerMap) {
			Set<EventHandler<Source, Event, Data>> eventHandlers = this.eventHandlerMap.get(event);
			return eventHandlers == null ? new EventHandler[0] : eventHandlers.toArray(new EventHandler[eventHandlers.size()]);
		}
	}
}
