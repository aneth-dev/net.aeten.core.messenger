import java.util.logging.Level;
import java.util.logging.Logger;

import org.pititom.core.messenger.AbstractMessenger;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.MessengerEventHandler;
import org.pititom.core.messenger.extension.Messenger;
import org.pititom.core.messenger.stream.StreamMessenger;

/**
 * 
 * @author Thomas Pérennou
 */
public class MessengerTest {

	public static void main(String[] arguments) throws Exception {
		final String messageTable = "1=Message\n2=AcknowledgeMessage";
		final String emission = "-n emission -c -os org.pititom.core.stream.UdpIpOutputStream -osc \"-d 230.2.15.2:5200 -p 64 -r\" -se MessengerEncoder -sec \"" + messageTable + "\"";
		final String reception = "-n reception -c -is org.pititom.core.stream.UdpIpInputStream -isc \"-d 230.2.15.2:5200 -p 64 -r\" -se MessengerDecoder -sec \"" + messageTable + "\"";

		Messenger<AbstractMessage, Acknowledge> server = new StreamMessenger<AbstractMessage, Acknowledge>("server", emission, reception);
		server.addEventHandler(new MessengerEventHandler<AbstractMessage, Acknowledge>() {

			@Override
			public void handleEvent(Messenger<AbstractMessage, Acknowledge> messenger, MessengerEvent event, MessengerEventData<AbstractMessage, Acknowledge> eventData) {
				if (eventData.getRecievedMessage() instanceof AcknowledgeMessage) {
					return;
				}
				if (eventData.getRecievedMessage().getAcknowledge() == null) {
					messenger.emit(new AcknowledgeMessage(Acknowledge.INVALID_MESSAGE));
					return;
				}
				switch (eventData.getRecievedMessage().getAcknowledge()) {
					case SOLLICITED_NEED_ACKNOWLEDGE:
					case UNSOLLICITED_NEED_ACKNOWLEDGE:
						if (eventData.getRecievedMessage() instanceof Message) {
							Message recievedMessage = (Message) eventData.getRecievedMessage();
							if ((recievedMessage.getValue() < Message.MIN_VALUE) || (recievedMessage.getValue() > Message.MAX_VALUE)) {
								messenger.emit(new AcknowledgeMessage(Acknowledge.INVALID_DATA));
							} else {
								messenger.emit(new AcknowledgeMessage(Acknowledge.OK));
							}
						}
						break;
				}

			}

		}, MessengerEvent.RECIEVED);

		Messenger<AbstractMessage, Acknowledge> client = new StreamMessenger<AbstractMessage, Acknowledge>("client", AcknowledgeProtocol.getInstance(), emission, reception);
		client.addEventHandler(new MessengerEventHandler<AbstractMessage, Acknowledge>() {

			public void handleEvent(Messenger<AbstractMessage, Acknowledge> messenger, MessengerEvent event, MessengerEventData<AbstractMessage, Acknowledge> eventData) {
				if ((event == MessengerEvent.RECIEVED) && (!(eventData.getRecievedMessage() instanceof AcknowledgeMessage))) {
					return;
				}
				Logger.getLogger(AbstractMessenger.class.getName()).log(Level.INFO, "messenger={" + messenger + "}; event={" + event + "}; eventData={" + eventData + "}");
			}
		}, MessengerEvent.SENT, MessengerEvent.RECIEVED, MessengerEvent.ACKNOWLEDGED, MessengerEvent.UNACKNOWLEDGED);

		server.connect();
		client.connect();

		Message valid = new Message();
		valid.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		valid.setValue(4);
		client.emit(valid);

		Message invalidData = new Message();
		invalidData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		invalidData.setValue(10);
		client.emit(invalidData);

		Message invalidDataMessage = new Message();
		client.emit(invalidDataMessage);
	}
}
