package org.pititom.core.test.messenger;

import java.util.Calendar;
import java.util.Date;
import org.pititom.core.Service;

import org.pititom.core.event.Handler;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingTransmitter;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.MessengerEventHandler;
import org.pititom.core.messenger.service.Messenger;
import org.pititom.core.messenger.stream.provider.StreamMessenger;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerTest {

	public static void main(String[] arguments) throws Exception {
		StreamMessenger<AbstractMessage, Acknowledge> client = null, server = null;
		for (Messenger messenger : Service.getProviders(Messenger.class)) {
			if (messenger.getName().equals("client")) {
				client = (StreamMessenger<AbstractMessage, Acknowledge>) messenger;
			} else if (messenger.getName().equals("server")) {
				server = (StreamMessenger<AbstractMessage, Acknowledge>) messenger;
			}
		}
		
		LoggingTransmitter.getInstance().addEventHandler(new Handler<Object, LoggingEvent, LoggingData>() {
			@Override
			public void handleEvent(Object source, LoggingEvent event, LoggingData data) {
				/* Basic logging. Can be plugged to anyone logging tool.
				 * org.pititom.core.messenger.AbstractMessenger exceptions can be caught by this way
				 */
				Date date = Calendar.getInstance().getTime();
				System.out.println(date + " " + (date.getTime() % 1000) + "ms " + event + " source={" + source + "} " + data.getMessage() + ((data.getException() == null) ? "" : " : "));
				if (data.getException() != null) {
					data.getException().printStackTrace(System.out);
				}
			}
		}, LoggingEvent.values());

		server.addEventHandler(new MessengerEventHandler<AbstractMessage, Acknowledge>() {

			@Override
			public void handleEvent(Messenger<AbstractMessage, Acknowledge> messenger, MessengerEvent event, MessengerEventData<AbstractMessage, Acknowledge> eventData) {
				if (eventData.getRecievedMessage() instanceof AcknowledgeMessage) {
					return;
				}
				if (eventData.getRecievedMessage().getAcknowledge() == null) {
					messenger.transmit(new AcknowledgeMessage(Acknowledge.INVALID_MESSAGE));
					return;
				}
				switch (eventData.getRecievedMessage().getAcknowledge()) {
					case SOLLICITED_NEED_ACKNOWLEDGE:
					case UNSOLLICITED_NEED_ACKNOWLEDGE:
						if (eventData.getRecievedMessage() instanceof Message) {
							Message recievedMessage = (Message) eventData.getRecievedMessage();
							if ((recievedMessage.getValue() < Message.MIN_VALUE) || (recievedMessage.getValue() > Message.MAX_VALUE)) {
								messenger.transmit(new AcknowledgeMessage(Acknowledge.INVALID_DATA));
							} else {
								messenger.transmit(new AcknowledgeMessage(Acknowledge.OK));
							}
						}
						break;
				}
			}

		}, MessengerEvent.RECIEVED);

		client.addEventHandler(new MessengerEventHandler<AbstractMessage, Acknowledge>() {

			public void handleEvent(Messenger<AbstractMessage, Acknowledge> messenger, MessengerEvent event, MessengerEventData<AbstractMessage, Acknowledge> eventData) {
				if ((event == MessengerEvent.RECIEVED) && (!(eventData.getRecievedMessage() instanceof AcknowledgeMessage))) {
					return;
				}
				LoggingTransmitter.getInstance().transmit(messenger, LoggingEvent.INFO, new LoggingData("event={" + event + "}; eventData={" + eventData + "}"));
			}
		}, MessengerEvent.SENT, MessengerEvent.RECIEVED, MessengerEvent.ACKNOWLEDGED, MessengerEvent.UNACKNOWLEDGED);


		Message valid = new Message();
		valid.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		valid.setValue(4);
		client.transmit(valid);

		Message invalidData = new Message();
		invalidData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		invalidData.setValue(10);
		client.transmit(invalidData);

		Message invalidMessage = new Message();
		client.transmit(invalidMessage);
	}
}
