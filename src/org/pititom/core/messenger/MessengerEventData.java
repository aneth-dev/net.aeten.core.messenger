package org.pititom.core.messenger;

/**
 *
 * @author Thomas Pérennou
 */
public class MessengerEventData<Message, Acknowledge extends Enum<?>>  implements org.pititom.core.Cloneable<MessengerEventData<Message, Acknowledge>> {

	private Throwable exception = null;
	private Message sentMessage = null;
	private Message recievedMessage = null;
	private Acknowledge acknowledge = null;

	public MessengerEventData() {
	}

	public MessengerEventData(Message sentMessage, Message recievedMessage, Acknowledge acknowledge, Throwable exception) {
		this.sentMessage = sentMessage;
		this.recievedMessage = recievedMessage;
		this.acknowledge = acknowledge;
		this.exception = exception;
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
		return "sent message={" + this.sentMessage + "}; recieved message={" + this.recievedMessage + "}; acknowledge={" + this.acknowledge + "}; exception={" + this.exception + "}";
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	@Override
	public MessengerEventData<Message, Acknowledge> clone() {
		return new MessengerEventData<Message, Acknowledge>(this.sentMessage, this.recievedMessage, this.acknowledge, this.exception);
	}
}
