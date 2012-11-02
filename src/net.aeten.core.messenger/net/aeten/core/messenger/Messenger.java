package net.aeten.core.messenger;

import java.io.IOException;
import java.util.concurrent.Future;

import net.aeten.core.Connection;
import net.aeten.core.Identifiable;
import net.aeten.core.event.HandlerRegister;
import net.aeten.core.event.Hook;
import net.aeten.core.event.HookEvent;
import net.aeten.core.event.HookEventGroup;
import net.aeten.core.event.Priority;

/**
 * 
 * @author Thomas Pérennou
 */
public interface Messenger<Message> extends HandlerRegister<HookEvent<MessengerEvent, Hook>, MessengerEventData<Message>>, Connection, Identifiable {

	public static final HookEventGroup<MessengerEvent, Hook> EVENTS = HookEventGroup.build(MessengerEvent.values());

	public Future<MessengerEventData<Message>> transmit(Message message);

	public Future<MessengerEventData<Message>> transmit(Message message, String sender);

	public Future<MessengerEventData<Message>> transmit(Message message, String sender, Priority priority);

	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact);

	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact, String service);

	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact, Priority priority);

	public Future<MessengerEventData<Message>> transmit(Message message, String sender, String contact, String service, Priority priority);

	public void addReceiver(Receiver<Message> reciever) throws IOException;

	public void addSender(Sender<Message> sender) throws IOException;

	public void removeReceiver(final Receiver<Message> receiver) throws IOException;

	public void removeSender(final Sender<Message> sender) throws IOException;

	public String[] getReceivers();

	public String[] getSenders();

}
