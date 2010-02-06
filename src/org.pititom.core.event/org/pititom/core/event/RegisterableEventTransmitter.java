package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface RegisterableEventTransmitter<Source, Event extends Enum<?>, Data> extends EventTransmitter<Event, Data>, EventHandlerRegister<Source, Event, Data> {
}
