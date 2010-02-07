package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface RegisterableForwarder<Source, Event extends Enum<?>, Data> extends Forwarder<Source, Event, Data>, HandlerRegister<Source, Event, Data> {
}
