package net.aeten.core.messenger.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Thomas Pérennou
 */
public abstract class AbstractMessage implements Serializable {
	private static final long serialVersionUID = -6143937359211512446L;

	private Acknowledge acknowledge;
	private static AtomicInteger counter = new AtomicInteger();
	private final int id = counter.incrementAndGet();

	public Acknowledge getAcknowledge() {
		return acknowledge;
	}

	public void setAcknowledge(Acknowledge acknowledge) {
		this.acknowledge = acknowledge;
	}

	protected void readObject(ObjectInputStream in) throws IOException {
		final int size = in.readInt();
		if (size != (this.getSize() + 4)) { throw new IOException("Invalid size. Recieved=" + size + " expected=" + this.getSize() + 4); }
		final int ordinal = in.readInt();
		this.acknowledge = ((ordinal >= 0) && (ordinal < Acknowledge.values().length))? Acknowledge.values()[ordinal]: null;
	}

	protected void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(4 + this.getSize());
		out.writeInt((this.acknowledge == null)? -1: this.acknowledge.ordinal());
	}

	protected abstract int getSize();

	@Override
	public String toString() {
		return "id=" + id + ", acknowledge=" + this.acknowledge;
	}
}
