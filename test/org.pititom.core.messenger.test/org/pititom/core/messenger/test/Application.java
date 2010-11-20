package org.pititom.core.messenger.test;

import java.util.Calendar;
import java.util.Date;

import org.pititom.core.event.Handler;
import org.pititom.core.event.Hook;
import org.pititom.core.event.Priority;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.messenger.Messenger;
import org.pititom.core.messenger.MessengerAcknowledgeEvent;
import org.pititom.core.messenger.MessengerAcknowledgeEventData;
import org.pititom.core.messenger.MessengerAcknowledgeHook;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.MessengerProvider;
import org.pititom.core.messenger.net.UdpIpReceiver;
import org.pititom.core.service.Configuration;
import org.pititom.core.service.Configurations;
import org.pititom.core.service.Service;

@Configurations({
	@Configuration(name = "server.pml", provider = MessengerProvider.class),
	@Configuration(name = "client.toto", provider = MessengerProvider.class, parser = "org.pititom.core.parsing.PmlParser", converter = "org.pititom.core.args4j.Markup2Args4j"),
	@Configuration(name = "client.receiver.pml", provider = UdpIpReceiver.class)})
public class Application {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		   Logger.addEventHandler(new Handler<LoggingData>() {
			@Override
			public void handleEvent(LoggingData data) {
				/*
				 * Basic logging. Can be plugged to anyone logging tool.
				 * org.pititom.core.messenger.Messenger exceptions can be caught
				 * by this way
				 */
				Date date = Calendar.getInstance().getTime();

				System.out.println(date + String.format(" %3dms %5s  source={%s} %s%s", date.getTime() % 1000, data.getEvent(),data.getSource(), data.getMessage(), (data.getThrowable() == null) ? "" : " : "));
				if (data.getThrowable() != null) {
					data.getThrowable().printStackTrace(System.out);
				}
			}
		}, LogLevel.values());

		Logger.log(Application.class, LogLevel.INFO, "Start");

		// Client & server are both loaded from configuration files located in
		// META-INF/provider/org.pititom.core.messenger.provider.
		// MessengerProvider/
		Messenger<AbstractMessage> client = Service.getProvider(Messenger.class, "org.pititom.core.test.messenger.client");
		Messenger<AbstractMessage> server = Service.getProvider(Messenger.class, "org.pititom.core.test.messenger.server");

		AcknowledgeProtocol protocol = new AcknowledgeProtocol();
		MessengerAcknowledgeHook<AbstractMessage, Acknowledge> clientAcknowledgeHook = new MessengerAcknowledgeHook<AbstractMessage, Acknowledge>("Client acknowledge hook test", null, protocol);
		MessengerAcknowledgeHook<AbstractMessage, Acknowledge> serverAcknowledgeHook = new MessengerAcknowledgeHook<AbstractMessage, Acknowledge>("Server acknowledge hook test", null, protocol);
		server.addEventHandler(serverAcknowledgeHook, Messenger.EVENTS.get(MessengerEvent.SEND, Hook.POST), Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.POST));
		client.addEventHandler(clientAcknowledgeHook, Messenger.EVENTS.get(MessengerEvent.SEND, Hook.POST), Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.POST));

		Handler<MessengerAcknowledgeEventData<AbstractMessage, Acknowledge>> acknowledgeEventHandler = new Handler<MessengerAcknowledgeEventData<AbstractMessage, Acknowledge>>() {
			@Override
			public void handleEvent(MessengerAcknowledgeEventData<AbstractMessage, Acknowledge> data) {
				LogLevel level = (data.getEvent() == MessengerAcknowledgeEvent.ACKNOWLEDGED) ? LogLevel.INFO : LogLevel.ERROR;
				Logger.log(data.getSource(), level, "event={" + data.getEvent() + "}; eventData={" + data + "}");
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
		}, Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.END));

		client.addEventHandler(new Handler<MessengerEventData<AbstractMessage>>() {

			@Override
			public void handleEvent(MessengerEventData<AbstractMessage> data) {
				if ((data.getEvent().getSourceEvent() == MessengerEvent.RECEIVE) && (!(data.getMessage() instanceof AcknowledgeMessage))) {
					return;
				}
				Logger.log(data.getSource(), LogLevel.INFO, "event={" + data.getEvent() + "}; eventData={" + data.getMessage() + "}");
			}
		}, Messenger.EVENTS.get(MessengerEvent.SEND, Hook.END), Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.END));

		server.connect();
		client.connect();
		
		Message valid = new Message();
		valid.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		valid.setValue(4);

		Message invalidData = new Message();
		invalidData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		invalidData.setValue(10);
		
		Message highData = new Message();
		highData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		highData.setValue(3);

		Message invalidMessage = new Message();

//		System.out.println(client.transmit(valid, "org.pititom.core.test.messenger.client.sender").get());
//		System.out.println(client.transmit(invalidData, "org.pititom.core.test.messenger.client.sender").get());
//		System.out.println(client.transmit(invalidMessage, "org.pititom.core.test.messenger.client.sender").get());
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender");
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(highData, "org.pititom.core.test.messenger.client.sender", Priority.HIGH);
		client.transmit(invalidData, "org.pititom.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(invalidMessage, "org.pititom.core.test.messenger.client.sender", Priority.HIGH);

                Thread.currentThread().join();
	}

}
