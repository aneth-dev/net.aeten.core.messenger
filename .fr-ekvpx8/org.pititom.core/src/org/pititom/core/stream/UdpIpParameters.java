package org.pititom.core.stream;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.pititom.core.data.Parameters;
import org.pititom.core.stream.controller.ConfigurationException;

public class UdpIpParameters {
	public static enum ParameterKey {
		SOURCE_ADDRESS, DESTINATION_ADDRESS, DESTINATION_PORT, PACKET_MAX_LENGTH, REUSE;
	}

	private final InetSocketAddress destinationInetSocketAddress;
	private final int maxPacketSize;
	private final boolean reuse;
	private InetAddress sourceInetAddress;
	private DatagramSocket socket;

	public UdpIpParameters(Parameters<ParameterKey> parameters)
	        throws IOException, ConfigurationException {
		this.destinationInetSocketAddress = new InetSocketAddress(parameters
		        .get(ParameterKey.DESTINATION_ADDRESS), Integer
		        .valueOf(parameters.get(ParameterKey.DESTINATION_PORT)));
		final String sourceAddressParameter = parameters
		        .get(ParameterKey.SOURCE_ADDRESS);
		this.sourceInetAddress = null;
		if ((sourceAddressParameter != null)
		        && !sourceAddressParameter.equals("")) {
			try {
				this.sourceInetAddress = InetAddress
				        .getByName(sourceAddressParameter);
			} catch (UnknownHostException exception) {
				throw new ConfigurationException("Unknown host : "
				        + sourceAddressParameter, exception);
			}
		}
		this.maxPacketSize = Integer.valueOf(parameters
		        .get(ParameterKey.PACKET_MAX_LENGTH));
		this.reuse = Boolean.valueOf(parameters.get(ParameterKey.REUSE));

		this.createSocket();
	}

	public UdpIpParameters(InetSocketAddress destinationInetSocketAddress,
	        InetAddress sourceInetAddress, boolean reuse, int maxPacketSize)
	        throws IOException, ConfigurationException {
		this.destinationInetSocketAddress = destinationInetSocketAddress;
		this.sourceInetAddress = sourceInetAddress;
		this.reuse = reuse;
		this.maxPacketSize = maxPacketSize;

		this.createSocket();
	}

	private void createSocket() throws IOException {
		if (this.destinationInetSocketAddress.getAddress().isMulticastAddress()) {
			MulticastSocket multicastSocket = new MulticastSocket(
			        (SocketAddress) null);
			if (this.sourceInetAddress != null)
				multicastSocket.setInterface(this.sourceInetAddress);
			multicastSocket.bind(new InetSocketAddress(
			        this.destinationInetSocketAddress.getPort()));
			multicastSocket.joinGroup(this.destinationInetSocketAddress
			        .getAddress());
			this.socket = multicastSocket;
		} else {
			this.socket = new DatagramSocket(this.destinationInetSocketAddress);
		}
		this.socket.setReuseAddress(reuse);
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public InetSocketAddress getDestinationInetSocketAddress() {
		return destinationInetSocketAddress;
	}

	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	public boolean isReuse() {
		return reuse;
	}

	public InetAddress getSourceInetAddress() {
		return sourceInetAddress;
	}

}
