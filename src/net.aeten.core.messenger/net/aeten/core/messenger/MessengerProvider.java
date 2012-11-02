package net.aeten.core.messenger;

import java.io.IOException;
import java.util.ArrayList;
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
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;

/**
 *
 * @author Thomas Pérennou
 */
@Provider(Messenger.class)
public class MessengerProvider<Message> implements
		Messenger<Message>,
		Handler<MessengerEventData<Message>> {

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

	@SuppressWarnings("unchecked")
	public MessengerProvider(@SpiInitializer MessengerInitializer init)
			throws IOException {
		identifier = init.getIdentifier ();
		senders = init.hasSenders () ? init.getSenders () : new HashMap<> ();
		receivers = init.hasReceivers () ? init.getReceivers () : new ArrayList<> ();
		autoConnect = init.hasAutoConnect () ? init.getAutoConnect () : false;
		this.hookTransmitter = TransmitterFactory.synchronous (EVENTS.values ());
		if (this.identifier == null) {
			this.asyncSendEventTransmitter = null;
		} else {
			this.asyncSendEventTransmitter = TransmitterFactory.asynchronous ("Sender transmitter of Messenger " + this.identifier, EVENTS.values (), this, EVENTS.get (MessengerEvent.SEND, Hook.PRE));
		}
		if (autoConnect) {
			connect ();
		}
	}

	@SuppressWarnings("unchecked")
	protected MessengerProvider(String identifier) {
		this (identifier, new Sender[0], new Receiver[0], true);
	}

	@SuppressWarnings("unchecked")
	protected MessengerProvider(String identifier,
			Sender<Message> sender,
			Receiver<Message> receiver) {
		this (identifier, new Sender[] {
			sender
		}, new Receiver[] {
			receiver
		}, true);
	}

	@SuppressWarnings("unchecked")
	protected MessengerProvider(String identifier,
			Sender<Message>[] senderList,
			Receiver<Message>[] receiverList,
			boolean autoConnect) {
		this.identifier = identifier;
		this.autoConnect = autoConnect;
		senders = new HashMap<> ();
		receivers = new ArrayList<> ();
		for (Sender<Message> sender : senderList) {
			try {
				this.addSender (sender);
			} catch (IOException exception) {
				Logger.log (sender, LogLevel.ERROR, "Sender \"" + sender.getIdentifier () + "\" has thrown an exception.", exception);
			}
		}

		for (Receiver<Message> reciever : receiverList) {
			try {
				this.addReceiver (reciever);
			} catch (IOException exception) {
				Logger.log (reciever, LogLevel.ERROR, "Receiver \"" + reciever.getIdentifier () + "\" has thrown an exception.", exception);
			}
		}

		this.hookTransmitter = TransmitterFactory.synchronous (EVENTS.values ());
		if (this.identifier == null) {
			this.asyncSendEventTransmitter = null;
		} else {
			this.asyncSendEventTransmitter = TransmitterFactory.asynchronous ("Sender transmitter of Messenger " + this.identifier, EVENTS.values (), this, EVENTS.get (MessengerEvent.SEND, Hook.PRE));
		}
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message,
			String sender,
			Priority priority) {
		return this.transmit (message, sender, null, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message,
			String sender) {
		return this.transmit (message, sender, null, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message) {
		return this.transmit (message, this.senders.get (0).getIdentifier (), null, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message,
			String sender,
			String contact) {
		return this.transmit (message, sender, contact, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message,
			String sender,
			String contact,
			String service) {
		return this.transmit (message, sender, contact, service, Priority.MEDIUM);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message,
			String sender,
			String contact,
			Priority priority) {
		return this.transmit (message, sender, contact, null, priority);
	}

	@Override
	public Future<MessengerEventData<Message>> transmit(Message message,
			String sender,
			String contact,
			String service,
			Priority priority) {
		MessengerEventData<Message> data = new MessengerEventData<Message> (this, contact, service, MessengerEvent.SEND, Hook.PRE, message, priority);
		data.setSubcontractor (sender);
		return this.asyncSendEventTransmitter.transmit (data);
	}

	@Override
	public String toString() {
		return "Messenger \"" + this.getIdentifier () + "\"";
	}

	@Override
	public synchronized void connect()
			throws IOException {
		if (!this.connected) {
			MessengerEventData<Message> data = new MessengerEventData<Message> (this, null, MessengerEvent.CONNECT, Hook.PRE, null);
			this.hookTransmitter.transmit (data);

			if (data.doIt ()) {
				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.START));

				for (Receiver<Message> reciever : this.receivers) {
					reciever.connect ();
					this.startReceiver (reciever);
				}
				for (Sender<Message> sender : this.senders.values ()) {
					sender.connect ();
				}
				this.connected = true;

				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.END));
				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.POST));
			}
		}
	}

	@Override
	public synchronized void disconnect()
			throws IOException {
		if (this.connected) {
			MessengerEventData<Message> data = new MessengerEventData<Message> (this, null, MessengerEvent.DISCONNECT, Hook.PRE, null);
			this.hookTransmitter.transmit (data);

			if (data.doIt ()) {
				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.START));

				for (Receiver<Message> reciever : this.receivers) {
					reciever.disconnect ();
				}
				for (Sender<Message> sender : this.senders.values ()) {
					sender.disconnect ();
				}
				this.connected = false;

				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.END));
				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.POST));
			}
		}
	}

	@Override
	public void addEventHandler(Handler<MessengerEventData<Message>> eventHandler,
			@SuppressWarnings("unchecked") HookEvent<MessengerEvent, Hook>... eventList) {
		this.hookTransmitter.addEventHandler (eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<MessengerEventData<Message>> eventHandler,
			@SuppressWarnings("unchecked") HookEvent<MessengerEvent, Hook>... eventList) {
		this.hookTransmitter.removeEventHandler (eventHandler, eventList);
	}

	@Override
	public synchronized void addReceiver(final Receiver<Message> receiver)
			throws IOException {
		this.receivers.add (receiver);
		if (this.connected) {
			receiver.connect ();
			this.startReceiver (receiver);
		}
	}

	private void startReceiver(final Receiver<Message> receiver) {
		new Thread ("Receiver " + receiver.getIdentifier ()) {

			@Override
			public void run() {
				while (receiver.isConnected ()) {
					MessengerEventData<Message> data = new MessengerEventData<Message> (MessengerProvider.this, null, MessengerEvent.RECEIVE, Hook.PRE, null);
					data.setSubcontractor (receiver.getIdentifier ());
					MessengerProvider.this.hookTransmitter.transmit (data);

					if (data.doIt ()) {
						MessengerProvider.this.hookTransmitter.transmit (EVENTS.hook (data, Hook.START));

						try {
							receiver.receive (data);
							MessengerProvider.this.hookTransmitter.transmit (EVENTS.hook (data, Hook.END));
							MessengerProvider.this.hookTransmitter.transmit (EVENTS.hook (data, Hook.POST));
						} catch (IOException exception) {
							try {
								Logger.log (receiver, LogLevel.ERROR, exception);
								receiver.disconnect ();
							} catch (IOException disconnectException) {
								Logger.log (receiver, LogLevel.ERROR, "Unable to disconnect receiver \"" + receiver.getIdentifier () + "\"", disconnectException);
							}
						} catch (Throwable exception) {
							Logger.log (receiver, LogLevel.ERROR, exception);
						}
					}

				}
			}
		}.start ();
	}

	@Override
	public synchronized void addSender(Sender<Message> sender)
			throws IOException {
		this.senders.put (sender.getIdentifier (), sender);
		if (this.connected) {
			sender.connect ();
		}
	}

	@Override
	public synchronized void removeReceiver(final Receiver<Message> reciever)
			throws IOException {
		reciever.disconnect ();
		this.receivers.remove (reciever);
	}

	@Override
	public synchronized void removeSender(final Sender<Message> sender)
			throws IOException {
		sender.disconnect ();
		this.senders.remove (sender);
	}

	@Override
	public synchronized String[] getReceivers() {
		return this.receivers.toArray (new String[this.receivers.size ()]);
	}

	@Override
	public synchronized String[] getSenders() {
		Set<String> keySet = this.senders.keySet ();
		return keySet.toArray (new String[keySet.size ()]);
	}

	@Override
	public void handleEvent(MessengerEventData<Message> data) {

		if (this.connected) {
			Sender<Message> sender = MessengerProvider.this.senders.get (data.getSubcontractor ());
			// Data event is already MessengerEvent.SEND, Hook.PRE
			this.hookTransmitter.transmit (data);

			if (data.doIt ()) {
				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.START));

				try {
					sender.send (data);
				} catch (IOException exception) {
					try {
						Logger.log (sender, LogLevel.ERROR, exception);
						sender.disconnect ();
					} catch (IOException disconnectException) {
						Logger.log (sender, LogLevel.ERROR, "Unable to disconnect sender \"" + sender.getIdentifier () + "\"", disconnectException);
					}
				} catch (Throwable exception) {
					Logger.log (sender, LogLevel.ERROR, exception);
				}

				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.END));
				this.hookTransmitter.transmit (EVENTS.hook (data, Hook.POST));
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
