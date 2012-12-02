package net.aeten.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.aeten.core.net.UdpIpSocketFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;

/**
 * 
 * @author Thomas Pérennou
 */
public class UdpIpInputStream extends
		InputStream {

	@FieldInit (alias = {
			"udp ip configuration",
			"UDP/IP configuration"
	})
	private final UdpIpSocketFactory socketFactory;

	private final Thread receptionThread;
	private final BlockingQueue <DatagramPacket> queue = new LinkedBlockingQueue <> ();
	private int position = 0;
	private int available = 0;
	private DatagramPacket currentPacket = null;

	public UdpIpInputStream (@SpiInitializer UdpIpInputStreamInitializer init) {
		this (init.getSocketFactory ());
	}

	public UdpIpInputStream (	InetSocketAddress destinationInetSocketAddress,
										InetAddress sourceInetAddress,
										boolean autoBind,
										boolean reuse,
										int maxPacketSize)
			throws IOException {
		this (new UdpIpSocketFactory (destinationInetSocketAddress, sourceInetAddress, autoBind, reuse, maxPacketSize));
	}

	public UdpIpInputStream (UdpIpSocketFactory socketFactory) {
		this.socketFactory = socketFactory;
		this.receptionThread = new Thread (new ReceptionThread (), this.toString ());
		this.receptionThread.start ();
	}

	private class ReceptionThread implements
			Runnable {
		@Override
		public void run () {
			try {
				while ((socketFactory.getSocket () != null) && !socketFactory.getSocket ().isClosed ()) {
					DatagramPacket packet = new DatagramPacket (new byte[socketFactory.getMaxPacketSize ()], socketFactory.getMaxPacketSize (), socketFactory.getDestinationInetSocketAddress ().getAddress (), socketFactory.getDestinationInetSocketAddress ().getPort ());
					try {
						socketFactory.getSocket ().receive (packet);
						available += packet.getLength ();
						queue.put (packet);
					} catch (SocketTimeoutException
								| InterruptedException exception) {
						continue;
					}
				}
			} catch (IOException exception) {
				if (socketFactory.getSocket () != null) socketFactory.getSocket ().close ();
			} finally {
				try {
					UdpIpInputStream.this.close ();
				} catch (IOException exception) {
					exception.printStackTrace ();
				}
			}
		}
	}

	@Override
	public String toString () {
		return UdpIpInputStream.class.getName () + " (" + this.socketFactory + ")";
	}

	@Override
	public int read () throws IOException {
		if (this.currentPacket == null) {
			try {
				this.currentPacket = this.queue.take ();
				this.position = 0;
			} catch (InterruptedException exception) {
				return this.read ();
			}
		}
		if (this.currentPacket.getLength () == this.position) {
			this.currentPacket = null;
			return this.read ();
		}
		this.available--;
		return this.currentPacket.getData ()[this.position++] & 0xFF;
	}

	@Override
	public int available () throws IOException {
		return this.available;
	}

	@Override
	public void close () throws IOException {
		this.receptionThread.interrupt ();
	}

	@Override
	public synchronized void reset () throws IOException {
		this.currentPacket = null;
		// TODO: reset on mark if >= 0
	}

	@Override
	public synchronized void mark (int readlimit) {
		this.position = readlimit;
		// TODO: make a mark field instead of position use
	}

	@Override
	public boolean markSupported () {
		return true;
	}

}
