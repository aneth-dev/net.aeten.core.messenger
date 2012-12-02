package net.aeten.core.messenger;

import net.aeten.core.event.EventData;

/**
 *
 * @author Thomas Pérennou
 */
public class MessengerAcknowledgeEventData<Message, Acknowledge extends Enum <?>> extends
		EventData <Messenger <Message>, MessengerAcknowledgeEvent> {

	private Acknowledge acknowledge = null;
	private Message sentMessage;
	private Message recievedMessage;

	public MessengerAcknowledgeEventData (	Messenger <Message> source,
														MessengerAcknowledgeEvent event,
														Message sentMessage,
														Message recievedMessage,
														Acknowledge acknowledge) {
		super (source, event);
		this.sentMessage = sentMessage;
		this.recievedMessage = recievedMessage;
		this.acknowledge = acknowledge;
	}

	public Acknowledge getAcknowledge () {
		return acknowledge;
	}

	public void setAcknowledge (Acknowledge acknowledge) {
		this.acknowledge = acknowledge;
	}

	public Message getSentMessage () {
		return sentMessage;
	}

	public void setSentMessage (Message sentMessage) {
		this.sentMessage = sentMessage;
	}

	public Message getRecievedMessage () {
		return recievedMessage;
	}

	public void setRecievedMessage (Message recievedMessage) {
		this.recievedMessage = recievedMessage;
	}

	@Override
	public String toString () {
		return "sent_message={" + this.sentMessage + "}" + " recieved_message={" + this.recievedMessage + "}" + " acknowledge={" + this.acknowledge + "}";
	}

}
