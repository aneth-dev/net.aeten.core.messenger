package net.aeten.core.messenger;

import java.io.IOException;

import net.aeten.core.Connection;
import net.aeten.core.Identifiable;

public interface Sender<Message> extends
		Identifiable,
		Connection {
	public abstract void send (MessengerEventData <Message> data) throws IOException;

	public static abstract class SenderAdapter<Message> extends
			ConnectionAdapter implements
			Sender <Message> {
		public SenderAdapter (String identifier) {
			super (identifier);
		}

		@Override
		public String toString () {
			return "Sender \"" + this.getIdentifier () + "\"";
		}
	}
}
