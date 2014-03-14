package net.aeten.core.messenger.test;

import net.aeten.core.messenger.MessengerAcknowledgeProtocol;

/**
 *
 * @author Thomas Pérennou
 */
public class AcknowledgeProtocol implements MessengerAcknowledgeProtocol<AbstractMessage, Acknowledge> {

	private static final long TIMEOUT = 1000;

	@Override
	public boolean isSuccess(Acknowledge acknowledge) {
		switch (acknowledge) {
		case OK:
			return true;
		default:
			return false;
		}
	}

	@Override
	public Acknowledge getAcknowledge(AbstractMessage sentMessage, AbstractMessage recievedMessage) {
		if ((recievedMessage == null) || (recievedMessage.getAcknowledge() == null)) { return null; }
		switch (recievedMessage.getAcknowledge()) {
		case INVALID_DATA:
		case INVALID_MESSAGE:
		case OK:
			return recievedMessage.getAcknowledge();
		default:
			return null;
		}

	}

	@Override
	public long getAcknowledgedTimeout(AbstractMessage message) {
		if (message.getAcknowledge() == null) { return 0; }
		switch (message.getAcknowledge()) {
		case SOLLICITED_NEED_ACKNOWLEDGE:
		case UNSOLLICITED_NEED_ACKNOWLEDGE:
			return TIMEOUT;
		default:
			return 0;
		}
	}

	@Override
	public boolean isBlocking(AbstractMessage message) {
		return false;
	}

}
