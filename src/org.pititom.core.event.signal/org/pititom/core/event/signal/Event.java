package org.pititom.core.event.signal;

/**
 *
 * @author Thomas Pérennou
 */
public interface Event<Data> {
	Class<Data> getDataClass();
}
