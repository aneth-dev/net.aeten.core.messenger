package org.pititom.core.messenger.extension;

import org.pititom.core.Connection;
import org.pititom.core.event.EventHandlerRegister;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;

/**
 * 
 * @author Thomas Pérennou
 */
public interface Messenger<Message, Acknowledge extends Enum<?>> extends
		EventHandlerRegister<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>,
		Connection {
	
	public void transmit(Message message);

	public String getName();
}
