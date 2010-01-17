package org.pititom.core.stream.dada;

import java.io.InputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.pititom.core.args4j.CommandLineParser;

public class StreamControllerConfiguration {

	@Option(name = "-n", aliases = "--name", required = true)
	private String name;

	@Option(name = "-c", aliases = "--auto-connect", required = false)
	private boolean isAutoConnect = true;

	@Option(name = "-is", aliases = "--input-stream", required = false)
	private Class<? extends InputStream> inputStreamClass;

	@Option(name = "-isc", aliases = "--input-stream-configuration", required = false)
	private String inputStreamConfiguration;

	@Option(name = "-os", aliases = "--output-stream", required = false)
	private Class<? extends OutputStream> outputStreamClass;

	@Option(name = "-osc", aliases = "--output-stream-configuration", required = false)
	private String outputStreamConfiguration;

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
		this.inputStreamClass = inputStreamClass;
		this.inputStreamConfiguration = inputStreamConfiguration;
		this.outputStreamClass = outputStreamClass;
		this.outputStreamConfiguration = outputStreamConfiguration;
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

	public Class<? extends InputStream> getInputStreamClass() {
		return inputStreamClass;
	}

	public String getInputStreamConfiguration() {
		return inputStreamConfiguration;
	}

	public Class<? extends OutputStream> getOutputStreamClass() {
		return outputStreamClass;
	}

	public String getOutputStreamConfiguration() {
		return outputStreamConfiguration;
	}

	public StreamEditorStack getEditorStack() {
		return editorStack;
	}

}
