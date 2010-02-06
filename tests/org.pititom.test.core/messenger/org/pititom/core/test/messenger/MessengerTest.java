package org.pititom.core.test.messenger;

import java.util.Calendar;
import java.util.Date;

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
		final String messageTable = "1: Message\n	2: AcknowledgeMessage";
		final String hook = "--hook org.pititom.core.messenger.DefaultMessengerHooks --configuration \"--name test --acknowledge-protocol AcknowledgeProtocol\"";
		final String stream = "--destination-inet-socket-adress 230.2.15.2:5200 --max-packet-size 64 --reuse";
		final String editor = "--configuration \"" + messageTable + "\"";
		final String emissionOutput = "--output-stream org.pititom.core.stream.UdpIpOutputStream --configuration \"" + stream;
		final String emission = "--name emission --auto-connect "+ emissionOutput + "\" --stream-editor MessengerEncoder " + editor;
		final String receptionInput = "--input-stream org.pititom.core.stream.UdpIpInputStream --configuration \"" + stream;
		final String reception = "--name reception --auto-connect "+ receptionInput + "\" --stream-editor MessengerDecoder " + editor;
		final boolean autoConnect = true;

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
				Date date = Calendar.getInstance().getTime();
				System.out.println(date + " " + (date.getTime() % 1000) + " messenger={" + messenger + "}; event={" + event + "}; eventData={" + eventData + "}");
			}
		}, MessengerEvent.SENT, MessengerEvent.RECIEVED, MessengerEvent.ACKNOWLEDGED, MessengerEvent.UNACKNOWLEDGED, MessengerEvent.ERROR);

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
