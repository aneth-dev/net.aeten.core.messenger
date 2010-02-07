package org.pititom.core.messenger.stream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.pititom.core.ConfigurationException;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingForwarder;
import org.pititom.core.messenger.AbstractMessenger;

/**
 *
 * @author Thomas Pérennou
 */
public class StreamMessenger<Message, Acknowledge extends Enum<?>>  extends AbstractMessenger<Message, Acknowledge> {

	private final ObjectOutputStream outputStream;
	private final ObjectInputStream[] inputStreamList;
	private final Collection<Reciever> recieverList;

	public StreamMessenger(String name, String hookConfiguration, ObjectOutputStream outputStream, ObjectInputStream... inputStreamList) throws ConfigurationException, IOException {
		super(name);
		this.configure(hookConfiguration);

		this.outputStream = outputStream;
		this.inputStreamList = inputStreamList;

		this.recieverList = new ArrayList<Reciever>(inputStreamList.length);

		this.connect();
	}

	@Override
	protected void doConnect() throws IOException {
		super.doConnect();
		Reciever reciever;
		for (int i = 0; i < this.inputStreamList.length; i++) {
			reciever = new Reciever(this, this.inputStreamList[i]);
			this.recieverList.add(reciever);
			new Thread(reciever, "Messenger \"" + this.getName() + "\" reciever").start();
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		super.doDisconnect();
		for (ObjectInputStream inputStream : this.inputStreamList) {
			inputStream.close();
		}
		this.outputStream.close();
	}

	private class Reciever implements Runnable {

		private final AbstractMessenger<Message, ? extends Enum<?>> messenger;
		private final ObjectInputStream in;

		public Reciever(AbstractMessenger<Message, ? extends Enum<?>> messenger, ObjectInputStream in) {
			this.messenger = messenger;
			this.in = in;
		}

		@Override
		public void run() {
			while (this.messenger.isConnected()) {
				Object bean;
				try {
					bean = this.in.readObject();
					@SuppressWarnings("unchecked")
					Message message = (Message) bean;

					StreamMessenger.this.doRecieve(message);

				} catch (Exception exception) {
					LoggingForwarder.getInstance().forward(this.messenger, LoggingEvent.ERROR, new LoggingData(exception));
				}
			}
		}
	}

	@Override
	protected void send(Message message) {
		try {
			this.outputStream.writeObject(message);
			this.outputStream.flush();
		} catch (IOException exception) {
			LoggingForwarder.getInstance().forward(this, LoggingEvent.ERROR, new LoggingData(exception));
		}
	}
}
