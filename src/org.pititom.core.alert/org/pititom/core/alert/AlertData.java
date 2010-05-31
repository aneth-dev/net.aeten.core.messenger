package org.pititom.core.alert;


import org.pititom.core.event.Hook;
import org.pititom.core.event.HookEvent;
import org.pititom.core.event.HookEventData;

/**
 *
 * @author Thomas Pérennou
 */
public class AlertData extends HookEventData<Object, AlertLevel, Hook> {
	private final String message;

	public AlertData(Object source, HookEvent<AlertLevel, Hook> event, String message) {
		super(source, event);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
