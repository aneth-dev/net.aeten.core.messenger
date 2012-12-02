package net.aeten.core.alert;

import net.aeten.core.event.Handler;
import net.aeten.core.event.Hook;
import net.aeten.core.event.HookEvent;
import net.aeten.core.event.HookEventGroup;
import net.aeten.core.event.RegisterableTransmitter;
import net.aeten.core.event.TransmitterFactory;

/**
 * 
 * @author Thomas Pérennou
 */
public final class Alert {

	public static final HookEventGroup <AlertLevel, Hook> EVENTS = HookEventGroup.build (AlertLevel.values (), new Hook[] {
			Hook.PRE,
			Hook.START,
			Hook.END,
			Hook.POST
	});

	private static final RegisterableTransmitter <HookEvent <AlertLevel, Hook>, AlertData> TRANSMITTER = TransmitterFactory.synchronous (EVENTS.values ());

	private Alert () {}

	public static AlertData start (	Object source,
												AlertLevel level,
												String title,
												String detail) {
		AlertData data = new AlertData (source, EVENTS.get (level, Hook.PRE), title, detail);
		TRANSMITTER.transmit (data);
		if (data.doIt ()) {
			TRANSMITTER.transmit (EVENTS.hook (data, Hook.START));
		}
		return data;
	}

	public static AlertData start (	Object source,
												AlertLevel level,
												String title) {
		AlertData data = new AlertData (source, EVENTS.get (level, Hook.PRE), title);
		TRANSMITTER.transmit (data);
		if (data.doIt ()) {
			TRANSMITTER.transmit (EVENTS.hook (data, Hook.START));
		}
		return data;
	}

	public static void stop (AlertData data) {
		TRANSMITTER.transmit (EVENTS.hook (data, Hook.END));
		TRANSMITTER.transmit (EVENTS.hook (data, Hook.POST));
	}

	public static void addEventHandler (Handler <AlertData> eventHandler,
													HookEvent <AlertLevel, Hook>... eventList) {
		TRANSMITTER.addEventHandler (eventHandler, eventList);
	}

	public static void removeEventHandler (Handler <AlertData> eventHandler,
														HookEvent <AlertLevel, Hook>... eventList) {
		TRANSMITTER.removeEventHandler (eventHandler, eventList);
	}
}
