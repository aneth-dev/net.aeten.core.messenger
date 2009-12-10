package org.pititom.core.stream.dada;

import java.io.InputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.pititom.core.args4j.ClassOptionHandler;
import org.pititom.core.args4j.CommandLineParser;

public class StreamControllerConfiguration {

	@Option(name = "-n")
	private String name;
	@Option(name = "-c")
	private boolean isAutoConnect;
	@Option(name = "-is")
	private Class<? extends InputStream> inputStreamClass;
	@Option(name = "-isc")
	private String inputStreamConfiguration;
	@Option(name = "-os")
	private Class<? extends OutputStream> outputStreamClass;
	@Option(name = "-osc")
	private String outputStreamConfiguration;
	@Option(name = "-es")
	private EditorStack editorStack;

	public StreamControllerConfiguration(String... arguments) throws CmdLineException {
		CommandLineParser.registerHandler(EditorStack.class, EditorStackOptionHandler.class);
		CommandLineParser commandLineParser = new CommandLineParser(this);
		commandLineParser.parseArgument(arguments);
	}

	public StreamControllerConfiguration(String name, boolean isAutoConnect,
            Class<? extends InputStream> inputStreamClass,
            String inputStreamConfiguration,
            Class<? extends OutputStream> outputStreamClass,
            String outputStreamConfiguration, EditorStack editorStack) {
	    super();
	    this.name = name;
	    this.isAutoConnect = isAutoConnect;
	    this.inputStreamClass = inputStreamClass;
	    this.inputStreamConfiguration = inputStreamConfiguration;
	    this.outputStreamClass = outputStreamClass;
	    this.outputStreamConfiguration = outputStreamConfiguration;
	    this.editorStack = editorStack;
    }

	public StreamControllerConfiguration(String configuration) throws CmdLineException {
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

	public EditorStack getEditorStack() {
		return editorStack;
	}

}
