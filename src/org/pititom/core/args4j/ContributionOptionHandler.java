package org.pititom.core.args4j;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.pititom.core.ContributionFactory;

/**
 * {@link Class} {@link OptionHandler}.
 * 
 * @author Thomas Pérennou
 */
public class ContributionOptionHandler extends OptionHandler<ContributionFactory> {

	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = {"--configuration"};

	public ContributionOptionHandler(CmdLineParser parser, OptionDef option,
			Setter<ContributionFactory> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int i = 0;
		String className = params.getParameter(0);
		String configuration;
		try {
			if (CONFIGURATION_OPTION_NAME.equals(params.getParameter(1)) || contains(params.getParameter(1), CONFIGURATION_OPTION_ALIASES)) {
				configuration = params.getParameter(2);
			} else {
				configuration = null;
			}
		} catch (CmdLineException exception) {
			configuration = null;
		}

		try {
			ContributionFactory factory = new ContributionFactory(Class.forName(className), configuration);
			setter.addValue(factory);
		} catch (ClassNotFoundException exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return (configuration == null) ? 1 : 3;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "CONTRIBUTION";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list) {
			if (element.equals(item)) {
				return true;
			}
		}
		return false;

	}
}
