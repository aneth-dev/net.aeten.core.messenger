package org.pititom.core.test.slotservice.provider;

import java.util.Calendar;
import java.util.Date;

import org.pititom.core.event.signal.service.Slot;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;

/**
 *
 * @author Thomas Pérennou
 */
	public class Logger implements Slot<Object, LoggingEvent, LoggingData> {
		@Override
		public void handleEvent(Object source, LoggingEvent event, LoggingData data) {
			Date date = Calendar.getInstance().getTime();
			System.out.println(date + " " + (date.getTime() % 1000) + "ms " + event + " source={" + source + "} " + data.getMessage() + ((data.getException() == null) ? "":  " " + data.getException()));
		}

		@Override
		public LoggingEvent[] getEvents() {
			return LoggingEvent.values();
		}
	}
