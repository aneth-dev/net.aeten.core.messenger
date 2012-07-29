package net.aeten.core.stream.args4j;

import java.io.OutputStream;

import net.aeten.core.Configurable;
import net.aeten.core.args4j.ValueType;
import net.aeten.core.spi.Provider;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link Class} {@link OptionHandler}.
 * 
 * @author Thomas Pérennou
 */
@Provider(OptionHandler.class)
@ValueType(OutputStream.class)
public class OutputStreamOptionHandler extends OptionHandler<OutputStream> {

	public static final String OUTPUT_STREAM_OPTION_NAME = "-o";
	public static final String[] OUTPUT_STREAM_OPTION_ALIASES = { "--over" };
	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "--configuration" };

	public OutputStreamOptionHandler(CmdLineParser parser, OptionDef option, Setter<OutputStream> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int i = 0;
		try {
			Class<OutputStream> outputStreamClass = (Class<OutputStream>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(i));
			String configuration;
			try {
				if (CONFIGURATION_OPTION_NAME.equals(params.getParameter(i + 1)) || contains(params.getParameter(i + 1), CONFIGURATION_OPTION_ALIASES)) {
					i += 2;
					configuration = params.getParameter(i);
				} else {
					configuration = null;
				}
				i++;
			} catch (CmdLineException exception) {
				configuration = null;
			}

			OutputStream outputStream = outputStreamClass.newInstance();
			if (outputStream instanceof Configurable) {
				((Configurable<String>) outputStream).configure(configuration);
			}

			for (;; i++) {
				try {
					if (OUTPUT_STREAM_OPTION_NAME.equals(params.getParameter(i)) || contains(params.getParameter(i), OUTPUT_STREAM_OPTION_ALIASES)) {
						++i;
					} else if (params.getParameter(i).startsWith("-")) {
						break;
					}
					outputStreamClass = (Class<OutputStream>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(i));
				} catch (ClassCastException exception) {
					throw new CmdLineException(this.owner, exception);
				} catch (CmdLineException exception) {
					break;
				}

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

				outputStream = outputStreamClass.getConstructor(OutputStream.class).newInstance(outputStream);
				if (outputStream instanceof Configurable) {
					((Configurable<String>) outputStream).configure(configuration);
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
		for (String item : list) {
			if (element.equals(item))
				return true;
		}
		return false;

	}
}
