package net.aeten.core.stream.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import net.aeten.core.stream.editor.StreamEditor;

/**
 * 
 * @author Thomas Pérennou
 */
public class Decoder implements StreamEditor {
	private ObjectOutputStream	out	= null;

	@Override
	public void edit(DataInputStream in, DataOutputStream out) throws IOException {
		if (this.out == null)
			this.out = new ObjectOutputStream(out);
		int length = in.readInt();
		if (length > 4) {
			byte[] data = new byte[length - 4];
			in.read(data);
			this.out.writeObject(new String(data));
		}
	}

}
