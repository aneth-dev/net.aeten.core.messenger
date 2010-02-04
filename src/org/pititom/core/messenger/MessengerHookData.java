package org.pititom.core.messenger;

import java.util.concurrent.BlockingQueue;

import org.pititom.core.event.EventEntry;
import org.pititom.core.messenger.extension.Messenger;

/**
 *
 * @author Thomas Pérennou
 */
public class MessengerHookData<Message, Acknowledge extends Enum<?>> {
	private byte[] sentData = null;
	private long sentDataDate = 0L;
	private Message messageToSend = null;
	private Message recievedMessage = null;
	private long recievedDataDate = 0L;
	private byte[] recievedData = null;
	private Acknowledge acknowledge = null;
	private MessengerEventData<Message, Acknowledge> currentEventData;
	private final BlockingQueue<EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>> notificationQueue;
	
	public MessengerHookData(BlockingQueue<EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>> notificationQueue) {
		this.notificationQueue = notificationQueue;
	}
	
	/**
	 * @return the sentData
	 */
	public byte[] getSentData() {
		return sentData;
	}
	/**
	 * @param sentData the sentData to set
	 */
	public void setSentData(byte[] sentData) {
		this.sentData = sentData;
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
	 * @return the recievedData
	 */
	public byte[] getRecievedData() {
		return recievedData;
	}
	/**
	 * @param recievedData the recievedData to set
	 */
	public void setRecievedData(byte[] recievedData) {
		this.recievedData = recievedData;
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
	 * TODO
	 **/
	public void fireEvent(Messenger<Message, Acknowledge> source, MessengerEvent event) {
		notificationQueue.add(new EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>(source, event, this.getCurrentEventData().clone()));
	}
	
}
