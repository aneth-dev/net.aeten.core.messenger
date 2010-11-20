package net.aeten.core.messenger;

import java.io.IOException;

import net.aeten.core.Connection;
import net.aeten.core.Identifiable;
import net.aeten.core.messenger.args4j.SenderOptionHandler;

import org.kohsuke.args4j.CmdLineParser;
public interface Sender<Message> extends Identifiable, Connection {
	public abstract void send(MessengerEventData<Message> data) throws IOException;

	public static abstract class Helper<Message> extends net.aeten.core.messenger.Helper implements Sender<Message> {
		static {
			CmdLineParser.registerHandler(Sender.class, SenderOptionHandler.class);
		}
		public Helper() {
			super();
		}
		public Helper(String identifier) {
			super(identifier);
		}
		@Override
		public String toString() {
			return "Sender \"" + this.getIdentifier() + "\"";
		}
	}
}
