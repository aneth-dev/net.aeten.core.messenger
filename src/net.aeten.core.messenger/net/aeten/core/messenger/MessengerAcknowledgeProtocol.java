package net.aeten.core.messenger;

/**
 *
 * @author Thomas Pérennou
 */
public interface MessengerAcknowledgeProtocol<Message, Acknowledge extends Enum <?>> {

	/** @return expected timeout or 0 for not acknowledge message */
	public long getAcknowledgedTimeout (Message message);

	/** @return true when given acknowledge is a success one */
	public boolean isSuccess (Acknowledge acknowledge);

	/** @return true when given sent message acknowledge is blocking */
	public boolean isBlocking (Message message);

	/** @return acknowledge for given sent message with recieved ones or null if recieved message does nt acknowledge sent one */
	public Acknowledge getAcknowledge (	Message sentMessage,
													Message recievedMessage);
}
