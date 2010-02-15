package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface RegisterableTransmitter<Source, Event, Data> extends Transmitter<Source, Event, Data>, HandlerRegister<Source, Event, Data> {
}
