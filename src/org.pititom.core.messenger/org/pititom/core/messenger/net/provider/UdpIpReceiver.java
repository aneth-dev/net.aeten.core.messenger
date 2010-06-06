package org.pititom.core.messenger.net.provider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.kohsuke.args4j.Option;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Factory;
import org.pititom.core.args4j.UdpIpParameters;
import org.pititom.core.messenger.service.MessageDecoder;
import org.pititom.core.messenger.service.Receiver;

public class UdpIpReceiver<Message> extends Receiver<Message> {
	
	@Option(name = "-d", aliases = "--message-decoder", required = true)
	private Factory<MessageDecoder<Message>>	messageBuilderFactory;
	@Option(name = "-udpip", aliases = "--udp-ip-configuration", required = true)
	private String								udpIpConfiguration;

	private MessageDecoder<Message>				messageBuilder;
	private UdpIpParameters						parameters;
	private DatagramSocket						socket;
	private DatagramPacket						packet;

	/** @deprecated Reserved to configuration building */
	public UdpIpReceiver() {
	}

	public UdpIpReceiver(String identifier, MessageDecoder<Message> messageBuilder, UdpIpParameters parameters) {
		super(identifier);
		this.messageBuilder = messageBuilder;
		this.parameters = parameters;
		this.socket = this.parameters.getSocket();
	}

	public UdpIpReceiver(String identifier, MessageDecoder<Message> messageBuilder, InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(identifier, messageBuilder, new UdpIpParameters(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize));
	}

	@Override
	public boolean isConnected() {
		return this.socket.isBound();
	}

	@Override
	protected void doConnect() throws IOException {
		this.parameters.createSocket();
		this.socket = this.parameters.getSocket();
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.socket.close();
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		super.configure(configuration);
		try {
			this.parameters = new UdpIpParameters(this.udpIpConfiguration);
			this.socket = this.parameters.getSocket();
			this.messageBuilder = this.messageBuilderFactory.getInstance();
			this.messageBuilderFactory = null;
			this.packet = new DatagramPacket(new byte[parameters.getMaxPacketSize()], parameters.getMaxPacketSize(), parameters.getDestinationInetSocketAddress().getAddress(), parameters.getDestinationInetSocketAddress().getPort());
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

	public Message receive() throws IOException {
		this.socket.receive(this.packet);
		try {
			return this.messageBuilder.decode(packet.getData(), packet.getOffset(), packet.getLength());
		} catch (IOException exception) {
			throw exception;
		} catch (Throwable exception) {
			throw new IOException(exception);
		}
	}
}
