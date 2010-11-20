package org.pititom.core.messenger.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.pititom.core.messenger.MessageDecoder;
import org.pititom.core.messenger.MessageEncoder;

public class MessageCodec implements MessageDecoder<AbstractMessage>, MessageEncoder<AbstractMessage> {
	private ByteArrayOutputStream buffer;
	private ObjectOutputStream out;

	public MessageCodec() throws IOException {
		this.buffer = new ByteArrayOutputStream();
		this.out = new TestObjectOutputStream(this.buffer);
	}

	@Override
	public AbstractMessage decode(byte[] data, int offset, int length) throws DecodingException {
		try {
			return (AbstractMessage) new TestObjectInputStream(new ByteArrayInputStream(data, offset, length)).readObject();
		} catch (Throwable exception) {
			throw new DecodingException(exception);
		}
	}

	@Override
	public byte[] encode(AbstractMessage message) throws EncodingException {
		try {
			this.buffer.reset();
			this.out.writeObject(message);
			return this.buffer.toByteArray();
		} catch (Throwable exception) {
			throw new EncodingException(exception);
		}
	}

}
