package org.pititom.core.messenger;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.pititom.core.args4j.CommandLineParser;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.event.EventHandler;

/**
 *
 * @author Thomas Pérennou
 */
public class DefaultMessengerHooks<Message, Acknowledge extends Enum<?>>  implements
		EventHandler<AbstractMessenger<Message, Acknowledge>, MessengerHook, MessengerHookData<Message, Acknowledge>>, Configurable {

	private static final Map<String, Object> MUTEX_MAP = new HashMap<String, Object>(1);
	@Option(name = "-n", aliases = "--name", required = true)
	private String name;
	@Option(name = "-ap", aliases = "--acknowledge-protocol", required = false)
	private Class<? extends DefaultMessengerAcknowledgeProtocol<Message, Acknowledge>> acknowledgeProtocolClass;
	@Option(name = "-apc", aliases = "--acknowledge-protocol-configuration", required = false)
	
	private String acknowledgeProtocolConfiguration;
	private DefaultMessengerAcknowledgeProtocol<Message, Acknowledge> acknowledgeProtocol = null;
	private Object acknowledgeMutex = new Object();
	private boolean waitingForAcknowledgeBlocking = true;
	private long waitingForAcknowledgeDeadLine = 0L;

	public DefaultMessengerHooks() {
		return;
	}

	@Override
	public void handleEvent(AbstractMessenger<Message, Acknowledge> source, MessengerHook event, MessengerHookData<Message, Acknowledge> data) {
		switch (event) {
			case START_SEND:
				this.sendHook(source, data);
				break;
			case START_RECEPTION:
				this.startReception(source, data);
				break;
		}
	}

	private void sendHook(AbstractMessenger<Message, Acknowledge> source, MessengerHookData<Message, Acknowledge> data) {
		long now;
		try {
			now = System.currentTimeMillis();
			for (; now < this.waitingForAcknowledgeDeadLine; now = System.currentTimeMillis()) {
				if (this.waitingForAcknowledgeBlocking && (data.getAcknowledge() == null)) {
					synchronized (this.acknowledgeMutex) {
						this.acknowledgeMutex.wait(this.waitingForAcknowledgeDeadLine - now);
					}
				}
				now = System.currentTimeMillis();

				if (this.waitingForAcknowledgeDeadLine < now) {
					continue;
				}

				this.waitingForAcknowledgeDeadLine = 0;

				MessengerEvent notificationEvent;
				if (data.getCurrentEventData().getRecievedMessage() == null) {
					notificationEvent = MessengerEvent.UNACKNOWLEDGED;
				} else {
					final boolean success = (data.getCurrentEventData().getAcknowledge() == null) ? false : this.acknowledgeProtocol.isSuccess(data.getCurrentEventData().getAcknowledge());
					notificationEvent = success ? MessengerEvent.ACKNOWLEDGED : MessengerEvent.UNACKNOWLEDGED;
				}

				data.getEventTransmitter().transmit(notificationEvent, data.getCurrentEventData().clone());
			}

			data.getCurrentEventData().setSentMessage(data.getMessageToSend());
			if (this.acknowledgeProtocol == null) {
				this.waitingForAcknowledgeDeadLine = 0;
			} else {
				long timeout = this.acknowledgeProtocol.getAcknowledgedTimeout(data.getMessageToSend());
				this.waitingForAcknowledgeDeadLine = timeout > 0 ? now + timeout : 0;

				// TODO: Does not works yet
				// this.waitingForAcknowledgeBlocking =
				//this.acknowledgeProtocol.isBlocking(data.getCurrentEventData()
				// .getSentMessage());
			}

		} catch (Exception exception) {
			source.error(exception);
		}
	}

	private void startReception(AbstractMessenger<Message, Acknowledge> source, MessengerHookData<Message, Acknowledge> data) {
		try {

			if (this.waitingForAcknowledgeDeadLine < System.currentTimeMillis()) {
				return;
			}
			final Acknowledge acknowledge = this.acknowledgeProtocol.getAcknowledge(data.getCurrentEventData().getSentMessage(), data.getRecievedMessage());

			if (acknowledge != null) {
				data.getCurrentEventData().setRecievedMessage(data.getRecievedMessage());
				data.getCurrentEventData().setAcknowledge(acknowledge);
				synchronized (this.acknowledgeMutex) {
					this.acknowledgeMutex.notifyAll();
				}
			}

		} catch (Exception exception) {
			source.error(exception);
		}
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		CommandLineParser commandLineParser = new CommandLineParser(this);
		try {
			commandLineParser.parseArgument(CommandLineParser.splitArguments(configuration));
			this.acknowledgeMutex = MUTEX_MAP.get(this.name);
			if (this.acknowledgeMutex == null) {
				this.acknowledgeMutex = new Object();
				MUTEX_MAP.put(this.name, this.acknowledgeMutex);
			}

			this.acknowledgeProtocol = acknowledgeProtocolClass.newInstance();
			if ((this.acknowledgeProtocolConfiguration != null) && this.acknowledgeProtocol instanceof Configurable) {
				((Configurable) this.acknowledgeProtocol).configure(this.acknowledgeProtocolConfiguration);
			}
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}
}
