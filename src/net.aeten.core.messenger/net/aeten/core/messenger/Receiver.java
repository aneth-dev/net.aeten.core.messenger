package net.aeten.core.messenger;

import java.io.IOException;

import net.aeten.core.Connection;
import net.aeten.core.Identifiable;

public interface Receiver<Message> extends Identifiable, Connection {
	public void receive(MessengerEventData<Message> data) throws IOException;

	/** Must be thread safe */
	@Override
	public boolean isConnected();

	public static abstract class ReceiverAdapter<Message> extends ConnectionAdapter implements Receiver<Message> {
		public ReceiverAdapter(String identifier) {
			super(identifier);
		}

		@Override
		public String toString() {
			return "Receiver \"" + this.getIdentifier() + "\"";
		}
	}
}
