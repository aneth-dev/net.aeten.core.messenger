package net.aeten.core.args4j;

import net.aeten.core.spi.Provider;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link Class} {@link OptionHandler}.
 * 
 * @author Thomas Pérennou
 */
@Provider(OptionHandler.class)
@ValueType(Class.class)
public class ClassOptionHandler extends OptionHandler<Class<?>> {

	public ClassOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Class<?>> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		try {
			setter.addValue(Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(0)));
		} catch (ClassNotFoundException exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "CLASS";
	}
}
