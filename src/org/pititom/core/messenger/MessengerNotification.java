package org.pititom.core.messenger;

import org.pititom.core.event.EventEntry;
import org.pititom.core.messenger.extension.Messenger;

/**
*
* @author Thomas Pérennou
*/
class MessengerNotification<Message, Acknowledge extends Enum<?>> extends EventEntry<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> {
	public MessengerNotification(Messenger<Message, Acknowledge> source, MessengerEvent event, MessengerEventData<Message, Acknowledge> data) {
		super(source, event, data);
	}
}
