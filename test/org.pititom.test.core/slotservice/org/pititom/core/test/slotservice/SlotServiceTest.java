package org.pititom.core.test.slotservice;

import org.pititom.core.event.signal.Signal;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;

public class SlotServiceTest {
	
	public static void main(String[] arguments) throws InterruptedException {
		Signal<LoggingData> debug = new Signal<LoggingData>(new Object(), LoggingEvent.DEBUG);
		Signal<LoggingData> info = new Signal<LoggingData>(new Object(), LoggingEvent.INFO);
		Signal<LoggingData> warning = new Signal<LoggingData>(new Object(), LoggingEvent.WARNING);
		Signal<LoggingData> error = new Signal<LoggingData>(new Object(), LoggingEvent.ERROR);
		debug.emit(new LoggingData("A debug message"));
		info.emit(new LoggingData("An info message"));
		warning.emit(new LoggingData("A warning message"));
		error.emit(new LoggingData("An error message"));
	}

}
