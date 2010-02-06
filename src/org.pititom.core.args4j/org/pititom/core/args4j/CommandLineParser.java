package org.pititom.core.args4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.pititom.core.Factory;

/**
 * {@link Class} {@link CmdLineParser}.
 *
 * @author Thomas Pérennou
 */
public class CommandLineParser extends CmdLineParser {
	static {
		registerHandler(Class.class, ClassOptionHandler.class);
		registerHandler(InetAddress.class, InetAddressOptionHandler.class);
		registerHandler(InetSocketAddress.class,
		        InetSocketAddressOptionHandler.class);
		registerHandler(Factory.class,
		        FactoryOptionHandler.class);
	}

	public CommandLineParser(Object bean) {
		super(bean);
	}

	public static String[] splitArguments(String arguments)
	        throws CmdLineException {
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
			if (end > i)
				splittedArguments.add(new String(arguments.getBytes(), i, end
				        - i));
			if (end == -1)
				throw new CmdLineException(null, "Unclosed quote at char "
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
	}

	private static void doAssertion(String[] input, String[] expectedOutput)
	        throws CmdLineException {
		assert (input.length == expectedOutput.length) : "Length";
		for (int i = 0; i < expectedOutput.length; i++)
			assert input[i].equals(expectedOutput[i]) : input[i] + " != "
			        + expectedOutput[i];
	}
}
