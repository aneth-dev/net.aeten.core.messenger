package net.aeten.core.args4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Singleton;
import net.aeten.core.util.StringUtil;

import org.kohsuke.args4j.CmdLineParser;

/**
 * {@link Class} {@link CmdLineParser}.
 * 
 * @author Thomas Pérennou
 */
public class CommandLineParserHelper {
	static {
		CmdLineParser.registerHandler(Class.class, ClassOptionHandler.class);
		CmdLineParser.registerHandler(InetAddress.class, InetAddressOptionHandler.class);
		CmdLineParser.registerHandler(InetSocketAddress.class, InetSocketAddressOptionHandler.class);
		CmdLineParser.registerHandler(Singleton.class, FactoryOptionHandler.class);
	}

	private CommandLineParserHelper() {}

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
