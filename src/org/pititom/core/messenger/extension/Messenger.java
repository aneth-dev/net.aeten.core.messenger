package org.pititom.core.messenger.extension;

import org.pititom.core.event.EventPerformer;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.stream.controller.Connection;

/**
*
* @author Thomas Pérennou
*/
public interface Messenger<Message, Acknowledge extends Enum<?>> extends EventPerformer<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>, Connection {
	public void emit(Message message);
	public String getName();
}
