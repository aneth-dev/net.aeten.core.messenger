package org.pititom.core.event;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingTransmitter;


/**
 *
 * @author Thomas Pérennou
 */
class SynchronousTransmitterMultiHandlers<Source, Event, Data extends EventData<Source, Event>> implements RegisterableTransmitter<Source, Event, Data> {
	
	private final Map<Event, Set<Handler<Data>>> eventHandlerMap;

	public SynchronousTransmitterMultiHandlers() {
		this.eventHandlerMap = new LinkedHashMap<Event, Set<Handler<Data>>>();
	}

	@Override
	public void addEventHandler(Handler<Data> eventHandler, Event... eventList) {
		synchronized (this.eventHandlerMap) {
			for (Event event : eventList) {
				Set<Handler<Data>> set = this.eventHandlerMap.get(event);
				if (set == null) {
					set = new HashSet<Handler<Data>>();
					this.eventHandlerMap.put(event, set);
				}
				set.add(eventHandler);
			}
		}
	}
	
	@Override
	public void removeEventHandler(Handler<Data> eventHandler, Event... eventList) {
		synchronized (this.eventHandlerMap) {
			for (Event event : eventList) {
				final Set<Handler<Data>> set = this.eventHandlerMap.get(event);
				if (set != null) {
					set.remove(eventHandler);
				}
			}
		}
	}
	

	@Override
	public void transmit(Data data) {
		for (Handler<Data> eventHandler : this.getEventHandlers(data.getEvent())) {
			try {
				eventHandler.handleEvent(data);
			} catch (Exception exception) {
				LoggingTransmitter.getInstance().transmit(new LoggingData(eventHandler, LoggingEvent.ERROR, exception));
			}
		}
	}
	
	private Handler<Data>[] getEventHandlers(Event event) {
		synchronized (this.eventHandlerMap) {
			Set<Handler<Data>> eventHandlers = this.eventHandlerMap.get(event);
			return eventHandlers == null ? new Handler[0] : eventHandlers.toArray(new Handler[eventHandlers.size()]);
		}
	}
}
