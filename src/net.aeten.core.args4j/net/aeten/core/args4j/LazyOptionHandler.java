package net.aeten.core.args4j;

import net.aeten.core.Lazy;
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
//@Provider(OptionHandler.class)
@ValueType(Lazy.class)
public class LazyOptionHandler extends OptionHandler<Lazy<?, ?>> {
	
	public LazyOptionHandler(CmdLineParser parser, OptionDef option, Setter<Lazy<?, ?>> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String className = params.getParameter(0);
		try {
			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			Lazy<?, ?> factory = Lazy.build(clazz);
			setter.addValue(factory);
		} catch (ClassNotFoundException exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "LAZY";
	}

	private static boolean contains(String element, String[] list) {
		for (String item : list) {
			if (element.equals(item)) {
				return true;
			}
		}
		return false;

	}
}
