package org.pititom.core.messenger;

/**
 *
 * @author Thomas Pérennou
 */
public class MessengerEventData<Message, Acknowledge extends Enum<?>> {

	private Message sentMessage;
	private Message recievedMessage;
	private Acknowledge acknowledge;

	public MessengerEventData(Message sentMessage, Message recievedMessage, Acknowledge acknowledge) {
		this.sentMessage = sentMessage;
		this.recievedMessage = recievedMessage;
		this.acknowledge = acknowledge;
	}

	public Acknowledge getAcknowledge() {
		return acknowledge;
	}

	public Message getRecievedMessage() {
		return recievedMessage;
	}

	public Message getSentMessage() {
		return sentMessage;
	}

	public void setAcknowledge(Acknowledge acknowledge) {
		this.acknowledge = acknowledge;
	}

	public void setRecievedMessage(Message recievedMessage) {
		this.recievedMessage = recievedMessage;
	}

	public void setSentMessage(Message sentMessage) {
		this.sentMessage = sentMessage;
	}

	@Override
	public String toString() {
		return "sent Message={" + this.sentMessage + "}; recieved message={" + this.recievedMessage + "}; acknowledge={" + this.acknowledge + "}";
	}
}
