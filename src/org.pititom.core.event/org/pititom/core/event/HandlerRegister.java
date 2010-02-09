package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface HandlerRegister<Source, Event, Data> {
	public void addEventHandler(Handler<Source, Event, Data> eventHandler, Event... eventList);
	public void removeEventHandler(Handler<Source, Event, Data> eventHandler, Event... eventList);
}
