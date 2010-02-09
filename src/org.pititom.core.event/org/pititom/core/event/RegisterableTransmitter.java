package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface RegisterableTransmitter<Source, Event, Data> extends Transmitter<Event, Data>, HandlerRegister<Source, Event, Data> {
}
