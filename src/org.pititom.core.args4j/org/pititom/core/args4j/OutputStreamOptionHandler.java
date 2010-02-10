package org.pititom.core.args4j;

import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.pititom.core.Configurable;

/**
 * {@link Class} {@link OptionHandler}.
 * 
 * @author Thomas Pérennou
 */
public class OutputStreamOptionHandler extends OptionHandler<OutputStream> {

	public static final String OUTPUT_STREAM_OPTION_NAME = "-os";
	public static final String[] OUTPUT_STREAM_OPTION_ALIASES = { "--output-stream", "--over" };
	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "--configuration" };
	public static final String END_OF_OPTIONS = "--end";

	public OutputStreamOptionHandler(CmdLineParser parser, OptionDef option, Setter<OutputStream> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int i = 0;
		try {
			Class<OutputStream> outputStreamClass = null;
			OutputStream outputStream = null;
			for (;; i++) {
				try {
					if (END_OF_OPTIONS.equals(params.getParameter(i))) {
						++i;
						break;
					}
				} catch (CmdLineException exception) {
				}
				try {
					if (OUTPUT_STREAM_OPTION_NAME.equals(params.getParameter(i)) || contains(params.getParameter(i), OUTPUT_STREAM_OPTION_ALIASES)) {
						++i;
					}
					outputStreamClass = (Class<OutputStream>)Class.forName(params.getParameter(i));
				} catch (ClassCastException exception) {
					throw new CmdLineException(this.owner, exception);
				} catch (CmdLineException exception) {
					break;
				}

				String configuration;

				try {
					if (CONFIGURATION_OPTION_NAME.equals(params.getParameter(i + 1)) || contains(params.getParameter(i + 1), CONFIGURATION_OPTION_ALIASES)) {
						i += 2;
						configuration = params.getParameter(i);
					} else {
						configuration = null;
					}
				} catch (CmdLineException exception) {
					configuration = null;
				}

				if (outputStream == null) {
					outputStream = outputStreamClass.newInstance();
				} else {
					outputStream = outputStreamClass.getConstructor(OutputStream.class).newInstance(outputStream);
				}
				if (outputStream instanceof Configurable) {
					((Configurable)outputStream).configure(configuration);
				}
				
			}
			setter.addValue(outputStream);
		} catch (Exception exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return i;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "OUTPUT_STREAM";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list)
			if (element.equals(item))
				return true;
		return false;

	}
}
