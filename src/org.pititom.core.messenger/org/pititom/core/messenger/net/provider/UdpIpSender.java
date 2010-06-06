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
import org.pititom.core.messenger.service.MessageEncoder;
import org.pititom.core.messenger.service.Sender;

public class UdpIpSender<Message> extends Sender<Message> {
	
	@Option(name = "-e", aliases = "--message-encoder", required = true)
	private Factory<MessageEncoder<Message>>	messageEncoderFactory;
	@Option(name = "-udpip", aliases = "--udp-ip-configuration", required = true)
	private String								udpIpConfiguration;

	private MessageEncoder<Message>				messageEncoder;
	private UdpIpParameters						parameters;
	private DatagramSocket						socket;

	/** @deprecated Reserved to configuration building */
	public UdpIpSender() {
	}

	public UdpIpSender(String identifier, MessageEncoder<Message> messageEncoder, UdpIpParameters parameters) {
		super(identifier);
		this.messageEncoder = messageEncoder;
		this.parameters = parameters;
		this.socket = this.parameters.getSocket();
	}

	public UdpIpSender(String identifier, MessageEncoder<Message> messageEncoder, InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(identifier, messageEncoder, new UdpIpParameters(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize));
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
			this.messageEncoder = this.messageEncoderFactory.getInstance();
			this.messageEncoderFactory = null;
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

	public void send(Message message) throws IOException {
		byte[] data = this.messageEncoder.encode(message);
		this.socket.send(new DatagramPacket(data, data.length, this.parameters.getDestinationInetSocketAddress()));
	}
}
