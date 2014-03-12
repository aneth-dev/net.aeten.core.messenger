package net.aeten.core.messenger;

import net.aeten.core.event.Hook;
import net.aeten.core.event.HookEventData;
import net.aeten.core.event.Priority;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerEventData<Message> extends
		HookEventData <Messenger <Message>, MessengerEvent, Hook> {

	private Message message;
	private String contact;
	private String subcontractor;
	private String service;

	public MessengerEventData (Messenger <Message> source,
										String contact,
										MessengerEvent event,
										Hook hook,
										Message message) {
		this (source, contact, event, hook, message, Priority.MEDIUM, true);
	}

	public MessengerEventData (Messenger <Message> source,
										String contact,
										MessengerEvent event,
										Hook hook,
										Message message,
										boolean doIt) {
		this (source, contact, event, hook, message, Priority.MEDIUM, doIt);
	}

	public MessengerEventData (Messenger <Message> source,
										String contact,
										MessengerEvent event,
										Hook hook,
										Message message,
										Priority priority) {
		this (source, contact, event, hook, message, priority, true);
	}

	protected MessengerEventData (Messenger <Message> source,
											String contact,
											MessengerEvent event,
											Hook hook,
											Message message,
											Priority priority,
											boolean doIt) {
		this (source, contact, null, event, hook, message, priority, doIt);
	}

	public MessengerEventData (Messenger <Message> source,
										String contact,
										String service,
										MessengerEvent event,
										Hook hook,
										Message message) {
		this (source, contact, service, event, hook, message, Priority.MEDIUM, true);
	}

	public MessengerEventData (Messenger <Message> source,
										String contact,
										String service,
										MessengerEvent event,
										Hook hook,
										Message message,
										boolean doIt) {
		this (source, contact, service, event, hook, message, Priority.MEDIUM, doIt);
	}

	public MessengerEventData (Messenger <Message> source,
										String contact,
										String service,
										MessengerEvent event,
										Hook hook,
										Message message,
										Priority priority) {
		this (source, contact, service, event, hook, message, priority, true);
	}

	protected MessengerEventData (Messenger <Message> source,
											String contact,
											String service,
											MessengerEvent event,
											Hook hook,
											Message message,
											Priority priority,
											boolean doIt) {
		super (source, Messenger.EVENTS.get (event, hook), priority, doIt);
		this.message = message;
		this.contact = contact;
		this.service = service;
	}

	public Message getMessage () {
		return message;
	}

	public void setMessage (Message message) {
		this.message = message;
	}

	@Override
	public String toString () {
		return super.toString () + " message=(" + this.message + ")" + " contact=(" + this.contact + ")" + " service=(" + this.service + ")" + " subcontractor=(" + this.subcontractor + ")";
	}

	public void setSubcontractor (String subcontractor) {
		this.subcontractor = subcontractor;
	}

	public String getSubcontractor () {
		return this.subcontractor;
	}

	public String getContact () {
		return this.contact;
	}

	public void setContact (String contact) {
		this.contact = contact;
	}

	public void setService (String service) {
		this.service = service;
	}

	public String getService () {
		return service;
	}

}
