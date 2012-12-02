package net.aeten.core.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;
import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpIpServer {
	/**
	 * @see {@link Socket#bind(SocketAddress)} (IP address)
	 */
	@FieldInit (alias = "interface")
	final InetSocketAddress destination;

	/**
	 * @see {@link Socket#setReuseAddress(boolean)}
	 */
	@FieldInit (required = false)
	final boolean reuse;

	/**
	 * @see {@link Socket#bind(SocketAddress)}
	 */
	@FieldInit (alias = {
							"back log",
							"max length"
					},
					required = false)
	final int backlog;

	/**
	 * @see {@link Socket#bind(SocketAddress)}
	 */
	@FieldInit (required = false)
	final boolean bind;

	/**
	 * @see {@link Socket#setSoTimeout(int)}
	 */
	@FieldInit (alias = "time out",
					required = false)
	final Integer timeout;

	private static final Logger LOGGER = LoggerFactory.getLogger (TcpIpClient.class);
	@GuardedBy ("SERVERS")
	private static final Map <InetSocketAddress, TcpIpServer> SERVERS = new HashMap <> ();
	@GuardedBy ("SERVERS")
	private static final Map <InetSocketAddress, Integer> USES = new HashMap <> ();

	protected final ServerSocket serverSocket;
	@GuardedBy ("serverSocket")
	protected volatile Socket socket;
	@GuardedBy ("serverSocket")
	private java.io.InputStream in;
	private java.io.OutputStream out;

	private TcpIpServer (InetSocketAddress destination,
								boolean bind,
								boolean reuse,
								int timeout,
								int backlog)
			throws IOException {
		this.destination = destination;
		this.bind = bind;
		this.reuse = reuse;
		this.timeout = timeout;
		this.backlog = backlog;
		serverSocket = createSocket ();
	}

	static TcpIpServer get (InetSocketAddress destination,
									boolean bind,
									boolean reuse,
									int timeout,
									int backlog) throws IOException {
		synchronized (SERVERS) {
			Integer uses = USES.get (destination);
			USES.put (destination, (uses == null)? 1: uses + 1);
			TcpIpServer server = SERVERS.get (destination);
			if (server == null) {
				server = new TcpIpServer (destination, bind, reuse, timeout, backlog);
				SERVERS.put (destination, server);
			}
			return server;
		}
	}

	static void release (InetSocketAddress destination) {
		synchronized (SERVERS) {
			Integer uses = USES.get (destination) - 1;
			if (uses == 0) {
				SERVERS.remove (destination);
				USES.remove (destination);
			} else {
				USES.put (destination, uses);
			}
		}
	}

	final java.io.InputStream getInputStream () throws IOException {
		synchronized (serverSocket) {
			if (!serverSocket.isBound ()) {
				bind ();
			}
			if ((socket == null || socket.isInputShutdown ()) && !serverSocket.isClosed ()) {
				socket = serverSocket.accept ();
				in = new BufferedInputStream (socket.getInputStream ());
				out = new BufferedOutputStream (socket.getOutputStream ());
			}
			return in;
		}
	}

	final java.io.OutputStream getOutputStream () throws IOException {
		synchronized (serverSocket) {
			return out;
		}
	}

	protected void bind () throws IOException {
		if (!serverSocket.isBound ()) {
			LOGGER.info ("Bind on {}", destination);
			serverSocket.bind (destination, backlog);
		} else {
			LOGGER.warn ("Inet socket address {} already bound", destination);
		}
	}

	private ServerSocket createSocket () throws IOException {
		ServerSocket socket = new ServerSocket ();
		if (bind) {
			bind ();
		}
		if (this.timeout != -1) {
			socket.setSoTimeout (this.timeout);
		}
		socket.setReuseAddress (this.reuse);
		return socket;
	}

	public static class InputStream extends
			java.io.InputStream {

		private final TcpIpServer server;
		@GuardedBy ("server.serverSocket")
		/* volatile for finalize, lock for close */
		private volatile boolean released = false;

		public InputStream (@SpiInitializer TcpIpServerInputStreamInitializer init)
				throws IOException {
			this (init.getDestination (), init.hasBind ()? init.getBind (): false, init.hasReuse ()? init.getReuse (): false, init.hasTimeout ()? init.getTimeout (): -1, init.hasBacklog ()? init.getBacklog (): -1);
		}

		public InputStream (InetSocketAddress destination)
				throws IOException {
			this (destination, false, false, -1, -1);
		}

		public InputStream (	InetSocketAddress destination,
									boolean bind,
									boolean reuse,
									int timeout,
									int backlog)
				throws IOException {
			server = TcpIpServer.get (destination, bind, reuse, timeout, backlog);
		}

		@Override
		public String toString () {
			return InputStream.class.getName () + " (" + server.destination + ")";
		}

		@Override
		public int read () throws IOException {
			return server.getInputStream ().read ();
		}

		@Override
		public int read (	byte[] b,
								int off,
								int len) throws IOException {
			return server.getInputStream ().read (b, off, len);
		}

		@Override
		public int available () throws IOException {
			synchronized (server.serverSocket) {
				if (server.socket == null || server.socket.isClosed ()) {
					return 0;
				}
				return server.getInputStream ().available ();
			}
		}

		@Override
		public void close () throws IOException {
			server.serverSocket.close ();
			server.getInputStream ().close ();
			server.getOutputStream ().close ();
			Socket client;
			synchronized (server.serverSocket) {
				client = server.socket;
				if (!released) {
					release (server.destination);
					released = true;
				}
			}
			if (client != null) {
				client.close ();
			}
		}

		@Override
		public synchronized void reset () throws IOException {
			synchronized (server.serverSocket) {
				try {
					java.io.InputStream in = server.getInputStream ();
					if (in.markSupported ()) {
						in.reset ();
						return;
					}
				} catch (IOException exception) {}
				server.socket = null;
			}
		}

		@Override
		public synchronized void mark (int readlimit) {
			try {
				java.io.InputStream in = server.getInputStream ();
				if (in.markSupported ()) {
					in.mark (readlimit);
				}
			} catch (IOException exception) {
				throw new IllegalStateException (exception);
			}
		}

		@Override
		public boolean markSupported () {
			synchronized (server.serverSocket) {
				return server.socket == null? false: server.socket.isBound ();
			}
		}

		@Override
		protected void finalize () throws Throwable {
			if (!released) {
				TcpIpServer.release (server.destination);
			}
			super.finalize ();
		}
	}

	public static class OutputStream extends
			java.io.OutputStream {
		private final TcpIpServer server;
		@GuardedBy ("server.serverSocket")
		/* volatile for finalize, lock for close */
		private volatile boolean released = false;

		public OutputStream (@SpiInitializer TcpIpServerOutputStreamInitializer init)
				throws IOException {
			this (init.getDestination (), init.hasBind ()? init.getBind (): false, init.hasReuse ()? init.getReuse (): false, init.hasTimeout ()? init.getTimeout (): -1, init.hasBacklog ()? init.getBacklog (): -1);
		}

		public OutputStream (InetSocketAddress destination)
				throws IOException {
			this (destination, false, false, -1, -1);
		}

		public OutputStream (InetSocketAddress destination,
									boolean bind,
									boolean reuse,
									int timeout,
									int backlog)
				throws IOException {
			server = TcpIpServer.get (destination, bind, reuse, timeout, backlog);
		}

		@Override
		public String toString () {
			return OutputStream.class.getName () + " (" + server.destination + ")";
		}

		@Override
		public void write (int b) throws IOException {
			server.getOutputStream ().write (b);
		}

		@Override
		public void write (	byte[] data,
									int offset,
									int length) throws IOException {
			server.getOutputStream ().write (data, offset, length);
		}

		@Override
		public void flush () throws IOException {
			server.getOutputStream ().flush ();
		}

		@Override
		public void close () throws IOException {
			server.serverSocket.close ();
			server.getInputStream ().close ();
			server.getOutputStream ().close ();
			synchronized (server.serverSocket) {
				if (!released) {
					release (server.destination);
					released = true;
				}
			}
		}

		@Override
		protected void finalize () throws Throwable {
			if (!released) {
				TcpIpServer.release (server.destination);
			}
			super.finalize ();
		}
	}

}
