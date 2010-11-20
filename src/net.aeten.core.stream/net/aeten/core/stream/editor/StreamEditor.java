package net.aeten.core.stream.editor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Thomas Pérennou
 */
public interface StreamEditor {
	public void edit(DataInputStream in, DataOutputStream out) throws IOException;
}
