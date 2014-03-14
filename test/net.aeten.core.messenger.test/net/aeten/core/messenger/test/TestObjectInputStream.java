package net.aeten.core.messenger.test;

import java.io.IOException;
import java.io.InputStream;

import net.aeten.core.stream.ObjectInputStream;

/**
 *
 * @author Thomas Pérennou
 */
public class TestObjectInputStream extends ObjectInputStream {

	public TestObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected Class<?> readClass() throws IOException {
		try {
			final byte[] className = new byte[this.readInt()];
			this.read(className);
			return Thread.currentThread().getContextClassLoader().loadClass(new String(className));
		} catch (ClassNotFoundException exception) {
			throw new IOException(exception);
		}
	}
}
