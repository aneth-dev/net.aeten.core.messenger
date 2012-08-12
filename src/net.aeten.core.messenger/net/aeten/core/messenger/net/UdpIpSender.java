package net.aeten.core.messenger.net;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.MessageEncoder;
import net.aeten.core.messenger.MessageEncoder.EncodingException;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Sender;
import net.aeten.core.net.UdpIpSocketFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;

@Provider(Sender.class)
public class UdpIpSender<Message> extends Sender.SenderAdapter<Message> {

	@FieldInit
	private final MessageEncoder<Message> messageEncoder;
	@FieldInit(alias = {"udp ip configuration", "UDP/IP configuration"})
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
		this.socket = this.socketFactory.getSocket();
	}

	public UdpIpSender(String identifier, MessageEncoder<Message> messageEncoder, InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(identifier, messageEncoder, new UdpIpSocketFactory(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize));
	}

	@Override
	public boolean isConnected() {
		return (this.socket != null) && !this.socket.isClosed();
	}

	@Override
	protected void doConnect() throws IOException {
		if (this.socketFactory.getSocket().isClosed()) {
			this.socket = this.socketFactory.createSocket();
		} else {
			this.socket = this.socketFactory.getSocket();
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
			InetSocketAddress inetSocketAddress = this.socketFactory.getDestinationInetSocketAddress();
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
