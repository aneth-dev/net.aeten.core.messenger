package net.aeten.core.messenger.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.aeten.core.messenger.MessageDecoder;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Receiver;
import net.aeten.core.net.UdpIpSocketFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider(Receiver.class)
public class UdpIpReceiver<Message> extends Receiver.ReceiverAdapter<Message> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpIpReceiver.class);

	@FieldInit
	private final MessageDecoder<Message> messageDecoder;
	@FieldInit(alias = { "udp ip configuration",
								"UDP/IP configuration"
	})
	private final UdpIpSocketFactory socketFactory;
	private DatagramSocket socket;
	private DatagramPacket packet;

	@SpiConstructor
	public UdpIpReceiver(UdpIpReceiverInit init) {
		this(init.getIdentifier(), init.getMessageDecoder(), init.getSocketFactory());
	}

	public UdpIpReceiver(String identifier, MessageDecoder<Message> messageDecoder, UdpIpSocketFactory socketFactory) {
		super(identifier);
		this.messageDecoder = messageDecoder;
		this.socketFactory = socketFactory;
	}

	public UdpIpReceiver(String identifier, MessageDecoder<Message> messageBuilder, InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(identifier, messageBuilder, new UdpIpSocketFactory(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize));
	}

	@Override
	public synchronized boolean isConnected() {
		return (socket != null) && !socket.isClosed() && socket.isBound();
	}

	@Override
	protected void doConnect() throws IOException {
		try {
			if (socketFactory.getSocket().isClosed()) {
				socket = socketFactory.createSocket();
			} else {
				socket = socketFactory.getSocket();
			}
			if (!socket.isBound()) {
				socket.bind(socketFactory.getDestinationInetSocketAddress());
			}
			packet = new DatagramPacket(new byte[socketFactory.getMaxPacketSize()], socketFactory.getMaxPacketSize());
		} catch (Exception exception) {
			throw new IOException(exception);
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		socketFactory.closeSocket();
		packet = null;
	}

	@Override
	public void receive(MessengerEventData<Message> data) throws IOException {
		socket.receive(packet);
		data.setContact(packet.getAddress().getHostAddress());
		data.setService("" + socketFactory.getDestinationInetSocketAddress().getPort());
		try {
			data.setMessage(this.messageDecoder.decode(packet.getData(), packet.getOffset(), packet.getLength()));
		} catch (Throwable exception) {
			LOGGER.error("Error while decoding packet from " + packet.getSocketAddress(), exception);
			receive(data);
		}
	}

}
