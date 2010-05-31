package org.pititom.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface Transmitter<Data extends EventData<?, ?>> {
	public void transmit(Data data);
}
