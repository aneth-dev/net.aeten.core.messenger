package org.pititom.core.event;

import java.util.concurrent.Future;

/**
 *
 * @author Thomas Pérennou
 */
public interface Transmitter<Data extends EventData<?, ?>> {
	public Future<Data> transmit(Data data);
}
