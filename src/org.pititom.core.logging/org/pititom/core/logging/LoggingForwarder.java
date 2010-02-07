package org.pititom.core.logging;

import org.pititom.core.event.ForwarderFactory;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableForwarder;

/**
 *
 * @author Thomas Pérennou
 */
public class LoggingForwarder implements RegisterableForwarder<Object, LoggingEvent, LoggingData> {

	private final RegisterableForwarder<Object, LoggingEvent, LoggingData> forwarder;
	private final static LoggingForwarder INSTANCE;

	static {
		INSTANCE = new LoggingForwarder();
	}

	private LoggingForwarder() {
		this.forwarder = ForwarderFactory.asynchronous("Logging forwarder");
	}

	public static LoggingForwarder getInstance() {
		return INSTANCE;
	}

	@Override
	public void forward(Object source, LoggingEvent event, LoggingData data) {
		this.forwarder.forward(source, event, data);
	}

	@Override
	public void addEventHandler(Handler<Object, LoggingEvent, LoggingData> eventHandler, LoggingEvent... eventList) {
		this.forwarder.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Object, LoggingEvent, LoggingData> eventHandler, LoggingEvent... eventList) {
		this.forwarder.removeEventHandler(eventHandler, eventList);
	}
}
