package org.pititom.core.messenger.args4j;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.pititom.core.Configurable;
import org.pititom.core.messenger.Sender;

public class SenderOptionHandler extends OptionHandler<Sender<?>> {
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "-c", "--configuration" };

	public SenderOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Sender<?>> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String configuration = null;
		try {
			Class<Sender<?>> senderClass = (Class<Sender<?>>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(0));
			Sender<?> sender = senderClass.newInstance();
			if (contains(params.getParameter(1), CONFIGURATION_OPTION_ALIASES)) {
				configuration = params.getParameter(2);
				((Configurable<String>) sender).configure(configuration);
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
