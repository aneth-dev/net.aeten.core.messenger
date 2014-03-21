package net.aeten.core.messenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import net.aeten.core.event.Handler;
import net.aeten.core.event.Hook;
import net.aeten.core.event.HookEvent;
import net.aeten.core.event.Priority;
import net.aeten.core.event.RegisterableTransmitter;
import net.aeten.core.event.Transmitter;
import net.aeten.core.event.TransmitterFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Pérennou
 */
@Provider(Messenger.class)
public class MessengerProvider<Message> implements Messenger<Message>, Handler<MessengerEventData<Message>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessengerProvider.class);

	@FieldInit(alias = "id")
	private final String identifier;
	@FieldInit(required = false)
	private final Map<String, Sender<Message>> senders;
	@FieldInit(required = false)
	private final List<Receiver<Message>> receivers;
	@FieldInit(required = false)
	private final boolean autoConnect;
	private volatile boolean connected;
	private Transmitter<MessengerEventData<Message>> asyncSendEventTransmitter;
	private RegisterableTransmitter<HookEvent<MessengerEvent, Hook>, MessengerEventData<Message>> hookTransmitter;
	private final MessengerInitializer init;

	@SuppressWarnings("unchecked")
	@SpiConstructor
	public MessengerProvider(MessengerInitializer init) throws IOException {
		this.init = init;
		identifier = init.getIdentifier();
		senders = new HashMap<>();
		receivers = new ArrayList<>();
		autoConnect = init.hasAutoConnect()? init.getAutoConnect(): false;
		hookTransmitter = TransmitterFactory.synchronous(EVENTS.values());
		if (identifier == null) {
			asyncSendEventTransmitter = null;
		} else {
			asyncSendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter of Messenger " + identifier, EVENTS.values(), this, EVENTS.get(MessengerEvent.SEND, Hook.PRE));
		}
		if (autoConnect) {
			connect();
		}
	}

	@SuppressWarnings("unchecked")
	protected MessengerProvider(String identifier) {
		this(identifier, new Sender[0], new Receiver[0], true);
	}

	@SuppressWarnings("unchecked")
	protected MessengerProvider(String identifier, Sender<Message> sender, Receiver<Message> receiver) {
		this(identifier, new Sender[] { sender
		}, new Receiver[] { receiver
		}, true);
	}

	@SuppressWarnings("unchecked")
	protected MessengerProvider(String identifier, Sender<Message>[] senderList, Receiver<Message>[] receiverList, boolean autoConnect) {
		init = null;
		this.identifier = identifier;
		this.autoConnect = autoConnect;
		senders = new HashMap<>();
		receivers = new ArrayList<>();
		for (Sender<Message> sender: senderList) {
			try {
				this.addSender(sender);
			} catch (IOException exception) {
				LOGGER.error("Sender \"" + sender.getIdentifier() + "\" has thrown an exception.", exception);
			}
		}

		for (Receiver<Message> reciever: receiverList) {
			try {
				this.addReceiver(reciever);
			} catch (IOException exception) {
				LOGGER.error("Receiver \"" + reciever.getIdentifier() + "\" has thrown an exception.", exception);
			}
		}

		hookTransmitter = TransmitterFactory.synchronous(EVENTS.values());
		if (identifier == null) {
			asyncSendEventTransmitter = null;
		} else {
			asyncSendEventTransmitter = TransmitterFactory.asynchronous("Sender transmitter of Messenger " + identifier, EVENTS.values(), this, EVENTS.get(MessengerEvent.SEND, Hook.PRE));
		}
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message, String sender, Priority priority) {
		return transmit(message, sender, null, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message, String sender) {
		return transmit(message, sender, null, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message) {
		return transmit(message, this.senders.get(0).getIdentifier(), null, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact) {
		return transmit(message, sender, contact, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact, String service) {
		return transmit(message, sender, contact, service, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact, Priority priority) {
		return transmit(message, sender, contact, null, priority);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact, String service, Priority priority) {
		MessengerEventData<Message> data = new MessengerEventData<Message>(this, contact, service, MessengerEvent.SEND, Hook.PRE, message, priority);
		data.setSubcontractor(sender);
		return asyncSendEventTransmitter.transmit(data);
	}

	@Override
	public String toString() {
		return "Messenger \"" + getIdentifier() + "\"";
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void connect() throws IOException {
		if (!connected) {
			if (init != null) {
				Map<String, Sender<Message>> newSenders = init.hasSenders()? init.getSenders(): Collections.emptyMap();
				Map<String, Sender<Message>> oldSenders = new HashMap<>();
				oldSenders.putAll(senders);
				for (String newSender: newSenders.keySet()) {
					oldSenders.remove(newSender);
				}
				senders.putAll(newSenders);

				List<Receiver<Message>> newReceivers = init.hasReceivers()? init.getReceivers(): Collections.emptyList();
				List<Receiver<Message>> oldReceivers = new ArrayList<>(receivers.size());
				oldReceivers.addAll(receivers);
				for (Receiver<Message> newReceiver: newReceivers) {
					for (Receiver<Message> receiver: oldReceivers) {
						if (receiver.getIdentifier().equals(newReceiver.getIdentifier())) {
							receivers.remove(receiver);
						}
					}
				}
				receivers.addAll(newReceivers);
			}
			MessengerEventData<Message> data = new MessengerEventData<Message>(this, null, MessengerEvent.CONNECT, Hook.PRE, null);
			hookTransmitter.transmit(data);

			if (data.doIt()) {
				hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				for (Receiver<Message> reciever: receivers) {
					reciever.connect();
					startReceiver(reciever);
				}
				for (Sender<Message> sender: senders.values()) {
					sender.connect();
				}
				connected = true;

				hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
				hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
			}
		}
	}

	@Override
	public synchronized void disconnect() throws IOException {
		if (connected) {
			MessengerEventData<Message> data = new MessengerEventData<Message>(this, null, MessengerEvent.DISCONNECT, Hook.PRE, null);
			hookTransmitter.transmit(data);

			if (data.doIt()) {
				hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				for (Receiver<Message> reciever: receivers) {
					reciever.disconnect();
				}
				for (Sender<Message> sender: senders.values()) {
					sender.disconnect();
				}
				connected = false;

				hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
				hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
			}
		}
	}

	@Override
	public void addEventHandler(Handler<MessengerEventData<Message>> eventHandler, @SuppressWarnings("unchecked") HookEvent<MessengerEvent, Hook>... eventList) {
		hookTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<MessengerEventData<Message>> eventHandler, @SuppressWarnings("unchecked") HookEvent<MessengerEvent, Hook>... eventList) {
		hookTransmitter.removeEventHandler(eventHandler, eventList);
	}

	@Override
	public synchronized void addReceiver(final Receiver<Message> receiver) throws IOException {
		receivers.add(receiver);
		if (connected) {
			receiver.connect();
			startReceiver(receiver);
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
								LOGGER.error("Receiver \"" + receiver.getIdentifier() + "\" I/O error", exception);
								receiver.disconnect();
							} catch (IOException disconnectException) {
								LOGGER.error("Unable to disconnect receiver \"" + receiver.getIdentifier() + "\"", disconnectException);
							}
						} catch (Throwable exception) {
							LOGGER.error("Receiver \"" + receiver.getIdentifier() + "\" error", exception);
						}
					}

				}
			}
		}.start();
	}

	@Override
	public synchronized void addSender(Sender<Message> sender) throws IOException {
		senders.put(sender.getIdentifier(), sender);
		if (connected) {
			sender.connect();
		}
	}

	@Override
	public synchronized void removeReceiver(final Receiver<Message> reciever) throws IOException {
		reciever.disconnect();
		receivers.remove(reciever);
	}

	@Override
	public synchronized void removeSender(final Sender<Message> sender) throws IOException {
		sender.disconnect();
		senders.remove(sender);
	}

	@Override
	public synchronized String[] getReceivers() {
		return receivers.toArray(new String[this.receivers.size()]);
	}

	@Override
	public synchronized String[] getSenders() {
		Set<String> keySet = senders.keySet();
		return keySet.toArray(new String[keySet.size()]);
	}

	@Override
	public void handleEvent(MessengerEventData<Message> data) {

		if (connected) {
			Sender<Message> sender = MessengerProvider.this.senders.get(data.getSubcontractor());
			// Data event is already MessengerEvent.SEND, Hook.PRE
			hookTransmitter.transmit(data);

			if (data.doIt()) {
				hookTransmitter.transmit(EVENTS.hook(data, Hook.START));

				try {
					sender.send(data);
				} catch (IOException exception) {
					try {
						LOGGER.error("Sender \"" + sender.getIdentifier() + "\" I/O error", exception);
						sender.disconnect();
					} catch (IOException disconnectException) {
						LOGGER.error("Unable to disconnect sender \"" + sender.getIdentifier() + "\"", disconnectException);
					}
				} catch (Throwable exception) {
					LOGGER.error("Sender \"" + sender.getIdentifier() + "\" error", exception);
				}

				hookTransmitter.transmit(EVENTS.hook(data, Hook.END));
				hookTransmitter.transmit(EVENTS.hook(data, Hook.POST));
			}
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

}
