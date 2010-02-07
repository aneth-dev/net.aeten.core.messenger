package org.pititom.core.messenger;

import org.pititom.core.event.Transmitter;

/**
 *
 * @author Thomas Pérennou
 */
public class MessengerHookData<Message, Acknowledge extends Enum<?>> {
	private long sentDataDate = 0L;
	private Message messageToSend = null;
	private Message recievedMessage = null;
	private long recievedDataDate = 0L;
	private Acknowledge acknowledge = null;
	private MessengerEventData<Message, Acknowledge> currentEventData;
	private final Transmitter<MessengerEvent, MessengerEventData<Message, Acknowledge>> eventTransmitter;

	
	public MessengerHookData(Transmitter<MessengerEvent, MessengerEventData<Message, Acknowledge>> eventTransmitter) {
		this.eventTransmitter = eventTransmitter;
	}
	
	/**
	 * @return the sentDataDate
	 */
	public long getSentDataDate() {
		return sentDataDate;
	}
	/**
	 * @param sentDataDate the sentDataDate to set
	 */
	public void setSentDataDate(long sentDataDate) {
		this.sentDataDate = sentDataDate;
	}
	/**
	 * @return the messageToSend
	 */
	public Message getMessageToSend() {
		return messageToSend;
	}
	/**
	 * @param messageToSend the messageToSend to set
	 */
	public void setMessageToSend(Message messageToSend) {
		this.messageToSend = messageToSend;
	}
	/**
	 * @return the recievedMessage
	 */
	public Message getRecievedMessage() {
		return recievedMessage;
	}
	/**
	 * @param recievedMessage the recievedMessage to set
	 */
	public void setRecievedMessage(Message recievedMessage) {
		this.recievedMessage = recievedMessage;
	}
	/**
	 * @return the recievedDataDate
	 */
	public long getRecievedDataDate() {
		return recievedDataDate;
	}
	/**
	 * @param recievedDataDate the recievedDataDate to set
	 */
	public void setRecievedDataDate(long recievedDataDate) {
		this.recievedDataDate = recievedDataDate;
	}
	/**
	 * @return the acknowledge
	 */
	public Acknowledge getAcknowledge() {
		return acknowledge;
	}
	/**
	 * @param acknowledge the acknowledge to set
	 */
	public void setAcknowledge(Acknowledge acknowledge) {
		this.acknowledge = acknowledge;
	}
	/**
	 * @return the currentEventData
	 */
	public MessengerEventData<Message, Acknowledge> getCurrentEventData() {
		return currentEventData;
	}
	/**
	 * @param currentEventData the currentEventData to set
	 */
	public void setCurrentEventData(MessengerEventData<Message, Acknowledge> currentEventData) {
		this.currentEventData = currentEventData;
	}
	
	/**
	 * @return the eventTransmitter
	 */
	public Transmitter<MessengerEvent, MessengerEventData<Message, Acknowledge>> getEventTransmitter() {
		return eventTransmitter;
	}
	
}
