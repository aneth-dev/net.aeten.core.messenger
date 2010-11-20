package net.aeten.core.messenger.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.aeten.core.Format;
import net.aeten.core.Singleton;
import net.aeten.core.args4j.UdpIpParameters;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.MessageDecoder;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Receiver;
import net.aeten.core.service.Provider;

import org.kohsuke.args4j.Option;

@Provider(Receiver.class)
@Format("args")
public class UdpIpReceiver<Message> extends Receiver.Helper<Message> {

	@Option(name = "-d", aliases = "--message-decoder", required = true)
	private Singleton<MessageDecoder<Message>> messageBuilderFactory;
	@Option(name = "-udpip", aliases = "--udp-ip-configuration", required = true)
	private String udpIpConfiguration;

	private MessageDecoder<Message> messageBuilder;
	private UdpIpParameters parameters;
	private DatagramSocket socket;
	private DatagramPacket packet;

	/** @deprecated Reserved to configuration building */
	@Deprecated
    public UdpIpReceiver() {}

	public UdpIpReceiver(String identifier, MessageDecoder<Message> messageBuilder, UdpIpParameters parameters) {
		super(identifier);
		this.messageBuilder = messageBuilder;
		this.parameters = parameters;
	}

	public UdpIpReceiver(String identifier, MessageDecoder<Message> messageBuilder, InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(identifier, messageBuilder, new UdpIpParameters(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize));
	}

	@Override
	public boolean isConnected() {
		return (this.socket != null) && !this.socket.isClosed() && this.socket.isBound();
	}

	@Override
	protected void doConnect() throws IOException {
		try {
			if (this.udpIpConfiguration != null) {
				this.parameters = new UdpIpParameters(this.udpIpConfiguration);
			}
			if (this.parameters.getSocket().isClosed()) {
				this.socket = this.parameters.createSocket();
			} else {
				this.socket = this.parameters.getSocket();
			}
			if (!this.socket.isBound()) {
				this.socket.bind(this.parameters.getDestinationInetSocketAddress());
			}
			if (this.messageBuilderFactory != null) {
				this.messageBuilder = this.messageBuilderFactory.getInstance();
				this.messageBuilderFactory = null;
			}
			this.packet = new DatagramPacket(new byte[parameters.getMaxPacketSize()], parameters.getMaxPacketSize());

		} catch (Exception exception) {
			if (this.configuration == null) {
				throw new IOException(exception);
			}
			throw new IOException("Configuration: " + configuration, exception);
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.parameters.closeSocket();
		this.packet = null;
	}

	@Override
	public void receive(MessengerEventData<Message> data) throws IOException {
		this.socket.receive(this.packet);
		data.setContact(this.packet.getAddress().getHostAddress());
		data.setService("" + this.parameters.getDestinationInetSocketAddress().getPort());
		try {
			data.setMessage(this.messageBuilder.decode(this.packet.getData(), this.packet.getOffset(), this.packet.getLength()));
		} catch (Throwable exception) {
			Logger.log(this, LogLevel.ERROR, "Error while decoding packet from " + packet.getSocketAddress(), exception);
			this.receive(data);
		}
	}

}
