package org.pititom.core.stream;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.pititom.core.args4j.CommandLineParser;

public class UdpIpParameters {

	@Option(name = "-d", aliases = { "--destination",
	        "--destination-inet-socket-adress" }, required = true)
	private InetSocketAddress destinationInetSocketAddress;

	@Option(name = "-p", aliases = "--max-packet-size", required = true)
	private int maxPacketSize;

	@Option(name = "-r", aliases = "--reuse", required = false)
	private boolean reuse = false;

	@Option(name = "-s", aliases = { "--source", "--source-inet-adress" }, required = false)
	private InetAddress sourceInetAddress = null;

	private DatagramSocket socket;

	public UdpIpParameters(String configuration) throws CmdLineException,
	        IOException {
		this(CommandLineParser.splitArguments(configuration));
	}

	public UdpIpParameters(String... arguments) throws CmdLineException,
	        IOException {
		CommandLineParser parser = new CommandLineParser(this);
		parser.parseArgument(arguments);

		this.createSocket();
	}

	public UdpIpParameters(InetSocketAddress destinationInetSocketAddress,
	        InetAddress sourceInetAddress, boolean reuse, int maxPacketSize)
	        throws IOException {
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
