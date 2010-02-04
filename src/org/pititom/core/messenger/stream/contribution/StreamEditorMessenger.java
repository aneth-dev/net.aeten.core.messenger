package org.pititom.core.messenger.stream.contribution;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.pititom.core.ConfigurationException;
import org.pititom.core.EventHandler;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.extension.Messenger;
import org.pititom.core.messenger.stream.MessengerObjectInputStream;
import org.pititom.core.messenger.stream.MessengerObjectOutputStream;
import org.pititom.core.stream.controller.StreamControllerConnection;
import org.pititom.core.stream.controller.StreamControllerConfiguration;

/**
*
* @author Thomas Pérennou
*/
public class StreamEditorMessenger<Message, Acknowledge extends Enum<?>> implements
		Messenger<Message, Acknowledge> {

	private final String name;
	private final String hookConfiguration;
	private final StreamControllerConfiguration emissionConfiguration;
	private final StreamControllerConfiguration[] receptionConfigurationList;
	private final StreamControllerConnection receptionConnectionList[];
	private final Map<EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>, Set<MessengerEvent>> eventHandlers;

	private StreamControllerConnection emissionConnection;

	private StreamMessenger<Message, Acknowledge> messenger = null;

	public StreamEditorMessenger(String name, boolean autoConnect, String hookConfiguration, String emissionConfiguration, String... receptionConfigurationList) throws ConfigurationException, IOException {
		this(name, hookConfiguration, emissionConfiguration, receptionConfigurationList);
		if (autoConnect) {
			this.connect();
		}
	}

	public StreamEditorMessenger(String name, String hookConfiguration, String emissionConfiguration, String... receptionConfigurationList) throws ConfigurationException {
		this.name = name;
		this.hookConfiguration = hookConfiguration;

		this.eventHandlers = new HashMap<EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>, Set<MessengerEvent>>();

		try {
			this.emissionConfiguration = new StreamControllerConfiguration(emissionConfiguration);
		} catch (CmdLineException exception) {
			throw new ConfigurationException(emissionConfiguration, exception);
		}

		this.receptionConfigurationList = new StreamControllerConfiguration[receptionConfigurationList.length];
		this.receptionConnectionList = new StreamControllerConnection[receptionConfigurationList.length];
		for (int i = 0; i < receptionConfigurationList.length; i++) {
			try {
				this.receptionConfigurationList[i] = new StreamControllerConfiguration(receptionConfigurationList[i]);
			} catch (CmdLineException exception) {
				throw new ConfigurationException(receptionConfigurationList[i], exception);
			}
		}
	}

	@Override
	public void emit(Message message) {
		if (this.isConnected()) {
			this.messenger.emit(message);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void addEventHandler(EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventHandler, MessengerEvent... eventList) {
		synchronized (this.eventHandlers) {
			Set<MessengerEvent> set = this.eventHandlers.get(eventHandler);
			if (set == null) {
				set = new HashSet<MessengerEvent>();
				this.eventHandlers.put(eventHandler, set);
			}
			for (MessengerEvent event : eventList) {
				set.add(event);
			}
		}
		if (this.messenger != null) {
			this.messenger.addEventHandler(eventHandler, eventList);
		}
	}

	@Override
	public void removeEventHandler(EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> eventHandler, MessengerEvent... eventList) {
		synchronized (this.eventHandlers) {
			Set<MessengerEvent> set = this.eventHandlers.get(eventHandler);
			if (set != null) {
				for (MessengerEvent event : eventList) {
					set.remove(event);
				}
				if (set.size() == 0) {
					this.eventHandlers.remove(eventHandler);
				}
			}
		}
		if (this.messenger != null) {
			this.messenger.removeEventHandler(eventHandler, eventList);
		}
	}

	@Override
	public void connect() throws IOException {
		if (this.messenger != null) {
			return;
		}
		try {
			final ObjectInputStream[] inputStreamList = new ObjectInputStream[this.receptionConfigurationList.length];
			PipedOutputStream pipedOut;
			for (int i = 0; i < this.receptionConfigurationList.length; i++) {
				pipedOut = new PipedOutputStream();
				this.receptionConnectionList[i] = new StreamControllerConnection(this.receptionConfigurationList[i], pipedOut);
				inputStreamList[i] = new MessengerObjectInputStream(new PipedInputStream(pipedOut));

				this.receptionConnectionList[i].connect();
			}

			final PipedInputStream pipedIn = new PipedInputStream();
			this.emissionConnection = new StreamControllerConnection(this.emissionConfiguration, pipedIn);
			ObjectOutputStream emissionStream = new MessengerObjectOutputStream(new PipedOutputStream(pipedIn));
			this.emissionConnection.connect();

			this.messenger = new StreamMessenger<Message, Acknowledge>(this.name, this.hookConfiguration, emissionStream, inputStreamList);
			synchronized (this.eventHandlers) {
				for (Map.Entry<EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>, Set<MessengerEvent>> eventEntry : this.eventHandlers.entrySet()) {
					for (MessengerEvent event : eventEntry.getValue()) {
						this.messenger.addEventHandler(eventEntry.getKey(), event);
					}
				}
			}

		} catch (ConfigurationException exception) {
			throw new IOException(exception);
		}

	}

	@Override
	public void disconnect() throws IOException {
		if ((this.messenger == null) || !this.messenger.isConnected()) {
			return;
		}
		this.messenger.disconnect();
		this.messenger = null;
		for (StreamControllerConnection receptionConnection : this.receptionConnectionList) {
			receptionConnection.disconnect();
		}
		this.emissionConnection.disconnect();
	}

	@Override
	public boolean isConnected() {
		return (this.messenger != null) && this.messenger.isConnected();
	}

}