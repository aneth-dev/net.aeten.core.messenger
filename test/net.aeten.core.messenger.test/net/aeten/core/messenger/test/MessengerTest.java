package net.aeten.core.messenger.test;

import java.util.Calendar;
import java.util.Date;
import net.aeten.core.event.Handler;
import net.aeten.core.event.Hook;
import net.aeten.core.event.Priority;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.logging.LoggingData;
import net.aeten.core.messenger.Messenger;
import net.aeten.core.messenger.MessengerAcknowledgeEvent;
import net.aeten.core.messenger.MessengerAcknowledgeEventData;
import net.aeten.core.messenger.MessengerAcknowledgeHook;
import net.aeten.core.messenger.MessengerEvent;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.MessengerProvider;
import net.aeten.core.messenger.net.UdpIpReceiver;
import net.aeten.core.spi.Configuration;
import net.aeten.core.spi.Configurations;
import net.aeten.core.spi.Service;

@Configurations({
	@Configuration(name = "server.yaml",
						provider = MessengerProvider.class),
	@Configuration(name = "client.yaml",
						provider = MessengerProvider.class,
						parser = "net.aeten.core.parsing.yaml.YamlParser"),
	@Configuration(name = "client.receiver.yaml",
						provider = UdpIpReceiver.class)})
public class MessengerTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Logger.addEventHandler(new Handler<LoggingData>() {

			@Override
			public void handleEvent(LoggingData data) {
				/*
				 * Basic logging. Can be plugged to anyone logging tool.
				 * net.aeten.core.messenger.Messenger exceptions can be caught by
				 * this way
				 */
				synchronized (this) {
					Date date = Calendar.getInstance().getTime();

					System.out.println(date + String.format(" %3dms %5s  source={%s} %s%s", date.getTime() % 1000, data.getEvent(), data.getSource(), data.getMessage(), (data.getThrowable() == null) ? "" : ": "));
					if (data.getThrowable() != null) {
						data.getThrowable().printStackTrace(System.out);
					}
				}
			}
		}, LogLevel.values());

		Logger.log(MessengerTest.class, LogLevel.INFO, "Start");

		Messenger<AbstractMessage> client = Service.getProvider(Messenger.class, "net.aeten.core.test.messenger.client");
		Messenger<AbstractMessage> server = Service.getProvider(Messenger.class, "net.aeten.core.test.messenger.server");

		AcknowledgeProtocol protocol = new AcknowledgeProtocol();
		MessengerAcknowledgeHook<AbstractMessage, Acknowledge> clientAcknowledgeHook = new MessengerAcknowledgeHook<>("Client acknowledge hook test", null, protocol);
		MessengerAcknowledgeHook<AbstractMessage, Acknowledge> serverAcknowledgeHook = new MessengerAcknowledgeHook<>("Server acknowledge hook test", null, protocol);
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
					data.getSource().transmit(new AcknowledgeMessage(Acknowledge.INVALID_MESSAGE), "net.aeten.core.test.messenger.server.sender");
					return;
				}
				switch (data.getMessage().getAcknowledge()) {
				case SOLLICITED_NEED_ACKNOWLEDGE:
				case UNSOLLICITED_NEED_ACKNOWLEDGE:
					if (data.getMessage() instanceof Message) {
						Message recievedMessage = (Message) data.getMessage();
						if ((recievedMessage.getValue() < Message.MIN_VALUE) || (recievedMessage.getValue() > Message.MAX_VALUE)) {
							data.getSource().transmit(new AcknowledgeMessage(Acknowledge.INVALID_DATA), "net.aeten.core.test.messenger.server.sender");
						} else {
							data.getSource().transmit(new AcknowledgeMessage(Acknowledge.OK), "net.aeten.core.test.messenger.server.sender");
						}
					}
					break;
				default:
					break;
				}
			}
		}, Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.END));
		server.addEventHandler(new Handler<MessengerEventData<AbstractMessage>>() {

			@Override
			public void handleEvent(MessengerEventData<AbstractMessage> data) {
				Logger.log(data.getSource(), LogLevel.INFO, "event={" + data.getEvent() + "}; data={" + data.getMessage() + "}");
			}
		}, Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.END), Messenger.EVENTS.get(MessengerEvent.SEND, Hook.END));

		client.addEventHandler(new Handler<MessengerEventData<AbstractMessage>>() {

			@Override
			public void handleEvent(MessengerEventData<AbstractMessage> data) {
				if ((data.getEvent().getSourceEvent() == MessengerEvent.RECEIVE) && (!(data.getMessage() instanceof AcknowledgeMessage))) {
					return;
				}
				Logger.log(data.getSource(), LogLevel.INFO, "event={" + data.getEvent() + "}; data={" + data.getMessage() + "}");
			}
		}, Messenger.EVENTS.get(MessengerEvent.SEND, Hook.END), Messenger.EVENTS.get(MessengerEvent.RECEIVE, Hook.END));

		server.connect();
		client.connect();

		Message invalidData = new Message();
		invalidData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		invalidData.setValue(10);

		Message highData = new Message();
		highData.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		highData.setValue(3);

		Message invalidMessage = new Message();

		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender");
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(valid(), "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(highData, "net.aeten.core.test.messenger.client.sender", Priority.HIGH);
		client.transmit(invalidData, "net.aeten.core.test.messenger.client.sender", Priority.LOW);
		client.transmit(invalidMessage, "net.aeten.core.test.messenger.client.sender", Priority.HIGH);

		Thread.currentThread().join();
	}

	private static Message valid() {
		Message valid = new Message();
		valid.setAcknowledge(Acknowledge.UNSOLLICITED_NEED_ACKNOWLEDGE);
		valid.setValue(4);
		return valid;
	}
}
