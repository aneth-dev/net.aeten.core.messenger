package org.pititom.core.messenger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pititom.core.controller.EventEntry;
import org.pititom.core.controller.EventForwarder;
import org.pititom.core.controller.QueueEventForwarder;
import org.pititom.core.extersion.EventHandler;
import org.pititom.core.messenger.extension.Messenger;

public abstract class AbstractMessenger<Message, Acknowledge extends Enum<?>> implements
		Messenger<Message, Acknowledge> {

	private final String name;
	private final EventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventForwarder;
	private final BlockingQueue<EventEntry<Messenger<Message, Acknowledge>, Enum<?>, Message>> emissionQueue;
	private final BlockingQueue<EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>> notificationQueue;
	private final MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol;
	private final Object acknowledgeMutex;

	private QueueEventForwarder<Messenger<Message, Acknowledge>, Enum<?>, Message> emissionController;
	private QueueEventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> notifierController;
	private boolean waitingForAcknowledgeBlocking;
	private long waitingForAcknowledgeDeadLine;
	private MessengerEventData<Message, Acknowledge> currentEventData;
	private boolean connected;

	public AbstractMessenger(String name, MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol) {
		this.name = name == null ? super.toString() : name;
		this.acknowledgeProtocol = acknowledgeProtocol;

		this.eventForwarder = new EventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>();

		this.acknowledgeMutex = new Object();
		this.waitingForAcknowledgeBlocking = true;
		this.waitingForAcknowledgeDeadLine = 0L;

		this.emissionQueue = new LinkedBlockingQueue<EventEntry<Messenger<Message, Acknowledge>, Enum<?>, Message>>();
		this.notificationQueue = new LinkedBlockingQueue<EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>>();

		this.currentEventData = null;

		this.notifierController = null;
		this.emissionController = null;
	}

	public AbstractMessenger(String name) {
		this(name, null);
	}

	public void emit(Message message) {
		if (this.connected) {
			this.emissionQueue.add(new EventEntry<Messenger<Message, Acknowledge>, Enum<?>, Message>(this, null, message));
		}
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public void finalize() throws Throwable {
		this.disconnect();
	}

	private class Transmitter implements
			EventHandler<Messenger<Message, Acknowledge>, Enum<?>, Message> {
		private long now;

		@Override
		public void handleEvent(Messenger<Message, Acknowledge> source, Enum<?> event, Message message) {
			try {
				now = System.currentTimeMillis();
				for (; now < AbstractMessenger.this.waitingForAcknowledgeDeadLine; now = System.currentTimeMillis()) {
					if (AbstractMessenger.this.waitingForAcknowledgeBlocking && (AbstractMessenger.this.currentEventData.getAcknowledge() == null)) {
						synchronized (AbstractMessenger.this.acknowledgeMutex) {
							AbstractMessenger.this.acknowledgeMutex.wait(AbstractMessenger.this.waitingForAcknowledgeDeadLine - now);
						}
					}
					now = System.currentTimeMillis();

					if (AbstractMessenger.this.waitingForAcknowledgeDeadLine < now) {
						break;
					}
					AbstractMessenger.this.waitingForAcknowledgeDeadLine = 0;
					final MessengerNotification<Message, Acknowledge> notification;
					if (AbstractMessenger.this.currentEventData.getRecievedMessage() == null) {
						notification = new MessengerNotification<Message, Acknowledge>(AbstractMessenger.this, MessengerEvent.UNACKNOWLEDGED, AbstractMessenger.this.currentEventData);
					} else {
						final boolean success = (AbstractMessenger.this.currentEventData.getAcknowledge() == null) ? false : AbstractMessenger.this.acknowledgeProtocol.isSuccess(AbstractMessenger.this.currentEventData.getAcknowledge());
						notification = new MessengerNotification<Message, Acknowledge>(AbstractMessenger.this, success ? MessengerEvent.ACKNOWLEDGED : MessengerEvent.UNACKNOWLEDGED, AbstractMessenger.this.currentEventData);
					}
					AbstractMessenger.this.notificationQueue.put(notification);
				}

				AbstractMessenger.this.currentEventData = new MessengerEventData<Message, Acknowledge>(message, null, null);
				if (AbstractMessenger.this.acknowledgeProtocol == null) {
					AbstractMessenger.this.waitingForAcknowledgeDeadLine = 0;
				} else {
					long timeout = AbstractMessenger.this.acknowledgeProtocol.getAcknowledgedTimeout(message);
					AbstractMessenger.this.waitingForAcknowledgeDeadLine = timeout > 0 ? now + timeout : 0;

					// TODO: Does not works yet
					// AbstractMessenger.this.waitingForAcknowledgeBlocking =
					// AbstractMessenger.this.acknowledgeProtocol.isBlocking(AbstractMessenger.this.currentEventData.getSentMessage());
				}
				AbstractMessenger.this.sendMessage(message);
				AbstractMessenger.this.notificationQueue.put(new MessengerNotification<Message, Acknowledge>(AbstractMessenger.this, MessengerEvent.SENT, new MessengerEventData<Message, Acknowledge>(message, null, null)));

			} catch (Exception exception) {
				Logger.getLogger(AbstractMessenger.class.getName()).log(Level.SEVERE, null, exception);
			}
		}
	}

	@Override
	public final synchronized void connect() throws IOException {
		if (!this.connected) {
			this.doConnect();
		}
	}

	@Override
	public final synchronized void disconnect() throws IOException {
		if (this.connected) {
			this.doDisconnect();
		}
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public void addEventHandler(EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventHandler, MessengerEvent... eventList) {
		this.eventForwarder.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventHandler, MessengerEvent... eventList) {
		this.eventForwarder.removeEventHandler(eventHandler, eventList);
	}

	protected void doConnect() throws IOException {
		this.notifierController = new QueueEventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>(this.name + " notifier", this.notificationQueue, this.eventForwarder);
		this.emissionController = new QueueEventForwarder<Messenger<Message, Acknowledge>, Enum<?>, Message>(this.name + " : emission controller", this.emissionQueue, new Transmitter());

		this.notifierController.start();
		this.emissionController.start();

		this.connected = true;
	}

	protected void doDisconnect() throws IOException {
		this.notifierController.kill();
		this.emissionController.kill();
		this.connected = false;
	}

	protected void setConnected(boolean connected) {
		this.connected = connected;
	}

	protected abstract void sendMessage(Message message);

	protected void doReception(Message message) {
		if (!this.connected) {
			return;
		}
		try {
			AbstractMessenger.this.notificationQueue.put(new MessengerNotification<Message, Acknowledge>(AbstractMessenger.this, MessengerEvent.RECIEVED, new MessengerEventData<Message, Acknowledge>(null, message, null)));

			if (AbstractMessenger.this.waitingForAcknowledgeDeadLine < System.currentTimeMillis()) {
				return;
			}
			final Acknowledge acknowledge = AbstractMessenger.this.acknowledgeProtocol.getAcknowledge(AbstractMessenger.this.currentEventData.getSentMessage(), message);

			if (acknowledge != null) {
				AbstractMessenger.this.currentEventData.setRecievedMessage(message);
				AbstractMessenger.this.currentEventData.setAcknowledge(acknowledge);
				synchronized (AbstractMessenger.this.acknowledgeMutex) {
					AbstractMessenger.this.acknowledgeMutex.notifyAll();
				}
			}

		} catch (Exception exception) {
			Logger.getLogger(AbstractMessenger.class.getName()).log(Level.SEVERE, null, exception);
		}
	}
}
