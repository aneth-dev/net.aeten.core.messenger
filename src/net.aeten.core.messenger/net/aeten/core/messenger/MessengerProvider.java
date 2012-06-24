package net.aeten.core.messenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Format;
import net.aeten.core.args4j.CommandLineParserHelper;
import net.aeten.core.event.Handler;
import net.aeten.core.event.Hook;
import net.aeten.core.event.HookEvent;
import net.aeten.core.event.Priority;
import net.aeten.core.event.RegisterableTransmitter;
import net.aeten.core.event.Transmitter;
import net.aeten.core.event.TransmitterFactory;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.args4j.ReceiverOptionHandler;
import net.aeten.core.messenger.args4j.SenderOptionHandler;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 
 * @author Thomas Pérennou
 */
@Provider(Messenger.class)
@Format("args")
public class MessengerProvider<Message> implements Messenger<Message>, Handler<MessengerEventData<Message>> {
	static {
		CmdLineParser.registerHandler(Sender.class, SenderOptionHandler.class);
		CmdLineParser.registerHandler(Receiver.class, ReceiverOptionHandler.class);
	}

	@Option(name = "-id", aliases = "--identifier", required = true)
	private final String identifier;

	@Option(name = "-s", aliases = "--sender", required = false)
	private final List<Sender<Message>> senderList = new ArrayList<>();

	@Option(name = "-r", aliases = "--receiver", required = false)
	private final List<Receiver<Message>> receiverList = new ArrayList<>();

	@Option(name = "-c", aliases = "--auto-connect", required = false)
	private final boolean autoConnect;

	private final Map<String, Sender<Message>> senderMap = new LinkedHashMap<>();

	private volatile boolean connected;

	private Transmitter<MessengerEventData<Message>> asyncSendEventTransmitter;
	private RegisterableTransmitter<HookEvent<MessengerEvent, Hook>, MessengerEventData<Message>> hookTransmitter;

	/** @deprecated Reserved to configuration building */
	@Deprecated
	public MessengerProvider() {
		this(null, new Sender[0], new Receiver[0], true);
	}

	protected MessengerProvider(String identifier) {
		this(identifier, new Sender[0], new Receiver[0], true);
	}

	protected MessengerProvider(String identifier, Sender<Message> sender, Receiver<Message> receiver) {
		this(identifier, new Sender[] { sender }, new Receiver[] { receiver }, true);
	}

	@SpiInitializer
	protected MessengerProvider(String identifier, Sender<Message>[] senderList, Receiver<Message>[] receiverList, boolean autoConnect) {
		this.identifier = identifier;
		this.autoConnect = autoConnect;
		for (Sender<Message> sender : senderList) {
			try {
				this.addSender(sender);
			} catch (IOException exception) {
				Logger.log(sender, LogLevel.ERROR, "Sender \"" + sender.getIdentifier() + "\" has thrown an exception.", exception);
			}
		}

		for (Receiver<Message> reciever : receiverList) {
			try {
				this.addReceiver(reciever);
			} catch (IOException exception) {
				Logger.log(reciever, LogLevel.ERROR, "Receiver \"" + reciever.getIdentifier() + "\" has thrown an exception.", exception);
			}
		}

		this.hookTransmitter = TransmitterFactory.synchronous(EVENTS.values());
		if (this.identifier == null) {
			this.asyncSendEventTransmitter = null;
		} else {
			this.asyncSendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter of Messenger " + this.identifier, EVENTS.values(), this, EVENTS.get(MessengerEvent.SEND, Hook.PRE));
		}
	}

	@Override
	public void transmit(Message message, String sender, Priority priority) {
		this.transmit(message, sender, null, Priority.MEDIUM);
	}

	@Override
	public void transmit(Message message, String sender) {
		this.transmit(message, sender, null, Priority.MEDIUM);
	}

	@Override
	public void transmit(Message message) {
		this.transmit(message, this.senderList.get(0).getIdentifier(), null, Priority.MEDIUM);
	}

	@Override
	public void transmit(Message message, String sender, String contact) {
		this.transmit(message, sender, contact, Priority.MEDIUM);
	}

	@Override
	public void transmit(Message message, String sender, String contact, String service) {
		this.transmit(message, sender, contact, service, Priority.MEDIUM);
	}

	@Override
	public void transmit(Message message, String sender, String contact, Priority priority) {
		this.transmit(message, sender, contact, null, priority);
	}

	@Override
	public void transmit(Message message, String sender, String contact, String service, Priority priority) {
		MessengerEventData<Message> data = new MessengerEventData<Message>(this, contact, service, MessengerEvent.SEND, Hook.PRE, message, priority);
		data.setSubcontractor(sender);
		this.asyncSendEventTransmitter.transmit(data);
	}

	@Override
	public String toString() {
		return "Messenger \"" + this.getIdentifier() + "\"";
	}

	@Override
	public synchronized void connect() throws IOException {
		if (!this.connected) {
			MessengerEventData<Message> data = new MessengerEventData<Message>(this, null, MessengerEvent.CONNECT, Hook.PRE, null);
			this.hookTransmitter.transmit(data);

			if (data.doIt()) {
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				for (Receiver<Message> reciever : this.receiverList) {
					reciever.connect();
					this.startReceiver(reciever);
				}
				for (Sender<Message> sender : this.senderMap.values()) {
					sender.connect();
				}
				this.connected = true;

				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
			}
		}
	}

	@Override
	public synchronized void disconnect() throws IOException {
		if (this.connected) {
			MessengerEventData<Message> data = new MessengerEventData<Message>(this, null, MessengerEvent.DISCONNECT, Hook.PRE, null);
			this.hookTransmitter.transmit(data);

			if (data.doIt()) {
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				for (Receiver<Message> reciever : this.receiverList) {
					reciever.disconnect();
				}
				for (Sender<Message> sender : this.senderMap.values()) {
					sender.disconnect();
				}
				this.connected = false;

				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
			}
		}
	}

	@Override
	public void addEventHandler(Handler<MessengerEventData<Message>> eventHandler, HookEvent<MessengerEvent, Hook>... eventList) {
		this.hookTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<MessengerEventData<Message>> eventHandler, HookEvent<MessengerEvent, Hook>... eventList) {
		this.hookTransmitter.removeEventHandler(eventHandler, eventList);
	}

	@SuppressWarnings("unchecked")
	public synchronized void configure(String configuration) throws ConfigurationException {
		CommandLineParserHelper.configure(this, configuration);
		if (this.asyncSendEventTransmitter == null) {
			this.asyncSendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter of Messenger " + this.identifier, EVENTS.values(), this, EVENTS.get(MessengerEvent.SEND, Hook.PRE));
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
	public synchronized void addReceiver(final Receiver<Message> receiver) throws IOException {
		this.receiverList.add(receiver);
		if (this.connected) {
			receiver.connect();
			this.startReceiver(receiver);
		}
	}

	private void startReceiver(final Receiver<Message> receiver) {
		new Thread("Receiver " + receiver.getIdentifier()) {
			@Override
			public void run() {
				while (receiver.isConnected()) {
					MessengerEventData<Message> data = new MessengerEventData<Message>(MessengerProvider.this, null, MessengerEvent.RECEIVE, Hook.PRE, null);
					data.setSubcontractor(receiver.getIdentifier());
					MessengerProvider.this.hookTransmitter.transmit(data);

					if (data.doIt()) {
						MessengerProvider.this.hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

						try {
							receiver.receive(data);
							MessengerProvider.this.hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
							MessengerProvider.this.hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
						} catch (IOException exception) {
							try {
								Logger.log(receiver, LogLevel.ERROR, exception);
								receiver.disconnect();
							} catch (IOException disconnectException) {
								Logger.log(receiver, LogLevel.ERROR, "Unable to disconnect receiver \"" + receiver.getIdentifier() + "\"", disconnectException);
							}
						} catch (Throwable exception) {
							Logger.log(receiver, LogLevel.ERROR, exception);
						}
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

	@Override
	public void handleEvent(MessengerEventData<Message> data) {

		if (this.connected) {
			Sender<Message> sender = MessengerProvider.this.senderMap.get(data.getSubcontractor());
			// Data event is already MessengerEvent.SEND, Hook.PRE
			this.hookTransmitter.transmit(data);

			if (data.doIt()) {
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				try {
					sender.send(data);
				} catch (IOException exception) {
					try {
						Logger.log(sender, LogLevel.ERROR, exception);
						sender.disconnect();
					} catch (IOException disconnectException) {
						Logger.log(sender, LogLevel.ERROR, "Unable to disconnect sender \"" + sender.getIdentifier() + "\"", disconnectException);
					}
				} catch (Throwable exception) {
					Logger.log(sender, LogLevel.ERROR, exception);
				}

				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
			}
		}
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

}
