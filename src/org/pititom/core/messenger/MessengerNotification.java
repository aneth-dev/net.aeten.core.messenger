package org.pititom.core.messenger;

class MessengerNotification<Message, Acknowledge extends Enum<?>> {
	private final Messenger<Message, Acknowledge> messenger;
	private final MessengerEvent event;
	private final MessengerEventData<Message, Acknowledge> eventData;
	
	public MessengerNotification(Messenger<Message, Acknowledge> messenger, MessengerEvent event, MessengerEventData<Message, Acknowledge> eventData) {
		super();
		this.messenger = messenger;
		this.event = event;
		this.eventData = eventData;
	}

	public Messenger<Message, Acknowledge> getMessenger() {
		return messenger;
	}
	
	public MessengerEvent getEvent() {
		return event;
	}

	public MessengerEventData<Message, Acknowledge> getEventData() {
		return eventData;
	}
}
