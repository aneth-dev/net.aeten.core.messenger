package org.pititom.core.test.messenger;

import java.util.Calendar;
import java.util.Date;

import org.pititom.core.Service;
import org.pititom.core.event.Handler;
import org.pititom.core.event.HookEvent;
import org.pititom.core.event.HookEvent.Hook;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingTransmitter;
import org.pititom.core.messenger.MessengerAcknowledgeEvent;
import org.pititom.core.messenger.MessengerAcknowledgeEventData;
import org.pititom.core.messenger.MessengerAcknowledgeHook;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.service.Messenger;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerTest {

	public static void main(String[] arguments) throws Exception {
		LoggingTransmitter.getInstance().addEventHandler(new Handler<LoggingData>() {
			@Override
			public void handleEvent(LoggingData data) {
				/*
				 * Basic logging. Can be plugged to anyone logging tool.
				 * org.pititom.core.messenger.AbstractMessenger exceptions can
				 * be caught by this way
				 */
				Date date = Calendar.getInstance().getTime();
				System.out.println(date + " " + (date.getTime() % 1000) + "ms " + data.getEvent() + " source={" + data.getSource() + "} " + data.getMessage() + ((data.getException() == null) ? "" : " : "));
				if (data.getException() != null) {
					data.getException().printStackTrace(System.out);
				}
			}
		}, LoggingEvent.values());

		// Client & server are both loaded from configuration files located in
		// META-INF/provider/org.pititom.core.messenger.stream.provider.StreamMessenger/
		Messenger<AbstractMessage> client = Service.getProvider(Messenger.class, "org.pititom.core.test.messenger.client");
		Messenger<AbstractMessage> server = Service.getProvider(Messenger.class, "org.pititom.core.test.messenger.server");

		AcknowledgeProtocol protocol = new AcknowledgeProtocol();
		MessengerAcknowledgeHook<AbstractMessage, Acknowledge> clientAcknowledgeHook = new MessengerAcknowledgeHook<AbstractMessage, Acknowledge>("Client acknowledge hook test", null, protocol);
		MessengerAcknowledgeHook<AbstractMessage, Acknowledge> serverAcknowledgeHook = new MessengerAcknowledgeHook<AbstractMessage, Acknowledge>("Server acknowledge hook test", null, protocol);
		server.addEventHandler(serverAcknowledgeHook, HookEvent.get(MessengerEvent.SEND, Hook.POST), HookEvent.get(MessengerEvent.RECIEVE, Hook.POST));
		client.addEventHandler(clientAcknowledgeHook, HookEvent.get(MessengerEvent.SEND, Hook.POST), HookEvent.get(MessengerEvent.RECIEVE, Hook.POST));

		Handler<MessengerAcknowledgeEventData<AbstractMessage, Acknowledge>> acknowledgeEventHandler = new Handler<MessengerAcknowledgeEventData<AbstractMessage, Acknowledge>>() {
			@Override
			public void handleEvent(MessengerAcknowledgeEventData<AbstractMessage, Acknowledge> data) {
				LoggingTransmitter.getInstance().transmit(new LoggingData(data.getSource(), data.getEvent().equals(MessengerAcknowledgeEvent.ACKNOWLEDGED) ? LoggingEvent.INFO : LoggingEvent.ERROR, "event={" + data.getEvent() + "}; eventData={" + data + "}"));
			}
		};
		clientAcknowledgeHook.addEventHandler(acknowledgeEventHandler, MessengerAcknowledgeEvent.values());
		serverAcknowledgeHook.addEventHandler(acknowledgeEventHandler, MessengerAcknowledgeEvent.values());

		server.addEventHandler(new Handler<MessengerEventData<AbstractMessage>>() {

			@Override
			public void handleEvent(MessengerEventData<AbstractMessage> data) {
				if (data.getMessage() instanceof AcknowledgeMessage) {
					return;
				}
				if (data.getMessage().getAcknowledge() == null) {
					data.getSource().transmit(new AcknowledgeMessage(Acknowledge.INVALID_MESSAGE), "org.pititom.core.test.messenger.server.sender");
					return;
				}
				switch (data.getMessage().getAcknowledge()) {
					case SOLLICITED_NEED_ACKNOWLEDGE:
					case UNSOLLICITED_NEED_ACKNOWLEDGE:
						if (data.getMessage() instanceof Message) {
							Message recievedMessage = (Message) data.getMessage();
							if ((recievedMessage.getValue() < Message.MIN_VALUE) || (recievedMessage.getValue() > Message.MAX_VALUE)) {
								data.getSource().transmit(new AcknowledgeMessage(Acknowledge.INVALID_DATA), "org.pititom.core.test.messenger.server.sender");
							} else {
								data.getSource().transmit(new AcknowledgeMessage(Acknowledge.OK), "org.pititom.core.test.messenger.server.sender");
							}
						}
						break;
					default:
						break;
				}
			}
		}, HookEvent.get(MessengerEvent.RECIEVE));

		client.addEventHandler(new Handler<MessengerEventData<AbstractMessage>>() {

			@Override
			public void handleEvent(MessengerEventData<AbstractMessage> data) {
				if ((data.getEvent().getSourceEvent() == MessengerEvent.RECIEVE) && (!(data.getMessage() instanceof AcknowledgeMessage))) {
					return;
				}
				LoggingEvent level = (data.getMessage().getAcknowledge() == Acknowledge.INVALID_MESSAGE) ? LoggingEvent.ERROR : LoggingEvent.INFO;
				LoggingTransmitter.getInstance().transmit(data.getSource(), level, "event={" + data.getEvent() + "}; eventData={" + data.getMessage() + "}");
			}
		}, HookEvent.get(MessengerEvent.SEND), HookEvent.get(MessengerEvent.RECIEVE));

		server.connect();
		client.connect();
		
		Message valid = new Message();
		valid.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		valid.setValue(4);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender");

		Message invalidData = new Message();
		invalidData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		invalidData.setValue(10);
		client.transmit(invalidData, "org.pititom.core.test.messenger.client.sender");

		Message invalidMessage = new Message();
		client.transmit(invalidMessage, "org.pititom.core.test.messenger.client.sender");
	}
}
