package net.aeten.core.messenger.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import net.aeten.core.messenger.MessageEncoder;
import net.aeten.core.messenger.MessageEncoder.EncodingException;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Sender;
import net.aeten.core.net.UdpIpSocketFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider(Sender.class)
public class UdpIpSender<Message> extends Sender.SenderAdapter<Message> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpIpSender.class);

	@FieldInit
	private final MessageEncoder<Message> messageEncoder;
	@FieldInit(alias = { "udp ip configuration",
								"UDP/IP configuration"
	})
	private final UdpIpSocketFactory socketFactory;
	private static ConcurrentHashMap<String, InetAddress> CACHE = new ConcurrentHashMap<>();
	private DatagramSocket socket;

	public UdpIpSender(@SpiInitializer UdpIpSenderInit init) {
		this(init.getIdentifier(), init.getMessageEncoder(), init.getSocketFactory());
	}

	public UdpIpSender(String identifier, MessageEncoder<Message> messageEncoder, UdpIpSocketFactory socketFactory) {
		super(identifier);
		this.messageEncoder = messageEncoder;
		this.socketFactory = socketFactory;
		socket = this.socketFactory.getSocket();
	}

	public UdpIpSender(String identifier, MessageEncoder<Message> messageEncoder, InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(identifier, messageEncoder, new UdpIpSocketFactory(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize));
	}

	@Override
	public boolean isConnected() {
		return (socket != null) && !socket.isClosed();
	}

	@Override
	protected void doConnect() throws IOException {
		if (socketFactory.getSocket().isClosed()) {
			socket = socketFactory.createSocket();
		} else {
			socket = socketFactory.getSocket();
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		socket.close();
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
			InetSocketAddress inetSocketAddress = this.socketFactory.getDestinationInetSocketAddress();
			inetAddress = inetSocketAddress.getAddress();
			port = inetSocketAddress.getPort();
			data.setContact(inetAddress.getHostAddress());
			data.setService("" + port);
		}

		try {
			byte[] buffer = this.messageEncoder.encode(message);
			DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length, inetAddress, port);
			socket.send(packet);
		} catch (SocketException exception) {
			LOGGER.error("Error while creating datagram packet for message " + message, exception);
		} catch (EncodingException exception) {
			LOGGER.error("Error while encoding message " + message, exception);
		}
	}
}
