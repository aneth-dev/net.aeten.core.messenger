package org.pititom.core.messenger.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.args4j.CommandLineParserHelper;
import org.pititom.core.event.Handler;
import org.pititom.core.event.Hook;
import org.pititom.core.event.HookEvent;
import org.pititom.core.event.Priority;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.Transmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.args4j.ReceiverOptionHandler;
import org.pititom.core.messenger.args4j.SenderOptionHandler;
import org.pititom.core.messenger.service.Messenger;
import org.pititom.core.messenger.service.Receiver;
import org.pititom.core.messenger.service.Sender;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerProvider<Message> implements Messenger<Message>, Configurable, Handler<MessengerEventData<Message>> {
	static {
		CmdLineParser.registerHandler(Sender.class, SenderOptionHandler.class);
		CmdLineParser.registerHandler(Receiver.class, ReceiverOptionHandler.class);
	}

	@Option(name = "-id", aliases = "--identifier", required = true)
	private String identifier;

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

	private Map<String, Sender<Message>> senderMap = new LinkedHashMap<String, Sender<Message>>();

	private boolean connected;

	private Transmitter<MessengerEventData<Message>> asyncSendEventTransmitter;
	private RegisterableTransmitter<Messenger<Message>, HookEvent<MessengerEvent, Hook>, MessengerEventData<Message>> hookTransmitter;

	/** @deprecated Reserved to configuration building */
	@SuppressWarnings("unchecked")
	public MessengerProvider() {
		this(null, Thread.NORM_PRIORITY, new Sender[0], new Receiver[0]);
	}

	@SuppressWarnings("unchecked")
	public MessengerProvider(String identifier) {
		this(identifier, Thread.NORM_PRIORITY, new Sender[0], new Receiver[0]);
	}

	@SuppressWarnings("unchecked")
	public MessengerProvider(String identifier, int threadPriority) {
		this(identifier, threadPriority, new Sender[0], new Receiver[0]);
	}

	@SuppressWarnings("unchecked")
	public MessengerProvider(String identifier, int threadPriority, Sender<Message> sender, Receiver<Message> receiver) {
		this(identifier, threadPriority, new Sender[] { sender }, new Receiver[] { receiver });
	}

	@SuppressWarnings("unchecked")
	public MessengerProvider(String identifier, int threadPriority, Sender<Message>[] senderList, Receiver<Message>[] receiverList) {
		this.identifier = identifier;
		this.threadPriority = threadPriority;

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

		this.hookTransmitter = TransmitterFactory.synchronous();
		if (this.identifier == null) {
			this.asyncSendEventTransmitter = null;
		} else {
			this.asyncSendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter", this.threadPriority, this, EVENTS.get(MessengerEvent.SEND, Hook.PRE));
		}
	}

	@Override
	public void transmit(Message message, String contact, Priority priority) {
		this.asyncSendEventTransmitter.transmit(new MessengerEventData<Message>(this, contact, MessengerEvent.SEND, Hook.PRE, message, priority));
	}

	@Override
	public void transmit(Message message, String contact) {
		this.transmit(message, contact, Priority.MEDIUM);
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
					this.startReciever(reciever);
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
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public void addEventHandler(Handler<MessengerEventData<Message>> eventHandler, HookEvent<MessengerEvent, Hook>... eventList) {
		this.hookTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<MessengerEventData<Message>> eventHandler, HookEvent<MessengerEvent, Hook>... eventList) {
		this.hookTransmitter.removeEventHandler(eventHandler, eventList);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public synchronized void configure(String configuration) throws ConfigurationException {
		CommandLineParserHelper.configure(this, configuration);
		if (this.threadPriority < 0) {
			this.threadPriority = Thread.NORM_PRIORITY;
		}
		if (this.asyncSendEventTransmitter == null) {
			this.asyncSendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter", this.threadPriority, this, EVENTS.get(MessengerEvent.SEND, Hook.PRE));
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

	private void startReciever(final Receiver<Message> receiver) {
		new Thread("Receiver " + receiver.getIdentifier()) {
			@Override
			public void run() {
				while (receiver.isConnected()) {

					MessengerEventData<Message> data = new MessengerEventData<Message>(MessengerProvider.this, receiver.getIdentifier(), MessengerEvent.RECEIVE, Hook.PRE, null);
					MessengerProvider.this.hookTransmitter.transmit(data);

					if (data.doIt()) {
						MessengerProvider.this.hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

						try {
							data.setMessage(receiver.receive());
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

						MessengerProvider.this.hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
						MessengerProvider.this.hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
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
			// Data event is already MessengerEvent.SEND, Hook.PRE
			this.hookTransmitter.transmit(data);

			if (data.doIt()) {
				this.hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				Sender<Message> sender = MessengerProvider.this.senderMap.get(data.getContact());
				try {
					sender.send(data.getMessage());
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

}
