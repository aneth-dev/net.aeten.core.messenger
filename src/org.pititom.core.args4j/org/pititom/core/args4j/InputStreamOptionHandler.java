package org.pititom.core.args4j;

import java.io.InputStream;

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
public class InputStreamOptionHandler extends OptionHandler<InputStream> {

	public static final String INPUT_STREAM_OPTION_NAME = "-is";
	public static final String[] INPUT_STREAM_OPTION_ALIASES = { "--input-stream", "--over" };
	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "--configuration" };
	public static final String END_OF_OPTIONS = "--end";

	public InputStreamOptionHandler(CmdLineParser parser, OptionDef option, Setter<InputStream> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int i = 0;
		try {
			Class<InputStream> inputStreamClass = null;
			InputStream inputStream = null;
			for (;; i++) {
				try {
					if (END_OF_OPTIONS.equals(params.getParameter(i))) {
						++i;
						break;
					}
				} catch (CmdLineException exception) {
				}
				try {
					if (INPUT_STREAM_OPTION_NAME.equals(params.getParameter(i)) || contains(params.getParameter(i), INPUT_STREAM_OPTION_ALIASES)) {
						++i;
					}
					inputStreamClass = (Class<InputStream>)Class.forName(params.getParameter(i));
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

				if (inputStream == null) {
					inputStream = inputStreamClass.newInstance();
				} else {
					inputStream = inputStreamClass.getConstructor(InputStream.class).newInstance(inputStream);
				}
				if (inputStream instanceof Configurable) {
					((Configurable)inputStream).configure(configuration);
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
		return "EDITOR_STACK";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list)
			if (element.equals(item))
				return true;
		return false;

	}
}
