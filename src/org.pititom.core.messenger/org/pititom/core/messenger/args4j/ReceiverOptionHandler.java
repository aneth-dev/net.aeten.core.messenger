package org.pititom.core.messenger.args4j;

import java.util.NoSuchElementException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.pititom.core.Configurable;
import org.pititom.core.messenger.Receiver;
import org.pititom.core.service.Service;

public class ReceiverOptionHandler extends OptionHandler<Receiver<?>> {
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "-c", "--configuration" };

	public ReceiverOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Receiver<?>> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String configuration = null;
		try {
			try {
				setter.addValue(Service.getProvider(Receiver.class, params.getParameter(0)));
				return 1;
			} catch (NoSuchElementException exception) {
				Class<Receiver<?>> recieverClass = (Class<Receiver<?>>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(0));
				Receiver<?> reciever = recieverClass.newInstance();
				if (contains(params.getParameter(1), CONFIGURATION_OPTION_ALIASES)) {
					configuration = params.getParameter(2);
					((Configurable<String>) reciever).configure(configuration);
				}
				setter.addValue(reciever);
			}
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
