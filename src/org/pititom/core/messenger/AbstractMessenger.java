package org.pititom.core.messenger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.kohsuke.args4j.Option;
import org.pititom.core.ContributionFactory;
import org.pititom.core.args4j.CommandLineParser;
import org.pititom.core.controller.EventEntry;
import org.pititom.core.controller.EventForwarder;
import org.pititom.core.controller.QueueEventForwarder;
import org.pititom.core.extersion.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.extersion.EventHandler;
import org.pititom.core.messenger.extension.Messenger;

public abstract class AbstractMessenger<Message, Acknowledge extends Enum<?>>   implements
		Messenger<Message, Acknowledge>, Configurable {

	@Option(name = "-h", aliases = "--hook", required = false)
	private ContributionFactory<DefaultMessengerHooks<Message, Acknowledge>> hookFactory;

	
	private final String name;
	private final EventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventForwarder;
	private final EventForwarder<Messenger<Message, Acknowledge>, MessengerHook, MessengerHookData<Message, Acknowledge>> hookForwarder;
	private final BlockingQueue<EventEntry<Messenger<Message, Acknowledge>, Enum<?>, Message>> emissionQueue;
	private final BlockingQueue<EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>> notificationQueue;
	private QueueEventForwarder<Messenger<Message, Acknowledge>, Enum<?>, Message> emissionController;
	private QueueEventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> notifierController;
	private MessengerEventData<Message, Acknowledge> currentEventData;
	private boolean connected;

	public AbstractMessenger(String name) {
		this.name = name == null ? super.toString() : name;

		this.eventForwarder = new EventForwarder<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>();
		this.hookForwarder = new EventForwarder<Messenger<Message, Acknowledge>, MessengerHook, MessengerHookData<Message, Acknowledge>>();

		this.emissionQueue = new LinkedBlockingQueue<EventEntry<Messenger<Message, Acknowledge>, Enum<?>, Message>>();
		this.notificationQueue = new LinkedBlockingQueue<EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>>();

		this.currentEventData = new MessengerEventData<Message, Acknowledge>();

		this.notifierController = null;
		this.emissionController = null;
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

		@Override
		public void handleEvent(Messenger<Message, Acknowledge> source, Enum<?> event, Message message) {
			MessengerHookData<Message, Acknowledge> hookData = new MessengerHookData<Message, Acknowledge>(AbstractMessenger.this.notificationQueue);
			hookData.setCurrentEventData(AbstractMessenger.this.currentEventData);
			hookData.setMessageToSend(message);

			AbstractMessenger.this.hookForwarder.forward(AbstractMessenger.this, MessengerHook.START_SEND, hookData);
			AbstractMessenger.this.sendMessage(message);
			AbstractMessenger.this.hookForwarder.forward(AbstractMessenger.this, MessengerHook.END_SEND, hookData);

			AbstractMessenger.this.notificationQueue.add(new MessengerNotification<Message, Acknowledge>(AbstractMessenger.this, MessengerEvent.SENT, new MessengerEventData<Message, Acknowledge>(message, null, null)));

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

	public void addHook(EventHandler<Messenger<Message, Acknowledge>, MessengerHook, MessengerHookData<Message, Acknowledge>> hookHandler, MessengerHook... hookList) {
		if (hookHandler != null) {
			this.hookForwarder.addEventHandler(hookHandler, hookList);
		}
	}

	public void removeHook(EventHandler<Messenger<Message, Acknowledge>, MessengerHook, MessengerHookData<Message, Acknowledge>> hookHandler, MessengerHook... hookList) {
		this.hookForwarder.removeEventHandler(hookHandler, hookList);
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

		MessengerHookData<Message, Acknowledge> hookData = new MessengerHookData<Message, Acknowledge>(AbstractMessenger.this.notificationQueue);
		hookData.setCurrentEventData(AbstractMessenger.this.currentEventData);
		hookData.setRecievedMessage(message);

		this.hookForwarder.forward(this, MessengerHook.START_RECEPTION, hookData);
		this.notificationQueue.add(new MessengerNotification<Message, Acknowledge>(AbstractMessenger.this, MessengerEvent.RECIEVED, new MessengerEventData<Message, Acknowledge>(null, message, null)));
		this.hookForwarder.forward(this, MessengerHook.END_RECEPTION, hookData);

	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		if (this.hookFactory != null) {
			throw new ConfigurationException(configuration, this.name + " is allready configured");
		}
		CommandLineParser commandLineParser = new CommandLineParser(this);
		try {
			commandLineParser.parseArgument(CommandLineParser.splitArguments(configuration));
			this.addHook(this.hookFactory.getInstance(), MessengerHook.START_RECEPTION, MessengerHook.START_SEND);
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}
}
