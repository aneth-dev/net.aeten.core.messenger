package org.pititom.core.args4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
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

	/** @see {@link MulticastSocket#setTrafficClass(int)} */
	@Option(name = "-c", aliases = { "--traffic-class" }, required = false)
	private String trafficClassOption = "0";
	private int trafficClass = 0;

	/** @see {@link DatagramSocket#setTimeout(int)} */
	@Option(name = "-t", aliases = { "--time-out" }, required = false)
	private int timeout = -1;

	/** @see {@link MulticastSocket#setTimeToLive(int)} */
	@Option(name = "-ttl", aliases = { "--time-to-live" }, required = false)
	private int timeToLive = -1;

	private DatagramSocket socket;

	public UdpIpParameters(String configuration) throws CmdLineException, IOException {
		this(CommandLineParserHelper.splitArguments(configuration));
	}

	public UdpIpParameters(String... arguments) throws CmdLineException, IOException {
		CmdLineParser parser = new CmdLineParser(this);
		parser.parseArgument(arguments);

		this.trafficClass = Integer.parseInt(this.trafficClassOption, 2);
		this.createSocket();
	}

	public UdpIpParameters(InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize, 0, -1, -1);
	}

	public UdpIpParameters(InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize, int trafficClass, int timeout, int timeToLive) throws IOException {
		this.destinationInetSocketAddress = destinationInetSocketAddress;
		this.sourceInetAddress = sourceInetAddress;
		this.bind = bind;
		this.reuse = reuse;
		this.maxPacketSize = maxPacketSize;
		this.trafficClass = trafficClass;
		this.timeout = timeout;
		this.timeToLive = timeToLive;

		this.createSocket();
	}

	public DatagramSocket createSocket() throws IOException {
		if ((this.socket != null) && !this.socket.isClosed()) {
			throw new IOException("Socket not closed");
		}
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
				multicastSocket.joinGroup(this.destinationInetSocketAddress.getAddress());
			}
			if (this.timeToLive != -1) {
				multicastSocket.setTimeToLive(this.timeToLive);
			}
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
		this.socket.setTrafficClass(this.trafficClass);
		if (this.timeout != -1) {
			this.socket.setSoTimeout(this.timeout);
		}
		this.socket.setReuseAddress(this.reuse);
		return this.socket;
	}

	public DatagramSocket getSocket() {
		return this.socket;
	}

	public void closeSocket() {
		if (this.socket instanceof MulticastSocket) {
			try {
				((MulticastSocket) this.socket).leaveGroup(this.destinationInetSocketAddress.getAddress());
			} catch (IOException exception) {
				Logger.log(this, LogLevel.ERROR, "Unable to leave multicast group " + this.destinationInetSocketAddress.getAddress(), exception);
			}
		}
		this.socket.close();
	}

	public InetSocketAddress getDestinationInetSocketAddress() {
		return this.destinationInetSocketAddress;
	}

	public int getMaxPacketSize() {
		return this.maxPacketSize;
	}

	public int getTrafficClass() {
		return this.trafficClass;
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
