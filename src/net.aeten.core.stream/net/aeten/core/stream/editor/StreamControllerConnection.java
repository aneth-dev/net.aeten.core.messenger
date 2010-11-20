package net.aeten.core.stream.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Connection;
import net.aeten.core.Singleton;

/**
 * 
 * @author Thomas Pérennou
 */
public class StreamControllerConnection implements Connection {

	private InputStream input;
	private OutputStream output;
	private StreamEditor[] editorStack;
	private StreamEditorController[] controllerStack;
	private boolean isConnected;

	public StreamControllerConnection(final InputStream input, final OutputStream output, final StreamEditor... editorStack) {
		this.input = input;
		this.output = output;
		this.editorStack = editorStack;
	}

	public StreamControllerConnection(final StreamControllerConfiguration configuration, final InputStream input, final OutputStream output) throws ConfigurationException {
		this(configuration);
		this.input = input;
		this.output = output;
	}

	public StreamControllerConnection(final StreamControllerConfiguration configuration, final InputStream input) throws ConfigurationException {
		this(configuration);
		this.input = input;
	}

	public StreamControllerConnection(final StreamControllerConfiguration configuration, final OutputStream output) throws ConfigurationException {
		this(configuration);
		this.output = output;
	}

	public StreamControllerConnection(final StreamControllerConfiguration configuration) throws ConfigurationException {

		this.input = configuration.getInputStream();
		this.output = configuration.getOutputStream();

		this.editorStack = new StreamEditor[(configuration.getEditorStack() == null) ? 0 : configuration.getEditorStack().getStack().size()];
		if (this.editorStack.length > 0) {
			int index = 0;
			try {
			for (Singleton<? extends StreamEditor> editorFactory : configuration.getEditorStack().getStack()) {
					this.editorStack[index++] = editorFactory.getInstance();
			}
			} catch (Exception exception) {
				throw new ConfigurationException(configuration.getName(), exception);
			}
		}
	}

	@Override
	public void connect() throws IOException {
		if (this.isConnected) {
			return;
		}

		// Build controller stack
		this.controllerStack = new StreamEditorController[this.editorStack.length];
		PipedInputStream pipedInputStream = null;
		PipedOutputStream pipedOutputStream = null;
		for (int i = 0; i < this.editorStack.length; i++) {
			if (i < this.editorStack.length - 1) {
				pipedOutputStream = new PipedOutputStream();
			}
			this.controllerStack[i] = new StreamEditorController((i == 0) ? this.input : pipedInputStream, (i == this.editorStack.length - 1) ? this.output : pipedOutputStream, this.editorStack[i]);
			if (i < this.editorStack.length - 1) {
				pipedInputStream = new PipedInputStream(pipedOutputStream);
			}
		}

		for (StreamEditorController controller : this.controllerStack) {
			controller.edit();
		}

		this.isConnected = true;
	}

	@Override
	public void disconnect() {
		if (!this.isConnected) {
			return;
		}
		for (StreamEditorController controller : this.controllerStack) {
			controller.kill();
		}
		this.controllerStack = null;
		this.isConnected = false;
	}

	@Override
	public boolean isConnected() {
		return this.isConnected;
	}

}
