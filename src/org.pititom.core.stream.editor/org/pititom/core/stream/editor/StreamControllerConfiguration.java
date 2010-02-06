package org.pititom.core.stream.editor;

import java.io.InputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.pititom.core.Factory;
import org.pititom.core.args4j.CommandLineParser;

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
	private Factory<? extends InputStream> inputStreamFactory = new Factory.Null<InputStream>();
	@Option(name = "-os", aliases = "--output-stream", required = false)
	private Factory<? extends OutputStream> outputStreamFactory = new Factory.Null<OutputStream>();
	@Option(name = "-se", aliases = "--stream-editor", required = true)
	private StreamEditorStack editorStack;

	public StreamControllerConfiguration(String... arguments)
			throws CmdLineException {
		CommandLineParser.registerHandler(StreamEditorStack.class,
				StreamEditorStackOptionHandler.class);
		CommandLineParser commandLineParser = new CommandLineParser(this);
		commandLineParser.parseArgument(arguments);
	}

	public StreamControllerConfiguration(String name, boolean isAutoConnect,
			Class<? extends InputStream> inputStreamClass,
			String inputStreamConfiguration,
			Class<? extends OutputStream> outputStreamClass,
			String outputStreamConfiguration, StreamEditorStack editorStack) {
		super();
		this.name = name;
		this.isAutoConnect = isAutoConnect;
		this.inputStreamFactory = new Factory<InputStream>(inputStreamClass, inputStreamConfiguration);
		this.outputStreamFactory = new Factory<OutputStream>(outputStreamClass, outputStreamConfiguration);
		this.editorStack = editorStack;
	}

	public StreamControllerConfiguration(String configuration)
			throws CmdLineException {
		this(CommandLineParser.splitArguments(configuration));
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

	public Factory<? extends InputStream> getInputStreamFactory() {
		return inputStreamFactory;
	}

	public Factory<? extends OutputStream> getOutputStreamFactory() {
		return outputStreamFactory;
	}
}
