package net.aeten.core.stream.editor;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Thomas Pérennou
 */
public class StreamEditorController {

	public static enum State {

		NOT_STARTED,
		IN_PROGRESS,
		FINISHED,
		FAILED,
		KILL_SIGNAL_SENT,
		KILLED,
		INTERRUPTED,
		ALLREADY_LAUNCHED
	}

	private final OutputStream out;
	private final InputStream in;
	private final StreamEditor editor;
	private State state;
	private Thread editorThread;
	private final Runnable editorLoop;

	public StreamEditorController (	InputStream inputStream,
												OutputStream outputStream,
												StreamEditor editor) {
		this.in = inputStream;
		this.out = outputStream;
		this.editor = editor;
		this.editorLoop = new EditorLoop ();
		this.state = State.NOT_STARTED;
	}

	public State edit () {
		if (this.state == State.NOT_STARTED) {
			this.editorThread = new Thread (this.editorLoop, "Stream Editor " + this.editor);
			this.editorThread.start ();
		} else {
			this.state = State.ALLREADY_LAUNCHED;
		}
		return state;
	}

	public void kill () {
		this.state = State.KILLED;
	}

	public void forceKill () throws Exception {
		this.in.close ();
	}

	public State editAndWait () {
		if (this.state == State.NOT_STARTED) {
			this.editorThread = new Thread (this.editorLoop, "Stream Editor" + this.editor);
			this.editorThread.start ();
		} else {
			this.state = State.ALLREADY_LAUNCHED;
		}
		return this.state;
	}

	public State getState () {
		return this.state;
	}

	private class EditorLoop implements
			Runnable {

		@Override
		public void run () {
			final DataInputStream inputStream;
			if (StreamEditorController.this.in instanceof DataInput) {
				inputStream = (DataInputStream) StreamEditorController.this.in;
			} else {
				inputStream = new DataInputStream (StreamEditorController.this.in);
			}

			final DataOutputStream outputStream;
			if (StreamEditorController.this.out instanceof DataOutput) {
				outputStream = (DataOutputStream) StreamEditorController.this.out;
			} else {
				outputStream = new DataOutputStream (StreamEditorController.this.out);
			}

			StreamEditorController.this.state = State.IN_PROGRESS;
			try {
				while (state == State.IN_PROGRESS) {
					editor.edit (inputStream, outputStream);
					StreamEditorController.this.out.flush ();
				}
			} catch (IOException exception) {
				StreamEditorController.this.state = State.FAILED;
			}
			if (StreamEditorController.this.state == State.KILL_SIGNAL_SENT) {
				StreamEditorController.this.state = State.KILLED;
			} else {
				StreamEditorController.this.state = State.FINISHED;
			}
			StreamEditorController.this.editorThread = null;
		}
	}
}
