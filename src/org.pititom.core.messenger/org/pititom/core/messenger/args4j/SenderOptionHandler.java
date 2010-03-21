package org.pititom.core.messenger.args4j;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.pititom.core.messenger.service.Sender;

public class SenderOptionHandler extends OptionHandler<Sender<?>> {
	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "--configuration" };

	public SenderOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Sender<?>> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String configuration = null;
		try {
			Class<Sender<?>> senderClass = (Class<Sender<?>>) Class.forName(params.getParameter(0));
			Sender<?> sender = senderClass.newInstance();
			if (CONFIGURATION_OPTION_NAME.equals(params.getParameter(1)) || contains(params.getParameter(1), CONFIGURATION_OPTION_ALIASES)) {
				configuration = params.getParameter(2);
				sender.configure(configuration);
			}
			setter.addValue(sender);
		} catch (Exception exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return (configuration == null) ? 1 : 3;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "SENDER";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list)
			if (element.equals(item))
				return true;
		return false;

	}

}
