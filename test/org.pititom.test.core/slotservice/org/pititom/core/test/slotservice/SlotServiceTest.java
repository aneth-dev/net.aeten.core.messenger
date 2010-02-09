package org.pititom.core.test.slotservice;

import org.pititom.core.event.signal.Signal;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;

public class SlotServiceTest {
	
	public static void main(String[] arguments) throws InterruptedException {
		Signal<LoggingData> info = new Signal<LoggingData>(new Object(), LoggingEvent.INFO);
		info.emit(new LoggingData("toto"));
	}

}
