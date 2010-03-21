package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface HandlerRegister<Source, Event, Data extends EventData<Source, Event>> {
	public void addEventHandler(Handler<Data> eventHandler, Event... eventList);
	public void removeEventHandler(Handler<Data> eventHandler, Event... eventList);
}
