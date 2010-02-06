package org.pititom.core.stream.controller;

import java.io.InputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.pititom.core.ContributionFactory;
import org.pititom.core.args4j.CommandLineParser;

public class StreamControllerConfiguration {

	@Option(name = "-n", aliases = "--name", required = true)
	private String name;
	@Option(name = "-c", aliases = "--auto-connect", required = false)
	private boolean isAutoConnect = true;
	@Option(name = "-is", aliases = "--input-stream", required = false)
	private ContributionFactory<? extends InputStream> inputStreamFactory = new ContributionFactory.Null<InputStream>();
	@Option(name = "-os", aliases = "--output-stream", required = false)
	private ContributionFactory<? extends OutputStream> outputStreamFactory = new ContributionFactory.Null<OutputStream>();
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
		this.inputStreamFactory = new ContributionFactory<InputStream>(inputStreamClass, inputStreamConfiguration);
		this.outputStreamFactory = new ContributionFactory<OutputStream>(outputStreamClass, outputStreamConfiguration);
		;
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

	public ContributionFactory<? extends InputStream> getInputStreamFactory() {
		return inputStreamFactory;
	}

	public ContributionFactory<? extends OutputStream> getOutputStreamFactory() {
		return outputStreamFactory;
	}
}
