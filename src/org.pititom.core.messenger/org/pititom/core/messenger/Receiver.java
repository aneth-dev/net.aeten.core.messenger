package org.pititom.core.messenger;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineParser;
import org.pititom.core.Connection;
import org.pititom.core.Identifiable;
import org.pititom.core.messenger.args4j.ReceiverOptionHandler;

public interface Receiver<Message> extends Identifiable, Connection {
	public void receive(MessengerEventData<Message> data) throws IOException;
	
	public static abstract class Helper<Message> extends org.pititom.core.messenger.Helper implements Receiver<Message> {
		static {
			CmdLineParser.registerHandler(Receiver.class, ReceiverOptionHandler.class);
		}
		public Helper() {
			super();
		}
		public Helper(String identifier) {
			super(identifier);
		}
		@Override
		public String toString() {
			return "Receiver \"" + this.getIdentifier() + "\"";
		}
	}
}
