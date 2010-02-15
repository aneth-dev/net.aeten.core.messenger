package org.pititom.core.logging;

import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;

/**
 *
 * @author Thomas Pérennou
 */
public final class LoggingTransmitter implements RegisterableTransmitter<Object, LoggingEvent, LoggingData> {

	private final RegisterableTransmitter<Object, LoggingEvent, LoggingData> transmitter;
	private final static LoggingTransmitter INSTANCE;

	static {
		INSTANCE = new LoggingTransmitter();
	}

	private LoggingTransmitter() {
		this.transmitter = TransmitterFactory.asynchronous("Logging transmitter");
	}

	public static LoggingTransmitter getInstance() {
		return INSTANCE;
	}

	@Override
	public void transmit(Object source, LoggingEvent event, LoggingData data) {
		this.transmitter.transmit(source, event, data);
	}

	@Override
	public void addEventHandler(Handler<Object, LoggingEvent, LoggingData> eventHandler, LoggingEvent... eventList) {
		this.transmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Object, LoggingEvent, LoggingData> eventHandler, LoggingEvent... eventList) {
		this.transmitter.removeEventHandler(eventHandler, eventList);
	}
}
