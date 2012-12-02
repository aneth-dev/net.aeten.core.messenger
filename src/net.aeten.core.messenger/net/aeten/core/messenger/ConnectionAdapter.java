package net.aeten.core.messenger;

import java.io.IOException;

import net.aeten.core.Connection;
import net.aeten.core.Identifiable;
import net.aeten.core.spi.FieldInit;

abstract class ConnectionAdapter implements
		Identifiable,
		Connection {
	@FieldInit
	protected final String identifier;

	protected volatile boolean connected = false;

	public ConnectionAdapter (String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier () {
		return identifier;
	}

	@Override
	public boolean isConnected () {
		return this.connected;
	}

	@Override
	public final synchronized void connect () throws IOException {
		if (!this.isConnected ()) {
			try {
				this.doConnect ();
				this.connected = true;
			} catch (Exception exception) {
				this.connected = false;
				throw new IOException (exception);
			}
		}
	}

	@Override
	public synchronized void disconnect () throws IOException {
		if (this.isConnected ()) {
			try {
				this.doDisconnect ();
				this.connected = false;
			} catch (IOException exception) {
				this.connected = true;
				throw exception;
			}
		}
	}

	protected abstract void doConnect () throws IOException;

	protected abstract void doDisconnect () throws IOException;

}