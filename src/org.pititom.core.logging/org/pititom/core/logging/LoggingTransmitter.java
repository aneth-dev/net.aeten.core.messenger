package org.pititom.core.logging;

import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;

/**
 *
 * @author Thomas Pérennou
 */
public final class LoggingTransmitter implements RegisterableTransmitter<Object, LoggingEvent, LoggingData> {

	private final RegisterableTransmitter<?, LoggingEvent, LoggingData> transmitter;
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
	public void transmit(LoggingData data) {
		this.transmitter.transmit(data);
	}

	public void transmit(Object source, LoggingEvent level, String message, Exception exception) {
		this.transmitter.transmit(new LoggingData(source, level, message, exception));
	}

	public void transmit(Object source, LoggingEvent level, Exception exception) {
		this.transmitter.transmit(new LoggingData(source, level, exception));
	}

	public void transmit(Object source, LoggingEvent level, String message) {
		this.transmitter.transmit(new LoggingData(source, level, message));
	}

	@Override
	public void addEventHandler(Handler<LoggingData> eventHandler, LoggingEvent... eventList) {
		this.transmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<LoggingData> eventHandler, LoggingEvent... eventList) {
		this.transmitter.removeEventHandler(eventHandler, eventList);
	}
}
