package net.aeten.core.alert;

import net.aeten.core.event.Hook;
import net.aeten.core.event.HookEvent;
import net.aeten.core.event.HookEventData;

/**
 *
 * @author Thomas Pérennou
 */
public class AlertData extends
		HookEventData <Object, AlertLevel, Hook> {
	private final String title;
	private final String detail;

	public AlertData (Object source,
							HookEvent <AlertLevel, Hook> event,
							String title,
							String detail) {
		super (source, event);
		this.title = title;
		this.detail = detail;
	}

	public AlertData (Object source,
							HookEvent <AlertLevel, Hook> event,
							String message) {
		this (source, event, message, null);
	}

	public String getTitle () {
		return title;
	}

	public String getDetail () {
		return detail;
	}

}
