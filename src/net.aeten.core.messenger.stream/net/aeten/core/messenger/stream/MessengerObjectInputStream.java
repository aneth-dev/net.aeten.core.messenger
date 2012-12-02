package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.InputStream;

import net.aeten.core.stream.ObjectInputStream;

/**
*
* @author Thomas Pérennou
*/
public class MessengerObjectInputStream extends
		ObjectInputStream {

	public MessengerObjectInputStream (InputStream in)
			throws IOException {
		super (in);
	}

	@Override
	protected Class <?> readClass () throws IOException {
		try {
			final byte[] className = new byte[this.readInt ()];
			this.read (className);
			return Class.forName (new String (className, MessengerEditor.CLASS_NAME_CHARSET), true, this.getClass ().getClassLoader ());
		} catch (ClassNotFoundException exception) {
			throw new IOException (exception);
		}
	}
}
