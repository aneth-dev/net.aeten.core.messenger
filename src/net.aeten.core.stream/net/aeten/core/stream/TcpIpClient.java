package net.aeten.core.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;
import net.jcip.annotations.GuardedBy;

public class TcpIpClient {
	/**
	 * @see {@link Socket#bind(SocketAddress)} (IP address)
	 */
	@FieldInit(alias = "interface")
	final InetSocketAddress destination;

	/**
	 * @see {@link Socket#setReuseAddress(boolean)}
	 */
	@FieldInit(required = false)
	final boolean reuse;

	/**
	 * @see {@link Socket#bind(SocketAddress)}
	 */
	@FieldInit(required = false)
	final boolean bind;

	/**
	 * @see {@link Socket#setSoTimeout(int)}
	 */
	@FieldInit(	alias = "time out",
					required = false)
	final Integer timeout;

	@GuardedBy("self")
	private static final Map<InetSocketAddress, Socket> SOCKETS = new HashMap<> ();
	@GuardedBy("SOCKETS")
	private static final Map<Class<?>, Socket> OWNED = new HashMap<> ();

	protected final Socket socket;

	public TcpIpClient(InetSocketAddress destination,
			boolean bind,
			boolean reuse,
			int timeout)
			throws IOException {
		this.destination = destination;
		this.bind = bind;
		this.reuse = reuse;
		this.timeout = timeout;

		synchronized (SOCKETS) {
			Socket socket = SOCKETS.get (destination);
			if ((socket != null) && !socket.isClosed () && OWNED.get (getClass ()) != null) {
				throw new IOException ("Socket not closed");
			}
			if (socket == null || socket.isClosed ()) {
				socket = createSocket ();
				OWNED.put (getClass (), socket);
				SOCKETS.put (destination, socket);
			}
			this.socket = socket;
		}

	}

	private Socket createSocket()
			throws IOException {
		Socket socket = new Socket (destination.getAddress (), destination.getPort ());
		if (bind) {
			if (!socket.isBound ()) {
				Logger.log (this, LogLevel.INFO, "Bind on " + destination);
				socket.bind (destination);
			} else {
				Logger.log (this, LogLevel.WARN, "Inet socket address" + destination + " already bound");
			}
		}
		if (this.timeout != -1) {
			socket.setSoTimeout (this.timeout);
		}
		socket.setReuseAddress (this.reuse);
		return socket;
	}

	public static class InputStream extends
			java.io.InputStream {

		private final TcpIpClient client;

		public InputStream(@SpiInitializer TcpIpClientInputStreamInitializer init)
				throws IOException {
			this (init.getDestination (), init.hasBind () ? init.getBind () : false, init.hasReuse () ? init.getReuse () : false, init.hasTimeout () ? init.getTimeout () : -1);
		}

		public InputStream(InetSocketAddress destination)
				throws IOException {
			this (destination, false, false, -1);
		}

		public InputStream(InetSocketAddress destination,
				boolean bind,
				boolean reuse,
				int timeout)
				throws IOException {
			client = new TcpIpClient (destination, bind, reuse, timeout);
		}

		@Override
		public String toString() {
			return InputStream.class.getName () + " (" + client.destination + ")";
		}

		@Override
		public int read()
				throws IOException {
			return client.socket.getInputStream ().read ();
		}

		@Override
		public int available()
				throws IOException {
			return client.socket.getInputStream ().available ();
		}

		@Override
		public void close()
				throws IOException {
			client.socket.close ();
		}

		@Override
		public synchronized void reset()
				throws IOException {
			client.socket.getInputStream ().reset ();
		}

		@Override
		public synchronized void mark(int readlimit) {
			try {
				client.socket.getInputStream ().mark (readlimit);
			} catch (IOException exception) {
				throw new IllegalStateException (exception);
			}
		}

		@Override
		public boolean markSupported() {
			try {
				return client.socket.getInputStream ().markSupported ();
			} catch (IOException exception) {
				throw new IllegalStateException (exception);
			}
		}
	}

	public static class OutputStream extends
			java.io.OutputStream {
		private final TcpIpClient client;

		public OutputStream(@SpiInitializer TcpIpClientOutputStreamInitializer init)
				throws IOException {
			this (init.getDestination (), init.hasBind () ? init.getBind () : false, init.hasReuse () ? init.getReuse () : false, init.hasTimeout () ? init.getTimeout () : -1);
		}

		public OutputStream(InetSocketAddress destination)
				throws IOException {
			this (destination, false, false, -1);
		}

		public OutputStream(InetSocketAddress destination,
				boolean bind,
				boolean reuse,
				int timeout)
				throws IOException {
			client = new TcpIpClient (destination, bind, reuse, timeout);
		}

		@Override
		public String toString() {
			return OutputStream.class.getName () + " (" + client.destination + ")";
		}

		@Override
		public void write(int b)
				throws IOException {
			client.socket.getOutputStream ().write (b);
		}

		@Override
		public void write(byte[] data,
				int offset,
				int length)
				throws IOException {
			client.socket.getOutputStream ().write (data, offset, length);
		}

		@Override
		public void flush()
				throws IOException {
			client.socket.getOutputStream ().flush ();
		}

		@Override
		public void close()
				throws IOException {
			client.socket.close ();
		}
	}

}
