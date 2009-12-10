import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;

import org.pititom.core.extersion.Configurable;
import org.pititom.core.stream.controller.StreamControllerConnection;
import org.pititom.core.stream.dada.StreamControllerConfiguration;
import org.pititom.core.stream.extension.StreamEditor;

public class Emission {

	public static void main(String[] arguments) throws Exception {
		final StreamControllerConfiguration configuration = new StreamControllerConfiguration(
		        "-n emission -c -os org.pititom.core.stream.UdpIpOutputStream -osc \"-d 230.2.15.2:5200 -p 64 -r\" -es Encoder");

		OutputStream out = configuration.getOutputStreamClass().newInstance();
		if (out instanceof Configurable)
			((Configurable) out).configure(configuration
			        .getOutputStreamConfiguration());

		PipedInputStream pipedIn = new PipedInputStream();

		StreamEditor[] editorStack = new StreamEditor[configuration
		        .getEditorStack().getConfigurationStackMap().size()];
		int index = 0;
		for (Map.Entry<Class<? extends StreamEditor>, String> streamEditorEntry : configuration
		        .getEditorStack().getConfigurationStackMap().entrySet()) {
			StreamEditor streamEditor = streamEditorEntry.getKey()
			        .newInstance();
			if (streamEditor instanceof Configurable)
				((Configurable) streamEditor).configure(streamEditorEntry
				        .getValue());
			editorStack[index++] = streamEditor;
		}

		StreamControllerConnection connection = new StreamControllerConnection(
		        pipedIn, out, editorStack);

		ObjectOutputStream outputStream = new ObjectOutputStream(
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
