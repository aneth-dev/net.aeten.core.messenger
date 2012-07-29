package net.aeten.core.args4j;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Predicate;
import net.aeten.core.spi.Service;
import net.aeten.core.util.StringUtil;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.OptionHandler;

/**
 * {@link Class} {@link CmdLineParser}.
 * 
 * @author Thomas Pérennou
 */
@SuppressWarnings("rawtypes")
public class CommandLineParserHelper {
	static {
		// Load option handlers
		for (@SuppressWarnings("unused")
		OptionHandler<?> handler : Service.getProviders(OptionHandler.class, new Predicate<Class<OptionHandler>>() {
			@Override
			public boolean evaluate(Class<OptionHandler> handler) {
				CmdLineParser.registerHandler(handler.getAnnotation(ValueType.class).value(), handler);
				return false;
			}
		})) {
			// Pass
		}
	}

	private CommandLineParserHelper() {
	}

	public static String[] splitArguments(String arguments) throws IllegalArgumentException {
		return StringUtil.splitWithQuote(arguments);
	}

	public static void configure(final Object bean, final String configuration) throws ConfigurationException {
		try {
			new CmdLineParser(bean).parseArgument(splitArguments(configuration));
		} catch (Throwable exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}
}
