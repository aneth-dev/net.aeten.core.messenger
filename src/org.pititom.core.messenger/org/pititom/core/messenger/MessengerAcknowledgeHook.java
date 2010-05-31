package org.pititom.core.messenger;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Identifiable;
import org.pititom.core.event.Handler;
import org.pititom.core.event.HandlerRegister;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.messenger.service.Messenger;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerAcknowledgeHook<Message, Acknowledge extends Enum<?>>
		implements
		Handler<MessengerEventData<Message>>,
		HandlerRegister<Messenger<Message>, MessengerAcknowledgeEvent, MessengerAcknowledgeEventData<Message, Acknowledge>>,
		Configurable, Identifiable {

	private static final Map<String, Object> MUTEX_MAP = new HashMap<String, Object>(
			1);

	@Option(name = "-id", aliases = "--identifier", required = true)
	private String identifier;

	@Option(name = "-ap", aliases = "--acknowledge-protocol", required = false)
	private Class<? extends MessengerAcknowledgeProtocol<Message, Acknowledge>> acknowledgeProtocolClass;

	@Option(name = "-apc", aliases = "--acknowledge-protocol-configuration", required = false)
	private String acknowledgeProtocolConfiguration;

	private MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol = null;
	private Object acknowledgeMutex = new Object();

	MessengerAcknowledgeEventData<Message, Acknowledge> currentEventData = null;

	private RegisterableTransmitter<Messenger<Message>, MessengerAcknowledgeEvent, MessengerAcknowledgeEventData<Message, Acknowledge>> eventTransmitter = null;

	public MessengerAcknowledgeHook(String identifier) {
		this(identifier, null, null);
	}

	public MessengerAcknowledgeHook() {
		this(null, null, null);
	}

	public MessengerAcknowledgeHook(
			String identifier,
			String description,
			MessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol) {
		this.identifier = (identifier == null) ? this.getClass().getName()
				: identifier;
		this.acknowledgeProtocol = acknowledgeProtocol;
		this.eventTransmitter = TransmitterFactory
				.asynchronous("Messenger acknowledge hook \"" + this
						+ "\" event transmitter");
	}

	@Override
	public void handleEvent(MessengerEventData<Message> data) {
		switch (data.getEvent().getSourceEvent()) {
		case SEND:
			this.sendHook(data);
			break;
		case RECEIVE:
			this.startReception(data);
			break;
		default:
			break;
		}
	}

	private void sendHook(MessengerEventData<Message> eventData) {
		this.currentEventData = new MessengerAcknowledgeEventData<Message, Acknowledge>(
				eventData.getSource(), null, eventData.getMessage(), null, null);
		long timeOut = this.acknowledgeProtocol
				.getAcknowledgedTimeout(eventData.getMessage());
		if (timeOut == 0) {
			return;
		}
		try {

			synchronized (this.acknowledgeMutex) {
				this.acknowledgeMutex.wait(timeOut);
			}

			MessengerAcknowledgeEvent notificationEvent;
			if (this.currentEventData.getRecievedMessage() == null) {
				notificationEvent = MessengerAcknowledgeEvent.UNACKNOWLEDGED;
			} else {
				final boolean success = (this.currentEventData.getAcknowledge() == null) ? false
						: this.acknowledgeProtocol
								.isSuccess(this.currentEventData
										.getAcknowledge());
				notificationEvent = success ? MessengerAcknowledgeEvent.ACKNOWLEDGED
						: MessengerAcknowledgeEvent.UNACKNOWLEDGED;
			}

			this.eventTransmitter
					.transmit(new MessengerAcknowledgeEventData<Message, Acknowledge>(
							this.currentEventData.getSource(),
							notificationEvent, this.currentEventData
									.getSentMessage(), this.currentEventData
									.getRecievedMessage(),
							this.currentEventData.getAcknowledge()));

		} catch (Exception exception) {
			Logger.log(this.currentEventData.getSource(),
							LogLevel.ERROR, exception);
		}
	}

	private void startReception(MessengerEventData<Message> eventData) {
		if (this.currentEventData != null) {
			try {
				final Acknowledge acknowledge = this.acknowledgeProtocol
						.getAcknowledge(this.currentEventData.getSentMessage(),
								eventData.getMessage());

				if (acknowledge != null) {
					this.currentEventData.setRecievedMessage(eventData
							.getMessage());
					this.currentEventData.setAcknowledge(acknowledge);
					synchronized (this.acknowledgeMutex) {
						this.acknowledgeMutex.notifyAll();
					}
				}

			} catch (Exception exception) {
				Logger.log(this.currentEventData.getSource(),
								LogLevel.ERROR, exception);
			}
		}
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		try {
			this.acknowledgeMutex = MUTEX_MAP.get(this.getIdentifier());
			if (this.acknowledgeMutex == null) {
				this.acknowledgeMutex = new Object();
				MUTEX_MAP.put(this.getIdentifier(), this.acknowledgeMutex);
			}

			this.acknowledgeProtocol = acknowledgeProtocolClass.newInstance();
			if ((this.acknowledgeProtocolConfiguration != null)
					&& this.acknowledgeProtocol instanceof Configurable) {
				((Configurable) this.acknowledgeProtocol)
						.configure(this.acknowledgeProtocolConfiguration);
			}

			this.eventTransmitter = TransmitterFactory
					.asynchronous("Messenger acknowledge hook \""
							+ this.getIdentifier() + "\" event transmitter");
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public String toString() {
		return this.identifier;
	}

	@Override
	public void addEventHandler(
			Handler<MessengerAcknowledgeEventData<Message, Acknowledge>> eventHandler,
			MessengerAcknowledgeEvent... eventList) {
		this.eventTransmitter.addEventHandler(eventHandler, eventList);

	}

	@Override
	public void removeEventHandler(
			Handler<MessengerAcknowledgeEventData<Message, Acknowledge>> eventHandler,
			MessengerAcknowledgeEvent... eventList) {
		this.eventTransmitter.removeEventHandler(eventHandler, eventList);
	}

}
