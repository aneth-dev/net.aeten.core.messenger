package net.aeten.core.messenger.test;

/**
 *
 * @author Thomas Pérennou
 */
public class AcknowledgeMessage extends AbstractMessage {
	private static final long serialVersionUID = -3235378810942042205L;

	public AcknowledgeMessage() {}

	public AcknowledgeMessage(Acknowledge acknowledge) {
		this.setAcknowledge(acknowledge);
	}

	@Override
	protected int getSize() {
		return 0;
	}
}
