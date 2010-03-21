package org.pititom.core.messenger.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.args4j.CommandLineParserHelper;
import org.pititom.core.event.Handler;
import org.pititom.core.event.HookEvent;
import org.pititom.core.event.Priority;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.Transmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.service.Messenger;
import org.pititom.core.messenger.service.Receiver;
import org.pititom.core.messenger.service.Sender;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerProvider<Message> implements Messenger<Message>, Configurable {

	@Option(name = "-id", aliases = "--identifier", required = true)
	private String identifier;

	@Option(name = "-d", aliases = "--description", required = false)
	private String description;

	@Option(name = "-p", aliases = "--thread-priority", required = false)
	private int threadPriority = -1;

	@SuppressWarnings("unchecked")
	@Option(name = "-s", aliases = "--sender", required = false)
	private List<Sender> senderList = new ArrayList<Sender>(0);

	@SuppressWarnings("unchecked")
	@Option(name = "-r", aliases = "--receiver", required = false)
	private List<Receiver> receiverList = new ArrayList<Receiver>(0);

	@Option(name = "-c", aliases = "--auto-connect", required = false)
	private boolean autoConnect = false;

	private Map<String, Sender<Message>> senderMap = new HashMap<String, Sender<Message>>();

	private boolean connected;

	private Transmitter<MessengerEventData<Message>> sendEventTransmitter;
	private Transmitter<MessengerEventData<Message>> receiveEventTransmitter;
	private RegisterableTransmitter<Messenger<Message>, HookEvent<MessengerEvent>, MessengerEventData<Message>> hookTransmitter;

	/** @deprecated Reserved to configuration building */
	public MessengerProvider() {
		this(null, null, Thread.NORM_PRIORITY, new Sender[0], new Receiver[0]);
	}

	public MessengerProvider(String identifier) {
		this(identifier, null, Thread.NORM_PRIORITY, new Sender[0], new Receiver[0]);
	}

	public MessengerProvider(String identifier, int threadPriority) {
		this(identifier, null, threadPriority, new Sender[0], new Receiver[0]);
	}

	public MessengerProvider(String identifier, String description) {
		this(identifier, description, Thread.NORM_PRIORITY, new Sender[0], new Receiver[0]);
	}
	
	public MessengerProvider(String identifier, String description, int threadPriority, Sender<Message> sender, Receiver<Message> receiver) {
		this(identifier, description, threadPriority, new Sender[] {sender}, new Receiver[] {receiver});
	}

	public MessengerProvider(String identifier, String description, int threadPriority, Sender<Message>[] senderList, Receiver<Message>[] receiverList) {
		this.identifier = identifier;
		this.description = description;
		this.threadPriority = threadPriority;

		try {
			for (Sender<Message> sender : senderList) {
				this.addSender(sender);
			}

			for (Receiver<Message> reciever : receiverList) {
				this.addReceiver(reciever);
			}
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}

		this.hookTransmitter = TransmitterFactory.synchronous(MessengerEvent.class);
		this.sendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter", this.threadPriority, this.hookTransmitter);
		this.receiveEventTransmitter = TransmitterFactory.asynchronous("Reciever transmitter", this.threadPriority, this.hookTransmitter);

		this.hookTransmitter.addEventHandler(new Handler<MessengerEventData<Message>>() {
			@Override
			public void handleEvent(MessengerEventData<Message> data) {
				MessengerProvider.this.senderMap.get(data.getContact()).send(data.getMessage());
			}
		}, HookEvent.get(MessengerEvent.SEND));

	}

	@Override
	public void transmit(Message message, String contact, Priority priority) {
		if (this.connected) {
			this.sendEventTransmitter.transmit(new MessengerEventData<Message>(this, contact, MessengerEvent.SEND, message, priority));
		}
	}

	@Override
	public void transmit(Message message, String contact) {
		this.transmit(message, contact, Priority.MEDIUM);
	}

	@Override
	public String toString() {
		return this.identifier + ((this.description == null) ? " (" + this.description + ")" : "");
	}

	@Override
	public synchronized void connect() throws IOException {
		if (!this.connected) {
			for (Receiver<Message> reciever : this.receiverList) {
				reciever.connect();
				this.startReciever(reciever);
			}
			for (Sender<Message> sender : this.senderMap.values()) {
				sender.connect();
			}
			this.connected = true;
		}
	}

	@Override
	public synchronized void disconnect() throws IOException {
		if (this.connected) {
			for (Receiver<Message> reciever : this.receiverList) {
				reciever.disconnect();
			}
			for (Sender<Message> sender : this.senderMap.values()) {
				sender.disconnect();
			}
			this.connected = false;
		}
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public void addEventHandler(Handler<MessengerEventData<Message>> eventHandler, HookEvent<MessengerEvent>... eventList) {
		this.hookTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<MessengerEventData<Message>> eventHandler, HookEvent<MessengerEvent>... eventList) {
		this.hookTransmitter.removeEventHandler(eventHandler, eventList);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public synchronized void configure(String configuration) throws ConfigurationException {
		CommandLineParserHelper.configure(this, configuration);
		if (this.threadPriority > -1) {
			// TODO
		}
		for (Sender<Message> sender : this.senderList) {
			this.senderMap.put(sender.getIdentifier(), sender);
		}
		if (this.autoConnect) {
			try {
				this.connect();
			} catch (IOException exception) {
				throw new ConfigurationException(configuration, exception);
			}
		}
	}

	@Override
	public synchronized void addReceiver(final Receiver<Message> reciever) throws IOException {
		this.receiverList.add(reciever);
		if (this.connected) {
			reciever.connect();
			this.startReciever(reciever);
		}
	}

	private void startReciever(final Receiver<Message> reciever) {
		new Thread() {
			@Override
			public void run() {
				for (Message message : reciever) {
					if (message != null) {
						MessengerProvider.this.receiveEventTransmitter.transmit(new MessengerEventData<Message>(MessengerProvider.this, reciever.getIdentifier(), MessengerEvent.RECEIVE, message));
					}
				}
			}
		}.start();
	}

	@Override
	public synchronized void addSender(Sender<Message> sender) throws IOException {
		this.senderMap.put(sender.getIdentifier(), sender);
		if (this.connected) {
			sender.connect();
		}
	}

	@Override
	public synchronized void removeReceiver(final Receiver<Message> reciever) throws IOException {
		reciever.disconnect();
		this.receiverList.remove(reciever);
	}

	@Override
	public synchronized void removeSender(final Sender<Message> sender) throws IOException {
		sender.disconnect();
		this.senderMap.remove(sender);
	}

	@Override
	public synchronized String[] getReceivers() {
		return this.receiverList.toArray(new String[this.receiverList.size()]);
	}

	@Override
	public synchronized String[] getSenders() {
		Set<String> keySet = this.senderMap.keySet();
		return keySet.toArray(new String[keySet.size()]);
	}

}
