package net.aeten.core.args4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Lazy;
import net.aeten.core.spi.Service;
import net.aeten.core.util.StringUtil;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.OptionHandler;

/**
 * {@link Class} {@link CmdLineParser}.
 * 
 * @author Thomas Pérennou
 */
public class CommandLineParserHelper {
	static {
		for (OptionHandler<?> handler : Service.getProviders(OptionHandler.class, CommandLineParserHelper.class.getClassLoader())) {
			System.err.println(handler.getClass() + " " + handler.getClass().getAnnotation(ValueType.class).value());
			// CmdLineParser.registerHandler(, handler.getClass());
		}
		CmdLineParser.registerHandler(Class.class, ClassOptionHandler.class);
		CmdLineParser.registerHandler(InetAddress.class, InetAddressOptionHandler.class);
		CmdLineParser.registerHandler(InetSocketAddress.class, InetSocketAddressOptionHandler.class);
		CmdLineParser.registerHandler(Lazy.class, LazyOptionHandler.class);
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
