package org.pititom.core.event.signal.service;

import org.pititom.core.event.Handler;

public interface Slot<Source, Event, Data> extends Handler<Source, Event, Data> {
	public Event[] getEvents();
}
