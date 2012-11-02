package net.aeten.core.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;
import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpIpClient {
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
	@FieldInit (required = false)
	final boolean bind;

	/**
	 * @see {@link Socket#setSoTimeout(int)}
	 */
	@FieldInit (alias = "time out",
					required = false)
	final Integer timeout;

	private static final Logger LOGGER = LoggerFactory.getLogger (TcpIpClient.class);
	@GuardedBy ("CLIENTS")
	private static final Map <InetSocketAddress, TcpIpClient> CLIENTS = new HashMap <> ();
	@GuardedBy ("CLIENTS")
	private static final Map <InetSocketAddress, Integer> USES = new HashMap <> ();

	protected final Socket socket;

	private TcpIpClient (InetSocketAddress destination,
								boolean bind,
								boolean reuse,
								int timeout)
			throws IOException {
		this.destination = destination;
		this.bind = bind;
		this.reuse = reuse;
		this.timeout = timeout;
		socket = createSocket ();
	}

	static TcpIpClient get (InetSocketAddress destination,
									boolean bind,
									boolean reuse,
									int timeout) throws IOException {
		synchronized (CLIENTS) {
			Integer uses = USES.get (destination);
			USES.put (destination, (uses == null)? 1: uses++);
			TcpIpClient client = CLIENTS.get (destination);
			if (client == null) {
				client = new TcpIpClient (destination, bind, reuse, timeout);
				CLIENTS.put (destination, client);
			}
			return client;
		}
	}

	static void release (InetSocketAddress destination) {
		synchronized (CLIENTS) {
			Integer uses = USES.get (destination) - 1;
			if (uses == 0) {
				CLIENTS.remove (destination);
				USES.remove (destination);
			} else {
				USES.put (destination, uses);
			}
		}
	}

	private Socket createSocket () throws IOException {
		Socket socket = new Socket (destination.getAddress (), destination.getPort ());
		if (bind) {
			if (!socket.isBound ()) {
				LOGGER.info ("Bind on {}", destination);
				socket.bind (destination);
			} else {
				LOGGER.warn ("Inet socket address {} already bound", destination);
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

		private final java.io.InputStream in;
		private final InetSocketAddress destination;

		public InputStream (@SpiInitializer TcpIpClientInputStreamInitializer init)
				throws IOException {
			this (init.getDestination (), init.hasBind ()? init.getBind (): false, init.hasReuse ()? init.getReuse (): false, init.hasTimeout ()? init.getTimeout (): -1);
		}

		public InputStream (InetSocketAddress destination)
				throws IOException {
			this (destination, false, false, -1);
		}

		public InputStream (	InetSocketAddress destination,
									boolean bind,
									boolean reuse,
									int timeout)
				throws IOException {
			in = new BufferedInputStream (TcpIpClient.get (destination, bind, reuse, timeout).socket.getInputStream ());
			this.destination = destination;
		}

		@Override
		public String toString () {
			return InputStream.class.getName () + " (" + destination + ")";
		}

		@Override
		public int read () throws IOException {
			return in.read ();
		}

		@Override
		public int available () throws IOException {
			return in.available ();
		}

		@Override
		public void close () throws IOException {
			in.close ();
		}

		@Override
		public synchronized void reset () throws IOException {
			in.reset ();
		}

		@Override
		public synchronized void mark (int readlimit) {
			in.mark (readlimit);
		}

		@Override
		public boolean markSupported () {
			return in.markSupported ();
		}

		@Override
		protected void finalize () throws Throwable {
			TcpIpClient.release (destination);
			super.finalize ();
		}
	}

	public static class OutputStream extends
			java.io.OutputStream {
		private final java.io.OutputStream out;
		private final InetSocketAddress destination;

		public OutputStream (@SpiInitializer TcpIpClientOutputStreamInitializer init)
				throws IOException {
			this (init.getDestination (), init.hasBind ()? init.getBind (): false, init.hasReuse ()? init.getReuse (): false, init.hasTimeout ()? init.getTimeout (): -1);
		}

		public OutputStream (InetSocketAddress destination)
				throws IOException {
			this (destination, false, false, -1);
		}

		public OutputStream (InetSocketAddress destination,
									boolean bind,
									boolean reuse,
									int timeout)
				throws IOException {
			out = new BufferedOutputStream (TcpIpClient.get (destination, bind, reuse, timeout).socket.getOutputStream ());
			this.destination = destination;
		}

		@Override
		public String toString () {
			return OutputStream.class.getName () + " (" + destination + ")";
		}

		@Override
		public void write (int b) throws IOException {
			out.write (b);
		}

		@Override
		public void write (	byte[] data,
									int offset,
									int length) throws IOException {
			out.write (data, offset, length);
		}

		@Override
		public void flush () throws IOException {
			out.flush ();
		}

		@Override
		public void close () throws IOException {
			out.close ();
		}

		@Override
		protected void finalize () throws Throwable {
			TcpIpClient.release (destination);
			super.finalize ();
		}
	}

}
