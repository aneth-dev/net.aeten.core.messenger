package org.pititom.core.logging;

import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;

/**
 *
 * @author Thomas Pérennou
 */
public final class Logger {

	private final static RegisterableTransmitter<?, LogLevel, LoggingData> TRANSMITTER;

	static {
		TRANSMITTER = TransmitterFactory.asynchronous("Logging transmitter", Thread.MIN_PRIORITY);
	}

	private Logger() {}


	public static void log(String source, LogLevel level, String message, Exception exception) {
		TRANSMITTER.transmit(new LoggingData(source, level, message, exception));
	}

	public static void log(String source, LogLevel level, Exception exception) {
		TRANSMITTER.transmit(new LoggingData(source, level, exception));
	}

	public static void log(String source, LogLevel level, String message) {
		TRANSMITTER.transmit(new LoggingData(source, level, message));
	}

	public static void log(Class<?> source, LogLevel level, String message, Exception exception) {
		TRANSMITTER.transmit(new LoggingData(source.getName(), level, message, exception));
	}

	public static void log(Class<?> source, LogLevel level, Exception exception) {
		TRANSMITTER.transmit(new LoggingData(source.getName(), level, exception));
	}

	public static void log(Class<?> source, LogLevel level, String message) {
		TRANSMITTER.transmit(new LoggingData(source.getName(), level, message));
	}

	public static void log(Object source, LogLevel level, String message, Exception exception) {
		TRANSMITTER.transmit(new LoggingData(source.getClass().getName(), level, message, exception));
	}

	public static void log(Object source, LogLevel level, Exception exception) {
		TRANSMITTER.transmit(new LoggingData(source.getClass().getName(), level, exception));
	}

	public static void log(Object source, LogLevel level, String message) {
		TRANSMITTER.transmit(new LoggingData(source.getClass().getName(), level, message));
	}

	public static void addEventHandler(Handler<LoggingData> eventHandler, LogLevel... eventList) {
		TRANSMITTER.addEventHandler(eventHandler, eventList);
	}

	public static void removeEventHandler(Handler<LoggingData> eventHandler, LogLevel... eventList) {
		TRANSMITTER.removeEventHandler(eventHandler, eventList);
	}
}
