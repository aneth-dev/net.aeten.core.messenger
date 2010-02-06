package org.pititom.core.stream.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface StreamEditor {
	public void edit(DataInputStream in, DataOutputStream out) throws IOException;
}
