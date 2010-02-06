package org.pititom.core.test.stream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.pititom.core.stream.editor.StreamControllerConnection;
import org.pititom.core.stream.editor.StreamControllerConfiguration;

/**
 *
 * @author Thomas Pérennou
 */
public class Reception {

	public static void main(String[] arguments) throws Exception {
		final StreamControllerConfiguration configuration = new StreamControllerConfiguration(
		        "-n reception -c -is org.pititom.core.stream.UdpIpInputStream -c \"-d 230.2.15.2:5200 -p 64 -r\" -se org.pititom.core.test.stream.Decoder");

		final PipedOutputStream pipedOut = new PipedOutputStream();
		final StreamControllerConnection connection = new StreamControllerConnection(
		        configuration, pipedOut);
		final PipedInputStream pipedIn = new PipedInputStream(pipedOut);
		connection.connect();

		final ObjectInputStream inputStream = new ObjectInputStream(pipedIn);
		while (true) {
			try {
				System.out.println("Object recieved from "
				        + configuration.getName() + ": "
				        + inputStream.readObject());
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (ClassNotFoundException exception) {
				exception.printStackTrace();
			}
		}
	}

}
