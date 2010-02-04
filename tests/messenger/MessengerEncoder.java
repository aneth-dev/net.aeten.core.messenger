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
import org.pititom.core.messenger.MessengerEditor;

public class MessengerEncoder implements MessengerEditor, Configurable {

	private Map<String, Integer> table;

	@Override
	public void edit(DataInputStream in, DataOutputStream out) throws IOException {

		final byte[] className = new byte[in.readInt()];
		in.read(className);
		int length = in.readInt();
		final byte[] data = new byte[length];
		in.read(data);

		// Write header
		out.writeInt(this.table.get(new String(className, MessengerEditor.CLASS_NAME_CHARSET)));
		out.writeLong(System.currentTimeMillis());
		out.writeInt(length);
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

	private final void configure(Reader reader) throws ConfigurationException, IOException {
		Properties properties = new Properties();
		properties.load(reader);
		this.table = new HashMap<String, Integer>(properties.size());
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			table.put(entry.getValue().toString(), Integer.parseInt(entry.getKey().toString()));
		}
	}
}
