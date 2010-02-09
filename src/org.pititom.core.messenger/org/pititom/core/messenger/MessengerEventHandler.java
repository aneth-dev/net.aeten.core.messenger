package org.pititom.core.messenger;

import org.pititom.core.event.Handler;
import org.pititom.core.messenger.service.Messenger;

/**
 *
 * @author Thomas Pérennou
 */
public interface MessengerEventHandler<Message, Acknowledge extends Enum<?>>
		extends Handler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> {
}
