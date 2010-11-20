package net.aeten.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.aeten.core.Configurable;
import net.aeten.core.ConfigurationException;
import net.aeten.core.args4j.UdpIpParameters;


/**
 * 
 * @author Thomas Pérennou
 */
public class UdpIpInputStream extends InputStream implements Configurable<String> {

	private UdpIpParameters parameters;

	private Thread receptionThread;
	private final BlockingQueue<DatagramPacket> queue = new LinkedBlockingQueue<DatagramPacket>();
	private int position = 0;
	private int available = 0;
	private DatagramPacket currentPacket = null;

	public UdpIpInputStream() {
		this.parameters = null;
		this.receptionThread = null;
	}

	public UdpIpInputStream(InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean autoBind, boolean reuse, int maxPacketSize) throws IOException {
		this(new UdpIpParameters(destinationInetSocketAddress, sourceInetAddress, autoBind, reuse, maxPacketSize));
	}

	public UdpIpInputStream(UdpIpParameters parameters) {
		this.parameters = parameters;
		this.receptionThread = new Thread(new ReceptionThread(), this.toString());
		this.receptionThread.start();
	}

	private class ReceptionThread implements Runnable {
		@Override
		public void run() {
			try {
				while ((UdpIpInputStream.this.parameters.getSocket() != null) && !UdpIpInputStream.this.parameters.getSocket().isClosed()) {
					DatagramPacket packet = new DatagramPacket(new byte[UdpIpInputStream.this.parameters.getMaxPacketSize()], UdpIpInputStream.this.parameters.getMaxPacketSize(), UdpIpInputStream.this.parameters.getDestinationInetSocketAddress().getAddress(), UdpIpInputStream.this.parameters.getDestinationInetSocketAddress().getPort());
					try {
						UdpIpInputStream.this.parameters.getSocket().receive(packet);
						UdpIpInputStream.this.available += packet.getLength();
						UdpIpInputStream.this.queue.put(packet);
					} catch (SocketTimeoutException exception) {
						continue;
					} catch (InterruptedException exception) {
						continue;
					}
				}
			} catch (IOException exception) {
				if (UdpIpInputStream.this.parameters.getSocket() != null)
					UdpIpInputStream.this.parameters.getSocket().close();
			} finally {
				try {
					UdpIpInputStream.this.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		if (this.parameters != null)
			throw new ConfigurationException(configuration, UdpIpInputStream.class.getCanonicalName() + " is already configured");
		try {
			this.parameters = new UdpIpParameters(configuration);
			this.receptionThread = new Thread(new ReceptionThread(), this.toString());
			this.receptionThread.start();
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

	@Override
	public String toString() {
		return UdpIpInputStream.class.getName() + " (" + this.parameters + ")";
	}

	@Override
	public int read() throws IOException {
		if (this.currentPacket == null) {
			try {
				this.currentPacket = this.queue.take();
				this.position = 0;
			} catch (InterruptedException exception) {
				return this.read();
			}
		}
		if (this.currentPacket.getLength() == this.position) {
			this.currentPacket = null;
			return this.read();
		}
		this.available--;
		return this.currentPacket.getData()[this.position++] & 0xFF;
	}

	@Override
	public int available() throws IOException {
		return this.available;
	}
	
	@Override
	public void close() throws IOException {
		this.receptionThread.interrupt();
	}
	
	@Override
    public synchronized void reset() throws IOException {
		this.currentPacket = null;
		// TODO: reset on mark if >= 0
    }

	@Override
	public synchronized void mark(int readlimit) {
		this.position = readlimit;
		// TODO: make a mark field instead of position use
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}

}
