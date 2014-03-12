package net.aeten.core.messenger;

public interface MessageDecoder<Message> {

	public Message decode (	byte[] data,
									int offset,
									int length) throws DecodingException;

	public static class DecodingException extends
			Exception {

		private static final long serialVersionUID = 7072317631168087097L;

		public DecodingException (String message) {
			super (message);
		}

		public DecodingException (Throwable cause) {
			super (cause);
		}

		public DecodingException (	String message,
											Throwable cause) {
			super (message, cause);
		}

	}
}
