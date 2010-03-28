package org.pititom.core.stream;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pititom.core.args4j.CommandLineParserHelper;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;

/**
 * 
 * @author Thomas Pérennou
 */
public class UdpIpParameters {

	@Option(name = "-d", aliases = { "--destination", "--destination-inet-socket-address" }, required = true)
	private InetSocketAddress destinationInetSocketAddress;

	@Option(name = "-p", aliases = "--max-packet-size", required = true)
	private int maxPacketSize;

	@Option(name = "-r", aliases = "--reuse", required = false)
	private boolean reuse = false;

	@Option(name = "-b", aliases = "--bind", required = false)
	private boolean bind = false;

	@Option(name = "-s", aliases = { "--source", "--source-inet-address" }, required = false)
	private InetAddress sourceInetAddress = null;

	private DatagramSocket socket;

	public UdpIpParameters(String configuration) throws CmdLineException, IOException {
		this(CommandLineParserHelper.splitArguments(configuration));
	}

	public UdpIpParameters(String... arguments) throws CmdLineException, IOException {
		CmdLineParser parser = new CmdLineParser(this);
		parser.parseArgument(arguments);

		this.createSocket();
	}

	public UdpIpParameters(InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this.destinationInetSocketAddress = destinationInetSocketAddress;
		this.sourceInetAddress = sourceInetAddress;
		this.bind = bind;
		this.reuse = reuse;
		this.maxPacketSize = maxPacketSize;

		this.createSocket();
	}

	private void createSocket() throws IOException {
		if (this.destinationInetSocketAddress.getAddress().isMulticastAddress()) {
			MulticastSocket multicastSocket = new MulticastSocket((SocketAddress) null);
			if (this.sourceInetAddress != null)
				multicastSocket.setInterface(this.sourceInetAddress);
			multicastSocket.setReuseAddress(true);
			if (this.bind) {
				if (!multicastSocket.isBound()) {
					Logger.log(this, LogLevel.INFO, "Bind on " + this.destinationInetSocketAddress);
					multicastSocket.bind(new InetSocketAddress(this.destinationInetSocketAddress.getPort()));
				} else {
					Logger.log(this, LogLevel.WARN, "Inet socket address" + this.destinationInetSocketAddress + " already bound");
				}
			}
			multicastSocket.joinGroup(this.destinationInetSocketAddress.getAddress());
			this.socket = multicastSocket;
		} else {
			this.socket = new DatagramSocket(null);
			if (this.bind) {
				if (!this.socket.isBound()) {
					Logger.log(this, LogLevel.INFO, "Bind on " + this.destinationInetSocketAddress);
					this.socket.bind(this.destinationInetSocketAddress);
				} else {
					Logger.log(this, LogLevel.WARN, "Inet socket address" + this.destinationInetSocketAddress + " already bound");
				}
			}
		}
		this.socket.setReuseAddress(this.reuse);
	}

	public DatagramSocket getSocket() {
		return this.socket;
	}

	public InetSocketAddress getDestinationInetSocketAddress() {
		return this.destinationInetSocketAddress;
	}

	public int getMaxPacketSize() {
		return this.maxPacketSize;
	}

	public boolean isReuse() {
		return this.reuse;
	}

	public boolean isBind() {
		return this.bind;
	}

	public InetAddress getSourceInetAddress() {
		return this.sourceInetAddress;
	}

	@Override
	public String toString() {
		return this.destinationInetSocketAddress.toString();
	}

}
