package net.aeten.core.stream.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import net.aeten.core.stream.editor.StreamEditor;

/**
 * 
 * @author Thomas Pérennou
 */
public class Encoder implements StreamEditor {

	private ObjectInputStream	in	= null;

	@Override
	public void edit(DataInputStream in, DataOutputStream out) throws IOException {
		if (this.in == null) {
			this.in = new ObjectInputStream(in);
		}
		try {
			Object object = this.in.readObject();
			if (object instanceof String) {
				String command = (String) object;
				byte[] dada = command.toUpperCase().getBytes();
				out.writeInt(4 + dada.length);
				out.write(dada);
			}
		} catch (ClassNotFoundException exception) {
			exception.printStackTrace();
		}
	}
}
