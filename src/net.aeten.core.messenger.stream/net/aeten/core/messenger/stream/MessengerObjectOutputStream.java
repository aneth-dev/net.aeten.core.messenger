package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.OutputStream;

import net.aeten.core.stream.ObjectOutputStream;

/**
*
* @author Thomas Pérennou
*/
public class MessengerObjectOutputStream extends
		ObjectOutputStream {

	public MessengerObjectOutputStream (OutputStream out)
			throws IOException {
		super (out);
	}

	@Override
	protected void writeClass (Class <?> clazz) throws IOException {
		this.writeInt (clazz.getName ().length ());
		this.write (clazz.getName ().getBytes (MessengerEditor.CLASS_NAME_CHARSET));
	}
}
