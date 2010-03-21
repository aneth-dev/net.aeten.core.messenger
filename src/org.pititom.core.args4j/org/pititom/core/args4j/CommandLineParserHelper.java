package org.pititom.core.args4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Factory;

/**
 * {@link Class} {@link CmdLineParser}.
 *
 * @author Thomas Pérennou
 */
public class CommandLineParserHelper {
	static {
		CmdLineParser.registerHandler(Class.class, ClassOptionHandler.class);
		CmdLineParser.registerHandler(InetAddress.class, InetAddressOptionHandler.class);
		CmdLineParser.registerHandler(InetSocketAddress.class,
		        InetSocketAddressOptionHandler.class);
		CmdLineParser.registerHandler(Factory.class,
		        FactoryOptionHandler.class);
		CmdLineParser.registerHandler(OutputStream.class,
				OutputStreamOptionHandler.class);
		CmdLineParser.registerHandler(InputStream.class,
				InputStreamOptionHandler.class);
	}
	
	private CommandLineParserHelper() {}

	public static String[] splitArguments(String arguments)
	        throws IllegalArgumentException {
		if (arguments == null) {
			return new String[] {};
		}
		
		
		final class SplittedArguments extends LinkedList<String> {
			private static final long serialVersionUID = 5477163044056835635L;

			public void addAll(String[] elements) {
				for (String element : elements)
					this.add(element);
			}
		}
		final SplittedArguments splittedArguments = new SplittedArguments();

		int begin;
		int end;
		for (int i = 0; i < arguments.length(); i++) {
			begin = findNextQuote(arguments, i);
			if (begin > i)
				splittedArguments.addAll(new String(arguments.getBytes(), i,
				        begin - i).trim().split("\\s+"));
			if (begin == -1) {
				splittedArguments.addAll(new String(arguments.getBytes(), i,
				        arguments.length() - i).trim().split("\\s+"));
				break;
			}
			if (i == begin)
				continue;
			i = begin;

			end = findNextQuote(arguments, ++i);
			if (end > i) {
				String quoted = new String(arguments.getBytes(), i, end - i);
				quoted = quoted.replaceAll("\\\\\"", "\"");
				splittedArguments.add(quoted);
			}
			if (end == -1)
				throw new IllegalArgumentException("Unclosed quote at char "
				        + begin);
			i = end;
		}
		String[] result = new String[splittedArguments.size()];
		return splittedArguments.toArray(result);
	}

	private static int findNextQuote(String arguments, int begin) {
		boolean isEscaped = false;
		for (int i = begin; i < arguments.length(); i++) {
			if (!isEscaped && (arguments.charAt(i) == '\"')) {
				return i;
			}
			isEscaped = (arguments.charAt(i) == '\\') ? !isEscaped : false;
		}
		return -1;

	}

	public static void main(String[] arguments) throws CmdLineException {
		doAssertion(splitArguments("-a toto -b titi"), new String[] { "-a",
		        "toto", "-b", "titi" });
		doAssertion(
		        splitArguments("-a toto -b titi -c \"tutu titi\\\"\" -d tata"),
		        new String[] { "-a", "toto", "-b", "titi", "-c",
		                "tutu titi\\\"", "-d", "tata", });
		doAssertion(splitArguments("-a toto -b \"-c \\\"titi\\\"\""), new String[] { "-a",
	        "toto", "-b", "-c \\\"titi\\\"" });
	}

	private static void doAssertion(String[] input, String[] expectedOutput) {
		assert (input.length == expectedOutput.length) : "Length";
		for (int i = 0; i < expectedOutput.length; i++)
			assert input[i].equals(expectedOutput[i]) : input[i] + " != "
			        + expectedOutput[i];
	}
	
	public static void configure(final Object bean, final String configuration) throws ConfigurationException {
		try {
			new CmdLineParser(bean).parseArgument(splitArguments(configuration));
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}
}
