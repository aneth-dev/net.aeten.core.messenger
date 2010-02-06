package org.pititom.core.event;

public interface RegisterableEventTransmitter<Source, Event extends Enum<?>, Data> extends EventTransmitter<Event, Data>, EventHandlerRegister<Source, Event, Data> {
}
