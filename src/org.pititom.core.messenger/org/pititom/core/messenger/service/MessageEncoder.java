package org.pititom.core.messenger.service;


public interface MessageEncoder<Message> {
	public byte[] encode(Message message) throws EncodingException;
}
