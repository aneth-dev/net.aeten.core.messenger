package net.aeten.core.stream.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import net.aeten.core.Connection;

/**
 * 
 * @author Thomas Pérennou
 */
public class StreamControllerConnection implements
		Connection {

	private final InputStream input;
	private final OutputStream output;
	private final StreamEditor[] editorStack;
	private StreamEditorController[] controllerStack;
	private boolean isConnected;

	public StreamControllerConnection (	final InputStream input,
													final OutputStream output,
													final StreamEditor... editorStack) {
		this.input = input;
		this.output = output;
		this.editorStack = editorStack;
	}

	@Override
	public void connect () throws IOException {
		if (this.isConnected) {
			return;
		}

		// Build controller stack
		this.controllerStack = new StreamEditorController[this.editorStack.length];
		PipedInputStream pipedInputStream = null;
		PipedOutputStream pipedOutputStream = null;
		for (int i = 0; i < this.editorStack.length; i++) {
			if (i < this.editorStack.length - 1) {
				pipedOutputStream = new PipedOutputStream ();
			}
			this.controllerStack[i] = new StreamEditorController ((i == 0)? this.input: pipedInputStream, (i == this.editorStack.length - 1)? this.output: pipedOutputStream, this.editorStack[i]);
			if (i < this.editorStack.length - 1) {
				pipedInputStream = new PipedInputStream (pipedOutputStream);
			}
		}

		for (StreamEditorController controller: this.controllerStack) {
			controller.edit ();
		}

		this.isConnected = true;
	}

	@Override
	public void disconnect () {
		if (!this.isConnected) {
			return;
		}
		for (StreamEditorController controller: this.controllerStack) {
			controller.kill ();
		}
		this.controllerStack = null;
		this.isConnected = false;
	}

	@Override
	public boolean isConnected () {
		return this.isConnected;
	}

}
