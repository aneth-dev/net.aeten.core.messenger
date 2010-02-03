package org.pititom.core.messenger.stream;

import java.io.IOException;
import java.io.OutputStream;

import org.pititom.core.messenger.extension.MessengerEditor;
import org.pititom.core.stream.ObjectOutputStream;

public class MessengerObjectOutputStream extends ObjectOutputStream {

	public MessengerObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override
	protected void writeClass(Class<?> clazz) throws IOException {
		this.writeInt(clazz.getName().length());
		this.write(clazz.getName().getBytes(MessengerEditor.CLASS_NAME_CHARSET));
	}
}
