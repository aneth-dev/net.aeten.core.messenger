package org.pititom.core.alert;

import org.pititom.core.event.Handler;
import org.pititom.core.event.Hook;
import org.pititom.core.event.HookEvent;
import org.pititom.core.event.HookEventGroup;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.TransmitterFactory;

/**
 * 
 * @author Thomas Pérennou
 */
public final class Alert {

	private static final RegisterableTransmitter<?, HookEvent<AlertLevel, Hook>, AlertData> TRANSMITTER = TransmitterFactory.synchronous();
	
	public static final HookEventGroup<AlertLevel, Hook> EVENTS = HookEventGroup.get(AlertLevel.values());

	private Alert() {}

	public static AlertData start(Object source, AlertLevel level, String message) {
		AlertData data = new AlertData(source, EVENTS.get(level, Hook.PRE), message);
		TRANSMITTER.transmit(data);
		if (data.doIt()) {
			TRANSMITTER.transmit(EVENTS.hook(data, Hook.START));
		}
		return data;
	}
	
	public static void stop(AlertData data) {
		TRANSMITTER.transmit(EVENTS.hook(data, Hook.END));
		TRANSMITTER.transmit(EVENTS.hook(data, Hook.POST));
	}
	
	public static void addEventHandler(Handler<AlertData> eventHandler, HookEvent<AlertLevel, Hook>... eventList) {
		TRANSMITTER.addEventHandler(eventHandler, eventList);
	}

	public static void removeEventHandler(Handler<AlertData> eventHandler, HookEvent<AlertLevel, Hook>... eventList) {
		TRANSMITTER.removeEventHandler(eventHandler, eventList);
	}
}
