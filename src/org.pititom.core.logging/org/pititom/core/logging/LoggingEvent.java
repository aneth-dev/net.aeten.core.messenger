package org.pititom.core.logging;

import org.pititom.core.event.signal.Event;

/**
 *
 * @author Thomas Pérennou
 */
public enum LoggingEvent implements Event<LoggingData> {
	INFO,
	DEBUG,
	WARNING,
	ERROR;

	@Override
	public Class<LoggingData> getDataClass() {
		return LoggingData.class;
	}
}
