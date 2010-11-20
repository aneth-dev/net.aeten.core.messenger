package org.pititom.core.messenger;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineParser;
import org.pititom.core.Connection;
import org.pititom.core.Identifiable;
import org.pititom.core.messenger.args4j.SenderOptionHandler;
public interface Sender<Message> extends Identifiable, Connection {
	public abstract void send(MessengerEventData<Message> data) throws IOException;

	public static abstract class Helper<Message> extends org.pititom.core.messenger.Helper implements Sender<Message> {
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
