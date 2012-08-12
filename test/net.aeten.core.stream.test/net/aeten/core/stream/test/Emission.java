package net.aeten.core.stream.test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import net.aeten.core.stream.UdpIpOutputStream;
import net.aeten.core.stream.editor.StreamControllerConnection;

/**
 * 
 * @author Thomas Pérennou
 */
public class Emission {

	public static void main(String[] arguments) throws Exception {
		final PipedInputStream pipedIn = new PipedInputStream();
		final StreamControllerConnection connection = new StreamControllerConnection(pipedIn, new UdpIpOutputStream(new InetSocketAddress("230.2.15.2", 5200), null, true, true, 64), new Encoder());
		final ObjectOutputStream outputStream = new ObjectOutputStream(new PipedOutputStream(pipedIn));

		connection.connect();
		int i = 0;
		while (true) {
			try {
				String object = "command/" + ++i;
				outputStream.writeObject(object);
				System.out.println("Object sent by emission: " + object);
				Thread.sleep(2000);
			} catch (IOException | InterruptedException exception) {
				exception.printStackTrace();
			}
		}

	}

}
