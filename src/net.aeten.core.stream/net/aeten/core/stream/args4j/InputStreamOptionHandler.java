package net.aeten.core.stream.args4j;

import java.io.InputStream;

import net.aeten.core.Configurable;

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
public class InputStreamOptionHandler extends OptionHandler<InputStream> {

	public static final String INPUT_STREAM_OPTION_NAME = "-o";
	public static final String[] INPUT_STREAM_OPTION_ALIASES = { "--over" };
	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "--configuration" };

	public InputStreamOptionHandler(CmdLineParser parser, OptionDef option, Setter<InputStream> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int i = 0;
		try {
			Class<InputStream> inputStreamClass = (Class<InputStream>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(i));
			
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

			InputStream inputStream = inputStreamClass.newInstance();
			if (inputStream instanceof Configurable) {
				((Configurable<String>) inputStream).configure(configuration);
			}

			for (;; i++) {
				try {
					if (INPUT_STREAM_OPTION_NAME.equals(params.getParameter(i)) || contains(params.getParameter(i), INPUT_STREAM_OPTION_ALIASES)) {
						++i;
					} else if (params.getParameter(i).startsWith("-")) {
						break;
					}
					inputStreamClass = (Class<InputStream>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(i));
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

				inputStream = inputStreamClass.getConstructor(InputStream.class).newInstance(inputStream);
				if (inputStream instanceof Configurable) {
					((Configurable<String>) inputStream).configure(configuration);
				}

			}
			setter.addValue(inputStream);
		} catch (Exception exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return i;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "INPUT_STREAM";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list)
			if (element.equals(item))
				return true;
		return false;

	}
}
