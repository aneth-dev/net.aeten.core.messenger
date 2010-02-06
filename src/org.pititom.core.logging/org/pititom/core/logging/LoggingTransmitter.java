package org.pititom.core.logging;

import org.pititom.core.event.EventHandler;
import org.pititom.core.event.EventTransmitterFactory;
import org.pititom.core.event.RegisterableEventTransmitter;

/**
 *
 * @author Thomas Pérennou
 */
public class LoggingTransmitter implements RegisterableEventTransmitter<Object, LoggingEvent, LoggingData> {

	private final RegisterableEventTransmitter<Object, LoggingEvent, LoggingData> logTransmitter;
	private final static LoggingTransmitter INSTANCE;

	static {
		INSTANCE = new LoggingTransmitter();
	}

	private LoggingTransmitter() {
		this.logTransmitter = EventTransmitterFactory.asynchronous("Logging transmitter", (Object) this);
	}

	public static LoggingTransmitter getInstance() {
		return INSTANCE;
	}

	@Override
	public void transmit(LoggingEvent event, LoggingData data) {
		this.logTransmitter.transmit(event, data);
	}

	@Override
	public void addEventHandler(EventHandler<Object, LoggingEvent, LoggingData> eventHandler, LoggingEvent... eventList) {
		this.logTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(EventHandler<Object, LoggingEvent, LoggingData> eventHandler, LoggingEvent... eventList) {
		this.logTransmitter.removeEventHandler(eventHandler, eventList);
	}
}
