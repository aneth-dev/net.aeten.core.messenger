package org.pititom.core.messenger.service;

import java.io.IOException;

public interface MessageDecoder<Message> {
	public Message decode(byte[] data, int offset, int length) throws IOException, ClassNotFoundException;
}
