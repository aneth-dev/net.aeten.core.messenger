package net.aeten.core.messenger;

public interface MessageEncoder<Message> {

	public byte[] encode(Message message) throws EncodingException;

	public static class EncodingException extends Exception {

		private static final long serialVersionUID = 7072317631168087097L;

		public EncodingException(String message) {
			super(message);
		}

		public EncodingException(Throwable cause) {
			super(cause);
		}

		public EncodingException(String message, Throwable cause) {
			super(message, cause);
		}

	}
}
