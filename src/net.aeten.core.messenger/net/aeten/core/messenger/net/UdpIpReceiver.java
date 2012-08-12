package net.aeten.core.messenger.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.MessageDecoder;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Receiver;
import net.aeten.core.net.UdpIpSocketFactory;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;

@Provider(Receiver.class)
public class UdpIpReceiver<Message> extends Receiver.ReceiverAdapter<Message> {

	@FieldInit
	private final MessageDecoder<Message> messageDecoder;
	@FieldInit(alias = {"udp ip configuration", "UDP/IP configuration"})
	private final UdpIpSocketFactory socketFactory;
	private DatagramSocket socket;
	private DatagramPacket packet;

	public UdpIpReceiver(@SpiInitializer UdpIpReceiverInit init) {
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
	public boolean isConnected() {
		return (this.socket != null) && !this.socket.isClosed() && this.socket.isBound();
	}

	@Override
	protected void doConnect() throws IOException {
		try {
			if (this.socketFactory.getSocket().isClosed()) {
				this.socket = this.socketFactory.createSocket();
			} else {
				this.socket = this.socketFactory.getSocket();
			}
			if (!this.socket.isBound()) {
				this.socket.bind(this.socketFactory.getDestinationInetSocketAddress());
			}
			this.packet = new DatagramPacket(new byte[socketFactory.getMaxPacketSize()], socketFactory.getMaxPacketSize());
		} catch (Exception exception) {
			throw new IOException(exception);
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.socketFactory.closeSocket();
		this.packet = null;
	}

	@Override
	public void receive(MessengerEventData<Message> data) throws IOException {
		this.socket.receive(this.packet);
		data.setContact(this.packet.getAddress().getHostAddress());
		data.setService("" + this.socketFactory.getDestinationInetSocketAddress().getPort());
		try {
			data.setMessage(this.messageDecoder.decode(this.packet.getData(), this.packet.getOffset(), this.packet.getLength()));
		} catch (Throwable exception) {
			Logger.log(this, LogLevel.ERROR, "Error while decoding packet from " + packet.getSocketAddress(), exception);
			this.receive(data);
		}
	}

}
