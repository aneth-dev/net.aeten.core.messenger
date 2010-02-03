package org.pititom.core.extersion;


public interface EventPerformer<Source, Event extends Enum<?>, Data> {
	public void addEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList);
	public void removeEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList);
}
