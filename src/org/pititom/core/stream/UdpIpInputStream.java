package org.pititom.core.stream;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import org.pititom.core.data.Parameters;
import org.pititom.core.stream.UdpIpParameters.ParameterKey;
import org.pititom.core.stream.controller.ConfigurationException;

public class UdpIpInputStream extends PipedInputStream {

	private final UdpIpParameters parameters;

	private PipedOutputStream pipedOutputStream;
	private Thread receptionThread;

	public UdpIpInputStream(Parameters<ParameterKey> parameters)
	        throws ConfigurationException, IOException {
		this.parameters = new UdpIpParameters(parameters);
		this.pipedOutputStream = new PipedOutputStream();
		this.connect(this.pipedOutputStream);
		this.receptionThread = new Thread(new ReceptionThread());
		this.receptionThread.start();
	}

	public UdpIpInputStream(InetSocketAddress destinationInetSocketAddress,
	        InetAddress sourceInetAddress, boolean reuse, int maxPacketSize)
	        throws ConfigurationException, IOException {
		this.parameters = new UdpIpParameters(destinationInetSocketAddress, sourceInetAddress, reuse, maxPacketSize);
		this.pipedOutputStream = new PipedOutputStream();
		this.connect(this.pipedOutputStream);
		this.receptionThread = new Thread(new ReceptionThread());
		this.receptionThread.start();
	}

	private class ReceptionThread implements Runnable {
		@Override
		public void run() {
			DatagramPacket packet = new DatagramPacket(
			        new byte[UdpIpInputStream.this.parameters.getMaxPacketSize()],
			        UdpIpInputStream.this.parameters.getMaxPacketSize(),
			        UdpIpInputStream.this.parameters.getDestinationInetSocketAddress().getAddress(),
			        UdpIpInputStream.this.parameters.getDestinationInetSocketAddress().getPort());
			try {
				while ((UdpIpInputStream.this.parameters.getSocket() != null)
				        && !UdpIpInputStream.this.parameters.getSocket().isClosed()) {
					try {
						UdpIpInputStream.this.parameters.getSocket().receive(packet);
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

}
