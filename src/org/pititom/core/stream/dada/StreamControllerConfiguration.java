package org.pititom.core.stream.dada;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.pititom.core.data.Parameters;
import org.pititom.core.stream.controller.ConfigurationException;
import org.pititom.core.stream.extension.StreamEditor;

public class StreamControllerConfiguration {
	private final String group;
	private final boolean isAutoConnect;
	private final Class<? extends InputStream> inputStreamClass;
	private final Parameters<?> inputStreamParameters;
	private final Class<? extends OutputStream> outputStreamClass;
	private final Parameters<?> outputStreamParameters;
	private final LinkedHashMap<StreamEditor, Parameters<?>> editorStack;

	@SuppressWarnings("unchecked")
    public StreamControllerConfiguration(String group, boolean isAutoConnect,
	        String inputStreamClassName,
	        String inputStreamParameterKeysEnumClassName,
	        String[][] inputStreamParameters, String outputStreamClassName,
	        String outputStreamParameterKeysEnumClassName,
	        String[][] outputStreamParameters, String[] editorStackClassNames,
	        String[] editorStackParameterKeysEnumClassesNames,
	        String[][][] editorStackParameters) throws ConfigurationException {
		try {
			this.group = group;
			this.isAutoConnect = isAutoConnect;
			if (inputStreamClassName != null) {
				this.inputStreamClass = (Class<? extends InputStream>) Class
				        .forName(inputStreamClassName);
				this.inputStreamParameters = new Parameters<Enum<?>>(
				        ((Class<Enum<?>>) Class
				                .forName(inputStreamParameterKeysEnumClassName))
				                .getEnumConstants(), inputStreamParameters);
			} else {
				this.inputStreamClass = null;
				this.inputStreamParameters = null;
			}
			if (outputStreamClassName != null) {
				this.outputStreamClass = (Class<? extends OutputStream>) Class
				        .forName(outputStreamClassName);
				this.outputStreamParameters = new Parameters<Enum<?>>(
				        ((Class<Enum<?>>) Class
				                .forName(outputStreamParameterKeysEnumClassName))
				                .getEnumConstants(), outputStreamParameters);
			} else {
				this.outputStreamClass = null;
				this.outputStreamParameters = null;
			}

			this.editorStack = new LinkedHashMap<StreamEditor, Parameters<?>>();
			for (int i = 0; i < editorStackClassNames.length; i++) {
				StreamEditor streamEditor;
				Class<? extends StreamEditor> editorClass = (Class<? extends StreamEditor>) Class
		        .forName(editorStackClassNames[i]);
				if (editorStackParameters[i].length > 0) {
				Parameters<Enum<?>> parameters = new Parameters<Enum<?>>(
				        ((Class<Enum<?>>) Class
				                .forName(editorStackParameterKeysEnumClassesNames[i]))
				                .getEnumConstants(), editorStackParameters[i]);
					streamEditor = editorClass.getConstructor(Parameters.class)
					        .newInstance(parameters);
					this.editorStack.put(streamEditor, parameters);
				} else {
					streamEditor = editorClass.newInstance();
				}

				this.editorStack.put(streamEditor, null);
			}
		} catch (Exception exception) {
			throw new ConfigurationException(exception);
		}

	}

	public StreamControllerConfiguration(String group, boolean isAutoConnect,
	        Class<? extends InputStream> inputStreamClass,
	        Class<Enum<?>> inputStreamParameterKeysEnumClass,
	        String[][] inputStreamParameters,
	        Class<? extends OutputStream> outputStreamClass,
	        Class<Enum<?>> outputStreamParameterKeysEnumClass,
	        String[][] outputStreamParameters,
	        Class<? extends StreamEditor>[] editorStackClass,
	        Class<Enum<?>>[] editorStackParameterKeysEnumClasses,
	        String[][][] editorStackParameters) throws ConfigurationException {
		try {
			this.group = group;
			this.isAutoConnect = isAutoConnect;
			this.inputStreamClass = inputStreamClass;
			this.inputStreamParameters = new Parameters<Enum<?>>(
			        inputStreamParameterKeysEnumClass.getEnumConstants(),
			        inputStreamParameters);
			this.outputStreamClass = outputStreamClass;
			this.outputStreamParameters = new Parameters<Enum<?>>(
			        outputStreamParameterKeysEnumClass.getEnumConstants(),
			        outputStreamParameters);
			this.editorStack = new LinkedHashMap<StreamEditor, Parameters<?>>();
			for (int i = 0; i < editorStackClass.length; i++) {
				StreamEditor streamEditor;
				Parameters<Enum<?>> parameters = new Parameters<Enum<?>>(
				        editorStackParameterKeysEnumClasses[i]
				                .getEnumConstants(), editorStackParameters[i]);
				if (editorStackParameters[i].length > 0)
					streamEditor = editorStackClass[i].getConstructor(
					        Parameters.class).newInstance(parameters);
				else
					streamEditor = editorStackClass[i].newInstance();

				this.editorStack.put(streamEditor, parameters);
			}
		} catch (Exception exception) {
			throw new ConfigurationException(exception);
		}

	}

	public String getGroup() {
		return group;
	}

	public boolean isAutoConnect() {
		return isAutoConnect;
	}

	public Class<? extends InputStream> getInputStreamClass() {
		return inputStreamClass;
	}

	public Parameters<?> getInputStreamParameters() {
		return inputStreamParameters;
	}

	public Class<? extends OutputStream> getOutputStreamClass() {
		return outputStreamClass;
	}

	public Parameters<?> getOutputStreamParameters() {
		return outputStreamParameters;
	}

	public LinkedHashMap<StreamEditor, Parameters<?>> getEditorStack() {
		return editorStack;
	}

}
