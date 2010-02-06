package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface EventHandlerRegister<Source, Event extends Enum<?>, Data> {
	public void addEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList);
	public void removeEventHandler(EventHandler<Source, Event, Data> eventHandler, Event... eventList);
}
