package org.pititom.core.messenger;

import org.pititom.core.extersion.EventHandler;
import org.pititom.core.messenger.extension.Messenger;

/**
 *
 * @author Thomas Pérennou
 */
public interface MessengerEventHandler<Message, Acknowledge extends Enum<?>>
		extends EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> {
}
