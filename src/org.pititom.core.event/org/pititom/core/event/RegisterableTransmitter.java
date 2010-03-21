package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface RegisterableTransmitter<Source, Event, Data extends EventData<Source, Event>> extends Transmitter<Data>, HandlerRegister<Source, Event, Data> {
}
