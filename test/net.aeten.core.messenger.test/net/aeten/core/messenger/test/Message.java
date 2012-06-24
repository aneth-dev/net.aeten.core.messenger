package net.aeten.core.messenger.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author Thomas Pérennou
 */
public class Message extends AbstractMessage {
	private static final long serialVersionUID = 5697397659744712657L;
	public static final int MIN_VALUE = 2;
	public static final int MAX_VALUE = 5;
	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	protected void readObject(ObjectInputStream in) throws IOException {
		super.readObject(in);
		this.value = in.readInt();
	}

	@Override
	protected void writeObject(ObjectOutputStream out) throws IOException {
		super.writeObject(out);
		out.writeInt(this.value);
	}

	@Override
	public String toString() {
		return super.toString() + "; value=" + this.value;
	}

	@Override
	protected int getSize() {
		return 4;
	}
}
