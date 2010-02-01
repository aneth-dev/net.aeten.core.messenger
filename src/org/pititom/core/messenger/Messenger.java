package org.pititom.core.messenger;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.pititom.core.controller.QueueNotifierController;
import org.pititom.core.extersion.ConfigurationException;
import org.pititom.core.extersion.Notifier;
import org.pititom.core.messenger.extension.MessengerAcknowledgeProtocol;
import org.pititom.core.stream.controller.StreamControllerConnection;
import org.pititom.core.stream.dada.StreamControllerConfiguration;
import org.pititom.core.stream.extension.Connection;

public class Messenger<Message, Acknowledge extends Enum<?>> implements
		Connection {

	private final String name;
	private final BlockingQueue<Message> emissionQueue;
	private final BlockingQueue<MessengerNotification<Message, Acknowledge>> notificationQueue;
	private final Map<MessengerEvent, Set<MessengerEventHandler<Message, Acknowledge>>> eventHandlers;
	private final StreamControllerConfiguration emissionConfiguration;
	private final StreamControllerConfiguration receptionConfiguration;
	private final QueueNotifierController<Message> emissionController;
	private final QueueNotifierController<MessengerNotification<Message, Acknowledge>> notifierController;
	private final Reciever reciever;
	private final MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol;
	private final Object acknowledgeMutex;
	private long waitingForAcknowledgeDeadLine;
	private MessengerEventData<Message, Acknowledge> currentEventData;
	private MessengerObjectOutputStream emissionStream;
	private MessengerObjectInputStream receptionStream;
	private Thread receptionThread;
	private StreamControllerConnection emissionConnection;
	private StreamControllerConnection receptionConnection;
	private boolean connected;

	public Messenger(MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol, String name, String emissionConfiguration, String receptionConfiguration) throws CmdLineException {
		this.name = name == null ? super.toString() : name;

		this.emissionConfiguration = new StreamControllerConfiguration(emissionConfiguration);
		this.receptionConfiguration = new StreamControllerConfiguration(receptionConfiguration);

		this.acknowledgeProtocol = acknowledgeProtocol;

		this.acknowledgeMutex = new Object();
		this.waitingForAcknowledgeDeadLine = 0L;

		this.connected = false;
		this.emissionQueue = new LinkedBlockingQueue<Message>();
		this.notificationQueue = new LinkedBlockingQueue<MessengerNotification<Message, Acknowledge>>();
		this.eventHandlers = new HashMap<MessengerEvent, Set<MessengerEventHandler<Message, Acknowledge>>>();
		for (MessengerEvent event : MessengerEvent.values()) {
			this.eventHandlers.put(event, new HashSet<MessengerEventHandler<Message, Acknowledge>>());
		}

		Messenger.this.currentEventData = null;

		this.reciever = new Reciever();
		this.notifierController = new QueueNotifierController<MessengerNotification<Message, Acknowledge>>(this.name + " : notifier", this.notificationQueue, new MessengerNotifier<Message, Acknowledge>());
		this.emissionController = new QueueNotifierController<Message>(this.name + " : emission controller", this.emissionQueue, new Transmitter());

	}

	public Messenger(String name, String emissionConfiguration, String receptionConfiguration) throws CmdLineException {
		this(null, name, emissionConfiguration, receptionConfiguration);
	}

	@Override
	public void connect() throws IOException {
		if (this.connected) {
			return;
		}

		this.notifierController.start();
		this.emissionController.start();

		try {
			final PipedInputStream pipedIn = new PipedInputStream();
			this.emissionConnection = new StreamControllerConnection(this.emissionConfiguration, pipedIn);
			this.emissionStream = new MessengerObjectOutputStream(new PipedOutputStream(pipedIn));

			final PipedOutputStream pipedOut = new PipedOutputStream();
			this.receptionConnection = new StreamControllerConnection(this.receptionConfiguration, pipedOut);
			this.receptionStream = new MessengerObjectInputStream(new PipedInputStream(pipedOut));
			this.receptionConnection.connect();
			this.emissionConnection.connect();
		} catch (ConfigurationException exception) {
			throw new IOException(exception);
		}

		this.receptionThread = new Thread(this.reciever);

		this.connected = true;
		this.receptionThread.start();

	}

	@Override
	public void disconnect() throws IOException {
		if (this.connected) {
			this.connected = false;
			this.receptionConnection.disconnect();
			this.emissionConnection.disconnect();
		}
	}

	public void addEventHandler(MessengerEventHandler<Message, Acknowledge> eventHandler, MessengerEvent... eventList) {
		synchronized (this.eventHandlers) {
			for (MessengerEvent event : eventList) {
				this.eventHandlers.get(event).add(eventHandler);
			}
		}
	}

	public void emit(Message message) {
		this.emissionQueue.add(message);
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public String toString() {
		return this.name;
	}

	private static class MessengerNotifier<Message, Acknowledge extends Enum<?>> implements
			Notifier<MessengerNotification<Message, Acknowledge>> {
		@Override
		public void notifyListener(MessengerNotification<Message, Acknowledge> notification) {
			for (MessengerEventHandler<Message, Acknowledge> eventHandler : this.getEventHandlers(notification)) {
				eventHandler.handleEvent(notification.getMessenger(), notification.getEvent(), notification.getEventData());
			}
		}

		private List<MessengerEventHandler<Message, Acknowledge>> getEventHandlers(MessengerNotification<Message, Acknowledge> notification) {
			List<MessengerEventHandler<Message, Acknowledge>> copy;
			synchronized (notification.getMessenger().eventHandlers) {
				Set<MessengerEventHandler<Message, Acknowledge>> eventHandlers = notification.getMessenger().eventHandlers.get(notification.getEvent());
				copy = new ArrayList<MessengerEventHandler<Message, Acknowledge>>(eventHandlers.size());
				for (MessengerEventHandler<Message, Acknowledge> eventHandler : eventHandlers) {
					copy.add(eventHandler);
				}
			}
			return copy;
		}
	}

	private class Transmitter implements Notifier<Message> {
		@Override
		public void notifyListener(Message message) {
			try {
				long now = System.currentTimeMillis();
				for (; now < Messenger.this.waitingForAcknowledgeDeadLine; now = System.currentTimeMillis()) {

					if (Messenger.this.currentEventData.getAcknowledge() == null) {
						synchronized (Messenger.this.acknowledgeMutex) {
							Messenger.this.acknowledgeMutex.wait(Messenger.this.waitingForAcknowledgeDeadLine - now);
						}
					}
					now = System.currentTimeMillis();

					if (Messenger.this.waitingForAcknowledgeDeadLine < now) {
						break;
					}
					Messenger.this.waitingForAcknowledgeDeadLine = 0;
					final MessengerNotification<Message, Acknowledge> notification;
					if (Messenger.this.currentEventData.getRecievedMessage() == null) {
						notification = new MessengerNotification<Message, Acknowledge>(Messenger.this, MessengerEvent.UNACKNOWLEDGED, Messenger.this.currentEventData);
					} else {
						final boolean success = (Messenger.this.currentEventData.getAcknowledge() == null) ? false : Messenger.this.acknowledgeProtocol.isSuccess(Messenger.this.currentEventData.getAcknowledge());
						notification = new MessengerNotification<Message, Acknowledge>(Messenger.this, success ? MessengerEvent.ACKNOWLEDGED : MessengerEvent.UNACKNOWLEDGED, Messenger.this.currentEventData);
					}
					Messenger.this.notificationQueue.put(notification);
				}

				Messenger.this.currentEventData = new MessengerEventData<Message, Acknowledge>(message, null, null);
				if (Messenger.this.acknowledgeProtocol == null) {
					Messenger.this.waitingForAcknowledgeDeadLine = 0;
				} else {
					long timeout = Messenger.this.acknowledgeProtocol.getAcknowledgedTimeout(message);
					Messenger.this.waitingForAcknowledgeDeadLine = timeout > 0 ? now + timeout : 0;
				}
				Messenger.this.emissionStream.writeObject(message);
				Messenger.this.notificationQueue.put(new MessengerNotification<Message, Acknowledge>(Messenger.this, MessengerEvent.SENT, new MessengerEventData<Message, Acknowledge>(message, null, null)));

			} catch (Exception exception) {
				Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, exception);
			}
		}

	}

	private class Reciever implements Runnable {
		@Override
		public void run() {
			try {
				while (Messenger.this.connected) {
					try {
						Object bean = Messenger.this.receptionStream.readObject();
						@SuppressWarnings("unchecked")
						Message recievedMessage = (Message) bean;
						Messenger.this.notificationQueue.put(new MessengerNotification<Message, Acknowledge>(Messenger.this, MessengerEvent.RECIEVED, new MessengerEventData<Message, Acknowledge>(null, recievedMessage, null)));

						if (Messenger.this.waitingForAcknowledgeDeadLine < System.currentTimeMillis()) {
							continue;
						}
						final Acknowledge acknowledge = Messenger.this.acknowledgeProtocol.getAcknowledge(Messenger.this.currentEventData.getSentMessage(), recievedMessage);

						if (acknowledge != null) {
							Messenger.this.currentEventData.setRecievedMessage(recievedMessage);
							Messenger.this.currentEventData.setAcknowledge(acknowledge);
							synchronized (Messenger.this.acknowledgeMutex) {
								Messenger.this.acknowledgeMutex.notifyAll();
							}
						}

					} catch (ClassCastException exception) {
						Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, exception);
					} catch (ClassNotFoundException exception) {
						Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, exception);
					}
				}
			} catch (Exception exception) {
				Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, exception);
			}
		}
	}
}
