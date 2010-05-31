package org.pititom.core.logging;

import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;

/**
 *
 * @author Thomas Pérennou
 */
public final class Logger {

	private final static RegisterableTransmitter<?, LogLevel, LoggingData> TRANSMITTER = TransmitterFactory.synchronous();

	private Logger() {}

	public static void log(Object source, LogLevel level, String message, Throwable throwable) {
		TRANSMITTER.transmit(new LoggingData(source, level, message, throwable));
	}

	public static void log(Object source, LogLevel level, Throwable throwable) {
		TRANSMITTER.transmit(new LoggingData(source, level, throwable));
	}

	public static void log(Object source, LogLevel level, String message) {
		TRANSMITTER.transmit(new LoggingData(source, level, message));
	}

	public static void addEventHandler(Handler<LoggingData> eventHandler, LogLevel... eventList) {
		TRANSMITTER.addEventHandler(eventHandler, eventList);
	}

	public static void removeEventHandler(Handler<LoggingData> eventHandler, LogLevel... eventList) {
		TRANSMITTER.removeEventHandler(eventHandler, eventList);
	}
}
