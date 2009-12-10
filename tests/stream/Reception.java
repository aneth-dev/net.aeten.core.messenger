import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;

import org.pititom.core.extersion.Configurable;
import org.pititom.core.stream.controller.StreamControllerConnection;
import org.pititom.core.stream.dada.StreamControllerConfiguration;
import org.pititom.core.stream.extension.StreamEditor;

public class Reception {

	public static void main(String[] arguments) throws Exception {
		final StreamControllerConfiguration configuration = new StreamControllerConfiguration(
		        "-n reception -c -is org.pititom.core.stream.UdpIpInputStream -isc \"-d 230.2.15.2:5200 -p 64 -r\" -es Decoder");

		InputStream in = configuration.getInputStreamClass().newInstance();
		if (in instanceof Configurable)
			((Configurable) in).configure(configuration
			        .getInputStreamConfiguration());

		PipedOutputStream pipedOut = new PipedOutputStream();

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
		        in, pipedOut, editorStack);

		final PipedInputStream pipedIn = new PipedInputStream(pipedOut);
		connection.connect();

		ObjectInputStream inputStream = new ObjectInputStream(pipedIn);
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
