package org.pititom.core.stream;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import org.kohsuke.args4j.CmdLineException;
import org.pititom.core.extersion.Configurable;
import org.pititom.core.extersion.ConfigurationException;

public class UdpIpInputStream extends PipedInputStream implements Configurable {

	private UdpIpParameters parameters;

	private PipedOutputStream pipedOutputStream;
	private Thread receptionThread;

	public UdpIpInputStream() throws IOException {
		this.parameters = null;
		this.pipedOutputStream = new PipedOutputStream();
		this.connect(this.pipedOutputStream);
		this.receptionThread = null;
	}

	public UdpIpInputStream(InetSocketAddress destinationInetSocketAddress,
	        InetAddress sourceInetAddress, boolean reuse, int maxPacketSize)
	        throws IOException {
		this.parameters = new UdpIpParameters(destinationInetSocketAddress,
		        sourceInetAddress, reuse, maxPacketSize);
		this.pipedOutputStream = new PipedOutputStream();
		this.connect(this.pipedOutputStream);
		this.receptionThread = new Thread(new ReceptionThread());
		this.receptionThread.start();
	}

	private class ReceptionThread implements Runnable {
		@Override
		public void run() {
			DatagramPacket packet = new DatagramPacket(
			        new byte[UdpIpInputStream.this.parameters
			                .getMaxPacketSize()],
			        UdpIpInputStream.this.parameters.getMaxPacketSize(),
			        UdpIpInputStream.this.parameters
			                .getDestinationInetSocketAddress().getAddress(),
			        UdpIpInputStream.this.parameters
			                .getDestinationInetSocketAddress().getPort());
			try {
				while ((UdpIpInputStream.this.parameters.getSocket() != null)
				        && !UdpIpInputStream.this.parameters.getSocket()
				                .isClosed()) {
					try {
						UdpIpInputStream.this.parameters.getSocket().receive(
						        packet);
						UdpIpInputStream.this.pipedOutputStream.write(packet
						        .getData(), 0, packet.getLength());
					} catch (SocketTimeoutException exception) {
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
			throw new ConfigurationException(configuration, UdpIpInputStream.class
			        .getCanonicalName()
			        + " is allreaady configured");
		try {
			this.parameters = new UdpIpParameters(configuration);
			this.receptionThread = new Thread(new ReceptionThread());
			this.receptionThread.start();
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

}
