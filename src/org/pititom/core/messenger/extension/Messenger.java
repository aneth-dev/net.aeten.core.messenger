package org.pititom.core.messenger.extension;

import org.pititom.core.extersion.EventPerformer;
import org.pititom.core.messenger.MessengerEvent;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.stream.extension.Connection;

public interface Messenger<Message, Acknowledge extends Enum<?>> extends EventPerformer<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>>, Connection {
	public void emit(Message message);
	public String getName();
}
