package org.pititom.core.test.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.pititom.core.stream.controller.StreamControllerConnection;
import org.pititom.core.stream.controller.StreamControllerConfiguration;

/**
 *
 * @author Thomas Pérennou
 */
public class Emission {

	public static void main(String[] arguments) throws Exception {
		final StreamControllerConfiguration configuration = new StreamControllerConfiguration(
		        "-n emission -c -os org.pititom.core.stream.UdpIpOutputStream -c \"-d 230.2.15.2:5200 -p 64 -r\" -se Encoder");

		final PipedInputStream pipedIn = new PipedInputStream();
		final StreamControllerConnection connection = new StreamControllerConnection(
		        configuration, pipedIn);
		final ObjectOutputStream outputStream = new ObjectOutputStream(
		        new PipedOutputStream(pipedIn));

		connection.connect();
		int i = 0;
		while (true) {
			try {
				String object = new String("command/" + ++i);
				outputStream.writeObject(object);
				System.out.println("Object sent by " + configuration.getName()
				        + ": " + object);
				Thread.sleep(2000);
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}

	}

}
