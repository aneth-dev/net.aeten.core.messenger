package org.pititom.core.messenger.service;

import org.pititom.core.Connection;
import org.pititom.core.event.HandlerRegister;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;

/**
 * 
 * @author Thomas Pérennou
 */
public interface Messenger<Message, Acknowledge extends Enum<?>> extends
		HandlerRegister<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>,
		Connection {
	
	public void transmit(Message message);

	public String getName();
}
