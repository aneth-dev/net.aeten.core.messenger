package org.pititom.core.test.slotservice;

import org.pititom.core.event.Default;
import org.pititom.core.event.Handler;
import org.pititom.core.event.signal.Signal;
import org.pititom.core.event.signal.SignalGroup;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;

public class SlotServiceTest {
	
	public static void main(String[] arguments) throws InterruptedException {
		Signal<LoggingData> debug = new Signal<LoggingData>(LoggingEvent.DEBUG);
		Signal<LoggingData> info = new Signal<LoggingData>(LoggingEvent.INFO);
		Signal<LoggingData> warning = new Signal<LoggingData>(LoggingEvent.WARNING);
		Signal<LoggingData> error = new Signal<LoggingData>(LoggingEvent.ERROR);
		Signal<String> iner = new Signal<String>(new Handler<Default, Default, String>() {
			@Override
			public void handleEvent(Default source, Default event, String data) {
				System.out.println(data);
			}
		});
		
		debug.emit(new LoggingData("A debug message"));
		info.emitSync(new LoggingData("An info message"));
		warning.emit(new LoggingData("A warning message"));
		error.emitSync(new LoggingData("An error message"));
		iner.emit("Iner");
		
		
		SignalGroup<LoggingEvent, LoggingData> log = new SignalGroup<LoggingEvent, LoggingData>(Default.ANONYMOUS_SOURCE, LoggingEvent.class);
		log.emit(LoggingEvent.ERROR, new LoggingData("An other debug message"));
	}

}
