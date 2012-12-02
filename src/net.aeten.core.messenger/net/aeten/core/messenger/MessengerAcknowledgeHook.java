package net.aeten.core.messenger;

import java.util.HashMap;
import java.util.Map;

import net.aeten.core.Identifiable;
import net.aeten.core.event.Handler;
import net.aeten.core.event.HandlerRegister;
import net.aeten.core.event.TransmitterFactory;
import net.aeten.core.event.TransmitterService;
import net.aeten.core.spi.FieldInit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerAcknowledgeHook<Message, Acknowledge extends Enum <?>> implements
		Handler <MessengerEventData <Message>>,
		HandlerRegister <MessengerAcknowledgeEvent, MessengerAcknowledgeEventData <Message, Acknowledge>>,
		Identifiable {

	private static final Logger LOGGER = LoggerFactory.getLogger (MessengerAcknowledgeHook.class);
	private static final Map <String, Object> MUTEX_MAP = new HashMap <> (1);

	@FieldInit
	private String identifier;

	@FieldInit (alias = "acknowledge protocol",
					required = false)
	private Class <? extends MessengerAcknowledgeProtocol <Message, Acknowledge>> acknowledgeProtocolClass;

	@FieldInit (alias = "acknowledge protocol configuration",
					required = false)
	private String acknowledgeProtocolConfiguration;

	private MessengerAcknowledgeProtocol <Message, Acknowledge> acknowledgeProtocol = null;
	private Object acknowledgeMutex = new Object ();

	private volatile MessengerAcknowledgeEventData <Message, Acknowledge> currentEventData = null;

	// private RegisterableTransmitter<MessengerAcknowledgeEvent,
	// MessengerAcknowledgeEventData<Message, Acknowledge>> eventTransmitter =
	// null;
	private TransmitterService <MessengerAcknowledgeEvent, MessengerAcknowledgeEventData <Message, Acknowledge>> eventTransmitter = null;

	public MessengerAcknowledgeHook (String identifier) {
		this (identifier, null, null);
	}

//	public MessengerAcknowledgeHook(@SpiInitializer MessengerAcknowledgeHookInit init) {
//		this(init.getId(), init.hasAcknowledgeProtocolClass() ? init.getAcknowledgeProtocolClass(): null, init.hasAcknowledgeProtocolConfiguration() ? init.getAcknowledgeProtocolConfiguration(): null);
//	}

	public MessengerAcknowledgeHook (String identifier,
												String description,
												MessengerAcknowledgeProtocol <Message, Acknowledge> acknowledgeProtocol) {
		this.identifier = (identifier == null)? this.getClass ().getName (): identifier;
		this.acknowledgeProtocol = acknowledgeProtocol;
		this.eventTransmitter = TransmitterFactory.asynchronous ("Messenger acknowledge hook \"" + this + "\" event transmitter", MessengerAcknowledgeEvent.values ());
		// this.eventTransmitter =
		// TransmitterFactory.synchronous(MessengerAcknowledgeEvent.values());

		try {
			this.acknowledgeMutex = MUTEX_MAP.get (this.identifier);
			if (this.acknowledgeMutex == null) {
				this.acknowledgeMutex = new Object ();
				MUTEX_MAP.put (this.identifier, this.acknowledgeMutex);
			}

//			this.acknowledgeProtocol = acknowledgeProtocolClass.newInstance();
//			if ((this.acknowledgeProtocolConfiguration != null) && (this.acknowledgeProtocol instanceof Configurable)) {
//				((Configurable<String>) this.acknowledgeProtocol).configure(this.acknowledgeProtocolConfiguration);
//			}

			this.eventTransmitter = TransmitterFactory.asynchronous ("Messenger acknowledge hook \"" + this.getIdentifier () + "\" event transmitter", MessengerAcknowledgeEvent.values ());
			// this.eventTransmitter =
			// TransmitterFactory.synchronous(MessengerAcknowledgeEvent.values());
		} catch (Exception exception) {
			throw new IllegalArgumentException (exception);
		}

	}

	@Override
	public void handleEvent (MessengerEventData <Message> data) {
		switch (data.getEvent ().getSourceEvent ()) {
		case SEND:
			this.sendHook (data);
			break;
		case RECEIVE:
			this.startReception (data);
			break;
		default:
			break;
		}
	}

	private void sendHook (MessengerEventData <Message> eventData) {
		currentEventData = new MessengerAcknowledgeEventData <Message, Acknowledge> (eventData.getSource (), null, eventData.getMessage (), null, null);
		long timeOut = acknowledgeProtocol.getAcknowledgedTimeout (eventData.getMessage ());
		if (timeOut == 0) {
			return;
		}
		try {

			synchronized (acknowledgeMutex) {
				acknowledgeMutex.wait (timeOut);
			}

			MessengerAcknowledgeEvent notificationEvent;
			if (currentEventData.getRecievedMessage () == null) {
				notificationEvent = MessengerAcknowledgeEvent.UNACKNOWLEDGED;
			} else {
				final boolean success = (currentEventData.getAcknowledge () == null)? false: acknowledgeProtocol.isSuccess (this.currentEventData.getAcknowledge ());
				notificationEvent = success? MessengerAcknowledgeEvent.ACKNOWLEDGED: MessengerAcknowledgeEvent.UNACKNOWLEDGED;
			}

			eventTransmitter.transmit (new MessengerAcknowledgeEventData <Message, Acknowledge> (currentEventData.getSource (), notificationEvent, this.currentEventData.getSentMessage (), this.currentEventData.getRecievedMessage (), this.currentEventData.getAcknowledge ()));

		} catch (Exception exception) {
			LOGGER.error (currentEventData.getSource ().toString (), exception);
		}
	}

	private void startReception (MessengerEventData <Message> eventData) {
		if (this.currentEventData != null) {
			try {
				final Acknowledge acknowledge = this.acknowledgeProtocol.getAcknowledge (this.currentEventData.getSentMessage (), eventData.getMessage ());

				if (acknowledge != null) {
					this.currentEventData.setRecievedMessage (eventData.getMessage ());
					this.currentEventData.setAcknowledge (acknowledge);
					synchronized (this.acknowledgeMutex) {
						this.acknowledgeMutex.notifyAll ();
					}
				}

			} catch (Exception exception) {
				LOGGER.error (currentEventData.getSource ().toString (), exception);
			}
		}
	}

	@Override
	public final String getIdentifier () {
		return this.identifier;
	}

	@Override
	public String toString () {
		return this.identifier;
	}

	@Override
	public void addEventHandler (	Handler <MessengerAcknowledgeEventData <Message, Acknowledge>> eventHandler,
											MessengerAcknowledgeEvent... eventList) {
		this.eventTransmitter.addEventHandler (eventHandler, eventList);

	}

	@Override
	public void removeEventHandler (	Handler <MessengerAcknowledgeEventData <Message, Acknowledge>> eventHandler,
												MessengerAcknowledgeEvent... eventList) {
		this.eventTransmitter.removeEventHandler (eventHandler, eventList);
	}

}
