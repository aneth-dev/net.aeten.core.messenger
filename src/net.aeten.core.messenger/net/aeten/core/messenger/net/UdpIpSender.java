package net.aeten.core.messenger.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import net.aeten.core.Format;
import net.aeten.core.Lazy;
import net.aeten.core.args4j.UdpIpParameters;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.MessageEncoder;
import net.aeten.core.messenger.MessageEncoder.EncodingException;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Sender;
import net.aeten.core.spi.Provider;

import org.kohsuke.args4j.Option;

@Provider(Sender.class)
@Format("args")
public class UdpIpSender<Message> extends Sender.SenderAdapter<Message> {
	@Option(name = "-e", aliases = "--message-encoder", required = true)
	private Lazy<MessageEncoder<Message>, ?> messageEncoderFactory;
	@Option(name = "-udpip", aliases = "--udp-ip-configuration", required = true)
	private String udpIpConfiguration;

	private static ConcurrentHashMap<String, InetAddress> CACHE = new ConcurrentHashMap<>();

	private MessageEncoder<Message> messageEncoder;
	private UdpIpParameters parameters;
	private DatagramSocket socket;

	/** @deprecated Reserved to configuration building */
	@Deprecated
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
		return (this.socket != null) && !this.socket.isClosed();
	}

	@Override
	protected void doConnect() throws IOException {
		try {
			this.parameters = new UdpIpParameters(this.udpIpConfiguration);
			this.socket = this.parameters.getSocket();
			this.messageEncoder = this.messageEncoderFactory.instance();
		} catch (Exception exception) {
			throw new IOException("Configuration: " + configuration, exception);
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.socket.close();
	}

	@Override
	public void send(MessengerEventData<Message> data) throws IOException {
		Message message = data.getMessage();
		String contact = data.getContact();
		String service = data.getService();
		InetAddress inetAddress;
		int port;
		if ((contact != null) && (service != null)) {
			String key = contact + ":" + service;
			inetAddress = CACHE.get(contact);
			if (inetAddress == null) {
				inetAddress = InetAddress.getByName(contact);
				CACHE.putIfAbsent(key, inetAddress);
			}
			port = Integer.parseInt(service);
		} else {
			InetSocketAddress inetSocketAddress = this.parameters.getDestinationInetSocketAddress();
			inetAddress = inetSocketAddress.getAddress();
			port = inetSocketAddress.getPort();
			data.setContact(inetAddress.getHostAddress());
			data.setService("" + port);
		}

		try {
			byte[] buffer = this.messageEncoder.encode(message);
			DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length, inetAddress, port);
			this.socket.send(packet);
		} catch (SocketException exception) {
			Logger.log(this, LogLevel.ERROR, "Error while creating datagram packet for message " + message, exception);
		} catch (EncodingException exception) {
			Logger.log(this, LogLevel.ERROR, "Error while encoding message " + message, exception);
		}
	}

}
