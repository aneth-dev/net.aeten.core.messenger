package org.pititom.core.test.messenger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AcknowledgeMessage extends AbstractMessage {
	private static final long serialVersionUID = -1074995809088143529L;

	public AcknowledgeMessage() {
	}

	public AcknowledgeMessage(Acknowledge acknowledge) {
		this.setAcknowledge(acknowledge);
	}

	@Override
	protected void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		final int size = in.readInt();
		if (size != 4) { // size
			throw new IOException("Invalid size. Recieved=" + size + " expected=" + 4);
		}
		super.readObject(in);
	}

	@Override
	protected void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(4); // size
		super.writeObject(out);
	}
}
