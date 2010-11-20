package net.aeten.core.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.aeten.core.Configurable;
import net.aeten.core.ConfigurationException;
import net.aeten.core.args4j.UdpIpParameters;


/**
 *
 * @author Thomas Pérennou
 */
public class UdpIpOutputStream extends OutputStream implements Configurable<String> {
	private UdpIpParameters parameters;
	private byte[] buffer;
	private int position = 0;

	public UdpIpOutputStream(UdpIpParameters parameters) {
	    this.parameters = parameters;
	    this.buffer = new byte[this.parameters.getMaxPacketSize()];
	}
	
	public UdpIpOutputStream(InetSocketAddress destinationInetSocketAddress,
	        InetAddress sourceInetAddress, boolean autoBind, boolean reuse, int maxPacketSize)
	        throws IOException {
	   this(new UdpIpParameters(destinationInetSocketAddress,
	                            sourceInetAddress, autoBind, reuse, maxPacketSize));
	    this.buffer = new byte[this.parameters.getMaxPacketSize()];
	}

	public UdpIpOutputStream() {
		this.parameters = null;
		this.buffer = null;
	}

	@Override
	public void write(int b) throws IOException {
		if (this.buffer == null)
			throw new IOException("Stream must be configured");
		if (this.position == this.parameters.getMaxPacketSize())
			this.flush();
		this.buffer[this.position++] = (byte) b;
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		for (int i=offset; i<length ; i++)
			this.write(data[i]);
	}

	@Override
	public void flush() throws IOException {
		this.parameters.getSocket().send(
		        new DatagramPacket(this.buffer, this.position,
		                this.parameters.getDestinationInetSocketAddress()));
		this.position = 0;
	}

	@Override
	public void close() throws IOException {
		this.parameters.getSocket().close();
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		if (this.parameters != null)
			throw new ConfigurationException(configuration, UdpIpInputStream.class
			        .getCanonicalName()
			        + " is already configured");
		try {
			this.parameters = new UdpIpParameters(configuration);
		    this.buffer = new byte[this.parameters.getMaxPacketSize()];
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

}
