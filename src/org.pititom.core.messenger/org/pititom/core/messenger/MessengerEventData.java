package org.pititom.core.messenger;

import org.pititom.core.event.Hook;
import org.pititom.core.event.HookEventData;
import org.pititom.core.event.Priority;
import org.pititom.core.messenger.service.Messenger;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerEventData<Message> extends HookEventData<Messenger<Message>, MessengerEvent, Hook> {

	private Message message;
	private String contact;

  public MessengerEventData(Messenger<Message> source, String contact, MessengerEvent event, Hook hook, Message message) {
     this(source, contact, event, hook, message, Priority.MEDIUM, true);
   }

  public MessengerEventData(Messenger<Message> source, String contact, MessengerEvent event, Hook hook, Message message, boolean doIt) {
     this(source, contact, event, hook, message, Priority.MEDIUM, doIt);
   }

  public MessengerEventData(Messenger<Message> source, String contact, MessengerEvent event, Hook hook, Message message, Priority priority) {
     this(source, contact, event, hook, message, priority, true);
   }

	protected MessengerEventData(Messenger<Message> source, String contact, MessengerEvent event, Hook hook, Message message, Priority priority, boolean doIt) {
		super(source, Messenger.EVENTS.get(event, hook), priority, doIt);
		this.message = message;
		this.contact = contact;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return super.toString() + " message=(" + this.message + ")";
	}

	public String getContact() {
		return this.contact;
	}

}
