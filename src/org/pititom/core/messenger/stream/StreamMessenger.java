package org.pititom.core.messenger.stream;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.pititom.core.extersion.ConfigurationException;
import org.pititom.core.messenger.AbstractMessenger;
import org.pititom.core.messenger.MessengerAcknowledgeProtocol;
import org.pititom.core.stream.controller.StreamControllerConnection;
import org.pititom.core.stream.dada.StreamControllerConfiguration;

public class StreamMessenger<Message, Acknowledge extends Enum<?>> extends AbstractMessenger<Message, Acknowledge> {

	private final StreamControllerConfiguration emissionConfiguration;
	private final StreamControllerConfiguration[] receptionConfigurationList;
	private final Collection<Reciever> recieverList;
	private final StreamControllerConnection receptionConnectionList[];

	private MessengerObjectOutputStream emissionStream;
	private StreamControllerConnection emissionConnection;

	public StreamMessenger(String name, MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol, String emissionConfiguration, String... receptionConfigurationList) throws CmdLineException {
		super(name, acknowledgeProtocol);

		this.emissionConfiguration = new StreamControllerConfiguration(emissionConfiguration);
		
		this.receptionConfigurationList = new StreamControllerConfiguration[receptionConfigurationList.length];
		this.receptionConnectionList = new StreamControllerConnection[receptionConfigurationList.length];
		this.recieverList = new ArrayList<Reciever>(receptionConfigurationList.length);
		for (int i=0; i<receptionConfigurationList.length; i++) {
			this.receptionConfigurationList[i] = new StreamControllerConfiguration(receptionConfigurationList[i]);
		}
	}

	public StreamMessenger(String name, String emissionConfiguration, String receptionConfiguration) throws CmdLineException {
		this(name, null, emissionConfiguration, receptionConfiguration);
	}

	protected void doConnect() throws IOException {
		try {
			final PipedInputStream pipedIn = new PipedInputStream();
			this.emissionConnection = new StreamControllerConnection(this.emissionConfiguration, pipedIn);
			this.emissionStream = new MessengerObjectOutputStream(new PipedOutputStream(pipedIn));

			PipedOutputStream pipedOut;
			for (int i=0; i<this.receptionConfigurationList.length; i++) {
				pipedOut = new PipedOutputStream();
				this.receptionConnectionList[i] = new StreamControllerConnection(this.receptionConfigurationList[i], pipedOut);
				this.recieverList.add(new Reciever(this, new MessengerObjectInputStream(new PipedInputStream(pipedOut))));
				this.receptionConnectionList[i].connect();
			}

			this.emissionConnection.connect();
			
		} catch (ConfigurationException exception) {
			this.setConnected(false);
			throw new IOException(exception);
		}

		super.doConnect();

		for (Reciever reciever : this.recieverList) {
			new Thread(reciever).start();
		}
	}

	protected void doDisconnect() throws IOException {
		super.doDisconnect();
		for (StreamControllerConnection receptionConnection : this.receptionConnectionList) {
			receptionConnection.disconnect();
		}
		this.emissionConnection.disconnect();
	}

	private class Reciever implements Runnable {
		private final AbstractMessenger<Message, ? extends Enum<?>> messenger;
		private final MessengerObjectInputStream in;
		
		public Reciever(AbstractMessenger<Message, ? extends Enum<?>> messenger, MessengerObjectInputStream in) {
			this.messenger = messenger;
			this.in = in;
		}
		
		@Override
		public void run() {
			try {
				while (this.messenger.isConnected()) {
					Object bean;
					try {
						bean = this.in.readObject();
						@SuppressWarnings("unchecked")
						Message message = (Message) bean;

						StreamMessenger.this.doReception(message);

					} catch (ClassNotFoundException exception) {
						Logger.getLogger(StreamMessenger.class.getName()).log(Level.SEVERE, null, exception);
					} catch (ClassCastException exception) {
						Logger.getLogger(StreamMessenger.class.getName()).log(Level.SEVERE, null, exception);
					}
				}
			} catch (IOException exception) {
				Logger.getLogger(StreamMessenger.class.getName()).log(Level.SEVERE, null, exception);
			}
		}
	}

	@Override
	protected void sendMessage(Message message) {
		try {
			this.emissionStream.writeObject(message);
			this.emissionStream.flush();
		} catch (IOException exception) {
			Logger.getLogger(StreamMessenger.class.getName()).log(Level.SEVERE, null, exception);
		}
	}
}
