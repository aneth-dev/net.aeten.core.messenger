package org.pititom.core.event.service;

import org.pititom.core.Descrivable;
import org.pititom.core.Identifiable;
import org.pititom.core.event.EventData;
import org.pititom.core.event.RegisterableTransmitter;

public interface TransmitterService<Source, Event, Data extends EventData<Source, Event>> extends
		RegisterableTransmitter<Source, Event, Data>, Identifiable, Descrivable {
}
