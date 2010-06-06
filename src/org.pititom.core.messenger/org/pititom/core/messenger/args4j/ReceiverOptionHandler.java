package org.pititom.core.messenger.args4j;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.pititom.core.messenger.service.Receiver;

public class ReceiverOptionHandler extends OptionHandler<Receiver<?>> {
	public static final String CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "--configuration" };

	public ReceiverOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Receiver<?>> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String configuration = null;
		try {
			Class<Receiver<?>> recieverClass = (Class<Receiver<?>>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(0));
			Receiver<?> reciever = recieverClass.newInstance();
			if (CONFIGURATION_OPTION_NAME.equals(params.getParameter(1)) || contains(params.getParameter(1), CONFIGURATION_OPTION_ALIASES)) {
				configuration = params.getParameter(2);
				reciever.configure(configuration);
			}
			setter.addValue(reciever);
		} catch (Exception exception) {
			throw new CmdLineException(this.owner, params.getParameter(0), exception);
		}
		return (configuration == null) ? 1 : 3;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "RECEIVER";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list)
			if (element.equals(item))
				return true;
		return false;

	}

}
