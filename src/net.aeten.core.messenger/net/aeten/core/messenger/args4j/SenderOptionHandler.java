package net.aeten.core.messenger.args4j;

import java.util.NoSuchElementException;

import net.aeten.core.Configurable;
import net.aeten.core.args4j.ValueType;
import net.aeten.core.messenger.Sender;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.Service;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

@Provider(OptionHandler.class)
@ValueType(Sender.class)
public class SenderOptionHandler extends OptionHandler<Sender<?>> {
	public static final String[] CONFIGURATION_OPTION_ALIASES = { "-c", "--configuration" };
	public static final String[] CLASS_OPTION_ALIASES = { "--class" };

	public SenderOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Sender<?>> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String configuration = null;
		boolean hasConfigurationTagOption = false;
		try {
			try {
				setter.addValue((Sender<?>)Service.getProvider(Sender.class, params.getParameter(0)));
				return 1;
			} catch (NoSuchElementException exception) {
				Class<Sender<?>> senderClass = null;

				senderClass = (Class<Sender<?>>) Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(0));
				Sender<?> sender = senderClass.newInstance();

				if (sender instanceof Configurable) {
					if (contains(params.getParameter(1), CONFIGURATION_OPTION_ALIASES)) {
						configuration = params.getParameter(2);
						((Configurable<String>) sender).configure(configuration);
						hasConfigurationTagOption = true;
					} else {
						configuration = params.getParameter(1);
						((Configurable<String>) sender).configure(configuration);
					}
				}
				setter.addValue(sender);
			}
		} catch (Exception exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return (configuration == null) ? 1 : hasConfigurationTagOption ? 3 : 2;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "SENDER";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list) {
			if (element.equals(item))
				return true;
		}
		return false;

	}

}
