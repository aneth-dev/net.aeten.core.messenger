/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pititom.core.messenger;

import org.pititom.core.extersion.EventHandler;

/**
 *
 * @author Thomas Pérennou
 */
public interface MessengerEventHandler<Message, Acknowledge extends Enum<?>>
		extends EventHandler<Messenger<Message, Acknowledge>, MessengerEvent, MessengerEventData<Message, Acknowledge>> {
}
