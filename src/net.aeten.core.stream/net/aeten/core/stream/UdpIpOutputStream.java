package net.aeten.core.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.aeten.core.net.UdpIpSocketFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;

/**
 *
 * @author Thomas Pérennou
 */
public class UdpIpOutputStream extends
		OutputStream {

	@FieldInit (alias = {
			"udp ip configuration",
			"UDP/IP configuration"
	})
	private final UdpIpSocketFactory socketFactory;
	private byte[] buffer;
	private int position = 0;

	public UdpIpOutputStream (@SpiInitializer UdpIpOutputStreamInitializer init) {
		this (init.getSocketFactory ());
	}

	public UdpIpOutputStream (UdpIpSocketFactory socketFactory) {
		this.socketFactory = socketFactory;
		this.buffer = new byte[this.socketFactory.getMaxPacketSize ()];
	}

	public UdpIpOutputStream (	InetSocketAddress destinationInetSocketAddress,
										InetAddress sourceInetAddress,
										boolean autoBind,
										boolean reuse,
										int maxPacketSize)
			throws IOException {
		this (new UdpIpSocketFactory (destinationInetSocketAddress, sourceInetAddress, autoBind, reuse, maxPacketSize));
		this.buffer = new byte[this.socketFactory.getMaxPacketSize ()];
	}

	@Override
	public void write (int b) throws IOException {
		if (this.buffer == null) {
			throw new IOException ("Stream must be configured");
		}
		if (this.position == this.socketFactory.getMaxPacketSize ()) {
			this.flush ();
		}
		this.buffer[this.position++] = (byte) b;
	}

	@Override
	public void write (	byte[] data,
								int offset,
								int length) throws IOException {
		for (int i = offset; i < length; i++) {
			this.write (data[i]);
		}
	}

	@Override
	public void flush () throws IOException {
		this.socketFactory.getSocket ().send (new DatagramPacket (this.buffer, this.position, this.socketFactory.getDestinationInetSocketAddress ()));
		this.position = 0;
	}

	@Override
	public void close () throws IOException {
		this.socketFactory.getSocket ().close ();
	}
}
