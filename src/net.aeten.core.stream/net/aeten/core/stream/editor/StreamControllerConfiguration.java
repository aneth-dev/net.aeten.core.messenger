package net.aeten.core.stream.editor;

import java.io.InputStream;
import java.io.OutputStream;

import net.aeten.core.Configurable;
import net.aeten.core.ConfigurationException;
import net.aeten.core.Factory;
import net.aeten.core.Singleton;
import net.aeten.core.args4j.CommandLineParserHelper;
import net.aeten.core.stream.args4j.InputStreamOptionHandler;
import net.aeten.core.stream.args4j.OutputStreamOptionHandler;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 
 * @author Thomas Pérennou
 */
public class StreamControllerConfiguration {

	@Option(name = "-n", aliases = "--name", required = true)
	private String name;
	@Option(name = "-c", aliases = "--auto-connect", required = false)
	private boolean isAutoConnect = true;
	@Option(name = "-is", aliases = "--input-stream", required = false)
	private InputStream inputStream = null;
	@Option(name = "-os", aliases = "--output-stream", required = false)
	private OutputStream outputStream = null;
	@Option(name = "-se", aliases = "--stream-editor", required = false)
	private StreamEditorStack editorStack;

	public StreamControllerConfiguration(String... arguments) throws CmdLineException {
		CmdLineParser.registerHandler(StreamEditorStack.class, StreamEditorStackOptionHandler.class);
		CmdLineParser.registerHandler(InputStream.class, InputStreamOptionHandler.class);
		CmdLineParser.registerHandler(OutputStream.class, OutputStreamOptionHandler.class);
		CmdLineParser commandLineParser = new CmdLineParser(this);
		commandLineParser.parseArgument(arguments);
	}

	public StreamControllerConfiguration(String name, boolean isAutoConnect, final Class<? extends InputStream> inputStreamClass, final String inputStreamConfiguration, final Class<? extends OutputStream> outputStreamClass, final String outputStreamConfiguration, StreamEditorStack editorStack) throws ConfigurationException {
		super();
		this.name = name;
		this.isAutoConnect = isAutoConnect;
		try {
			if ((inputStreamConfiguration != null) && (inputStreamClass.isAssignableFrom(Configurable.class))) {
				this.inputStream = new Singleton<InputStream>(new Factory<InputStream>() {
					@SuppressWarnings("unchecked")
					@Override
					public <Y extends InputStream> InputStream create() throws Exception {
						InputStream instance = inputStreamClass.newInstance();
						((Configurable<String>) instance).configure(inputStreamConfiguration);
						return instance;
					}

				}).getInstance();
			} else {
				this.inputStream = new Singleton<InputStream>(inputStreamClass).getInstance();
			}
		} catch (Exception exception) {
			throw new ConfigurationException(inputStreamConfiguration, exception);
		}
		try {
			if ((outputStreamConfiguration != null) && (outputStreamClass.isAssignableFrom(Configurable.class))) {
				this.outputStream = new Singleton<OutputStream>(new Factory<OutputStream>() {
					@SuppressWarnings("unchecked")
					@Override
					public <Y extends OutputStream> OutputStream create() throws Exception {
						OutputStream instance = outputStreamClass.newInstance();
						((Configurable<String>) instance).configure(outputStreamConfiguration);
						return instance;
					}

				}).getInstance();
			} else {
				this.outputStream = new Singleton<OutputStream>(outputStreamClass).getInstance();
			}
		} catch (Exception exception) {
			throw new ConfigurationException(outputStreamConfiguration, exception);
		}
		this.editorStack = editorStack;
	}

	public StreamControllerConfiguration(String configuration) throws CmdLineException {
		this(CommandLineParserHelper.splitArguments(configuration));
	}

	public String getName() {
		return name;
	}

	public boolean isAutoConnect() {
		return isAutoConnect;
	}

	public StreamEditorStack getEditorStack() {
		return editorStack;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}
}
