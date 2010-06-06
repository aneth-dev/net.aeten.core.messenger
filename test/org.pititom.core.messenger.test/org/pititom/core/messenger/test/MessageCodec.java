package org.pititom.core.messenger.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.messenger.service.MessageDecoder;
import org.pititom.core.messenger.service.MessageEncoder;

public class MessageCodec implements MessageDecoder<AbstractMessage>, MessageEncoder<AbstractMessage> {
	private ByteArrayOutputStream buffer;
	private ObjectOutputStream out;

	public MessageCodec() {
		this.buffer = new ByteArrayOutputStream();
		try {
			this.out = new TestObjectOutputStream(this.buffer);
		} catch (IOException exception) {
			// This should not happen
			Logger.log(this, LogLevel.ERROR, exception);
		}
	
	}
	
	@Override
	public AbstractMessage decode(byte[] data, int offset, int length) throws IOException, ClassNotFoundException {
		return (AbstractMessage) new TestObjectInputStream(new ByteArrayInputStream(data, offset, length)).readObject();
	}

	@Override
	public byte[] encode(AbstractMessage message) {
		try {
			this.buffer.reset();
			this.out.writeObject(message);
			return this.buffer.toByteArray();
		} catch (IOException exception) {
			// This should not happen
			Logger.log(this, LogLevel.ERROR, exception);
			return null;
		}
	}

}
