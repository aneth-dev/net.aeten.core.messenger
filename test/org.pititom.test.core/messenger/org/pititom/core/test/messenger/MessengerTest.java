package org.pititom.core.test.messenger;

import java.util.Calendar;
import java.util.Date;
import org.pititom.core.event.Handler;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingForwarder;

import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.MessengerEventHandler;
import org.pititom.core.messenger.extension.Messenger;
import org.pititom.core.messenger.stream.StreamEditorMessenger;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerTest {

	public static void main(String[] arguments) throws Exception {
		final String messageTable = "1: org.pititom.core.test.messenger.Message\n	2: org.pititom.core.test.messenger.AcknowledgeMessage";
		final String hook = "--hook org.pititom.core.messenger.DefaultMessengerHooks --configuration \"--name test --acknowledge-protocol org.pititom.core.test.messenger.AcknowledgeProtocol\"";
		final String stream = "--destination-inet-socket-adress 230.2.15.2:5200 --max-packet-size 64 --reuse";
		final String editor = "--configuration \"" + messageTable + "\"";
		final String emissionOutput = "--output-stream org.pititom.core.stream.UdpIpOutputStream --configuration \"" + stream;
		final String emission = "--name emission --auto-connect "+ emissionOutput + "\" --stream-editor org.pititom.core.test.messenger.MessengerEncoder " + editor;
		final String receptionInput = "--input-stream org.pititom.core.stream.UdpIpInputStream --configuration \"" + stream;
		final String reception = "--name reception --auto-connect "+ receptionInput + "\" --stream-editor org.pititom.core.test.messenger.MessengerDecoder " + editor;
		final boolean autoConnect = true;

		LoggingForwarder.getInstance().addEventHandler(new Handler<Object, LoggingEvent, LoggingData>() {
			@Override
			public void handleEvent(Object source, LoggingEvent event, LoggingData data) {
				/* Basic logging. Can be plugged to anyone logging tool
				 * org.pititom.core.messenger.AbstractMessenger exceptions can be caught by this way
				 */
				Date date = Calendar.getInstance().getTime();
				System.out.println(date + " " + (date.getTime() % 1000) + "ms " + event + " source={" + source + "} " + data.getMessage() + ((data.getException() == null) ? "" : " : " + data.getException()));
			}
		}, LoggingEvent.values());

		Messenger<AbstractMessage, Acknowledge> server = new StreamEditorMessenger<AbstractMessage, Acknowledge>("server", autoConnect, hook, emission, reception);
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

		Messenger<AbstractMessage, Acknowledge> client = new StreamEditorMessenger<AbstractMessage, Acknowledge>("client", autoConnect, hook, emission, reception);
		client.addEventHandler(new MessengerEventHandler<AbstractMessage, Acknowledge>() {

			public void handleEvent(Messenger<AbstractMessage, Acknowledge> messenger, MessengerEvent event, MessengerEventData<AbstractMessage, Acknowledge> eventData) {
				if ((event == MessengerEvent.RECIEVED) && (!(eventData.getRecievedMessage() instanceof AcknowledgeMessage))) {
					return;
				}
				LoggingForwarder.getInstance().forward(messenger, LoggingEvent.INFO, new LoggingData("event={" + event + "}; eventData={" + eventData + "}"));
			}
		}, MessengerEvent.SENT, MessengerEvent.RECIEVED, MessengerEvent.ACKNOWLEDGED, MessengerEvent.UNACKNOWLEDGED);

		if (!autoConnect) {
			server.connect();
			client.connect();
		}

		Message valid = new Message();
		valid.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		valid.setValue(4);
		client.transmit(valid);

		Message invalidData = new Message();
		invalidData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		invalidData.setValue(10);
		client.transmit(invalidData);

		Message invalidDataMessage = new Message();
		client.transmit(invalidDataMessage);
	}
}
