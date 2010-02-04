import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.stream.controller.StreamEditor;

public class MessengerDecoder implements StreamEditor, Configurable {

	private Map<Integer, String> table;

	@Override
	public void edit(DataInputStream in, DataOutputStream out) throws IOException {

		String className = this.table.get(in.readInt());
		in.readLong(); // timestamp
		byte[] data = new byte[in.readInt()];
		in.read(data);

		out.writeInt(className.length());
		out.write(className.getBytes());
		out.writeInt(data.length);
		out.write(data);

	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		try {
			this.configure(new FileReader(new File(new URI(configuration))));
		} catch (FileNotFoundException exception) {
			throw new ConfigurationException(configuration, exception);
		} catch (URISyntaxException uriSyntaxException) {
			try {
				this.configure(new StringReader(configuration));
			} catch (IOException exception) {
				throw new ConfigurationException(configuration, exception);
			}
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

	private void configure(Reader reader) throws ConfigurationException, IOException {
		Properties properties = new Properties();
		properties.load(reader);
		this.table = new HashMap<Integer, String>(properties.size());
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			this.table.put(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
		}
	}
}
