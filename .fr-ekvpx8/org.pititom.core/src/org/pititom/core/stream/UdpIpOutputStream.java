package org.pititom.core.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.pititom.core.data.Parameters;
import org.pititom.core.stream.UdpIpParameters.ParameterKey;
import org.pititom.core.stream.controller.ConfigurationException;

public class UdpIpOutputStream extends OutputStream {
	private final UdpIpParameters parameters;
	private final ByteBuffer buffer;

	public UdpIpOutputStream(InetSocketAddress destinationInetSocketAddress,
	        InetAddress sourceInetAddress, boolean reuse, int maxPacketSize)
	        throws IOException, ConfigurationException {
		this.parameters = new UdpIpParameters(destinationInetSocketAddress,
		        sourceInetAddress, reuse, maxPacketSize);
		this.buffer = ByteBuffer.allocate(this.parameters.getMaxPacketSize());
	}

	public UdpIpOutputStream(Parameters<ParameterKey> parameters)
	        throws ConfigurationException, IOException {
		this.parameters = new UdpIpParameters(parameters);
		this.buffer = ByteBuffer.allocate(this.parameters.getMaxPacketSize());
	}

	@Override
	public void write(int b) throws IOException {
		if (this.buffer.position() == this.buffer.capacity())
			this.flush();
		this.buffer.put((byte) b);
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		for (int i=offset; i<length ; i++)
			this.write(data[i]);
	}

	public void flush() throws IOException {
		this.parameters.getSocket().send(
		        new DatagramPacket(this.buffer.array(), this.buffer.position(),
		                this.parameters.getDestinationInetSocketAddress()));
		this.buffer.clear();
	}

	public void close() throws IOException {
		this.parameters.getSocket().close();
	}

}
