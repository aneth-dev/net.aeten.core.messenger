package org.pititom.core.messenger;

import java.io.IOException;

import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Factory;
import org.pititom.core.args4j.CommandLineParser;
import org.pititom.core.event.Handler;
import org.pititom.core.event.Transmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.RegisterableForwarder;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.messenger.extension.Messenger;

/**
 *
 * @author Thomas Pérennou
 */
public abstract class AbstractMessenger<Message, Acknowledge extends Enum<?>>  implements Messenger<Message, Acknowledge>, Configurable {

	@Option(name = "-n", aliases = "--name", required = false)
	private String name;
	@Option(name = "-h", aliases = "--hook", required = false)
	private Factory<DefaultMessengerHooks<Message, Acknowledge>> hookFactory;
	
	private Transmitter<MessengerHook, MessengerHookData<Message, Acknowledge>> hookEventTransmitter;
	private final MessengerEventData<Message, Acknowledge> currentEventData;
	private final MessageTransmitter messageTransmitter;
	private Transmitter<MessengerEvent, Message> internalEventTransmitter;
	private RegisterableTransmitter<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventTransmitter;
	private boolean connected;

	public AbstractMessenger() {
		this(null, null);
	}
	public AbstractMessenger(String name) {
		this(name, null);
	}
	public AbstractMessenger(String name, DefaultMessengerHooks<Message, Acknowledge> hook) {
		this.name = name == null ? super.toString() : name;
		if (hook != null) {
			this.hookEventTransmitter = TransmitterFactory.synchronous(this, hook, MessengerHook.START_RECEPTION, MessengerHook.START_SEND);
		}
		
		this.currentEventData = new MessengerEventData<Message, Acknowledge>();
		this.messageTransmitter = new MessageTransmitter();

		this.eventTransmitter = null;
		this.internalEventTransmitter = null;
	}

	public void transmit(Message message) {
		if (this.connected) {
			this.internalEventTransmitter.transmit(MessengerEvent.SEND, message);
		}
	}

	protected abstract void send(Message message);

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

	private class MessageTransmitter implements
			Handler<Messenger<Message, Acknowledge>, MessengerEvent, Message> {

		@Override
		public void handleEvent(Messenger<Message, Acknowledge> source, MessengerEvent event, Message message) {
			MessengerHookData<Message, Acknowledge> hookData = new MessengerHookData<Message, Acknowledge>(AbstractMessenger.this.eventTransmitter);
			hookData.setCurrentEventData(AbstractMessenger.this.currentEventData);
			hookData.setMessageToSend(message);

			AbstractMessenger.this.hookEventTransmitter.transmit(MessengerHook.START_SEND, hookData);
			AbstractMessenger.this.send(message);
			AbstractMessenger.this.hookEventTransmitter.transmit(MessengerHook.END_SEND, hookData);

			AbstractMessenger.this.eventTransmitter.transmit(MessengerEvent.SENT, new MessengerEventData<Message, Acknowledge>(message, null, null));

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
	public void addEventHandler(Handler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventHandler, MessengerEvent... eventList) {
		this.eventTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventHandler, MessengerEvent... eventList) {
		this.eventTransmitter.removeEventHandler(eventHandler, eventList);
	}

	protected void doConnect() throws IOException {
		this.internalEventTransmitter = TransmitterFactory.asynchronous(
				"Messenger \"" + this.name + "\" internal event transmitter",
				this, this.messageTransmitter, MessengerEvent.SEND);
		this.eventTransmitter = TransmitterFactory.<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>asynchronous(
				"Messenger \"" + this.name + "\" event transmitter",
				this);
		this.connected = true;
	}

	protected void doDisconnect() throws IOException {
		this.eventTransmitter = null;
		this.internalEventTransmitter = null;
		this.connected = false;
	}

	protected void setConnected(boolean connected) {
		this.connected = connected;
	}

	protected void doRecieve(Message message) {
		if (!this.connected) {
			return;
		}

		final MessengerHookData<Message, Acknowledge> hookData = new MessengerHookData<Message, Acknowledge>(AbstractMessenger.this.eventTransmitter);
		hookData.setCurrentEventData(AbstractMessenger.this.currentEventData);
		hookData.setRecievedMessage(message);

		this.hookEventTransmitter.transmit(MessengerHook.START_RECEPTION, hookData);
		this.eventTransmitter.transmit(MessengerEvent.RECIEVED, new MessengerEventData<Message, Acknowledge>(null, message, null));
		this.hookEventTransmitter.transmit(MessengerHook.END_RECEPTION, hookData);
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		if (this.hookFactory != null) {
			throw new ConfigurationException(configuration, "Messenger \"" + this.name + "\" is allready configured");
		}
		CommandLineParser commandLineParser = new CommandLineParser(this);
		try {
			commandLineParser.parseArgument(CommandLineParser.splitArguments(configuration));
			this.hookEventTransmitter = TransmitterFactory.synchronous(this, this.hookFactory.getInstance(), MessengerHook.START_RECEPTION, MessengerHook.START_SEND);
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}
}
