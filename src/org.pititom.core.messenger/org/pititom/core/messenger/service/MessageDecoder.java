package org.pititom.core.messenger.service;


public interface MessageDecoder<Message> {
	public Message decode(byte[] data, int offset, int length) throws DecodingException;
}
