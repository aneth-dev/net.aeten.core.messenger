package net.aeten.core.args4j;

import java.net.InetSocketAddress;

import net.aeten.core.spi.Provider;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link InetSocketAddress} {@link OptionHandler}.
 * 
 * @author Thomas Pérennou
 */
@Provider(OptionHandler.class)
@ValueType(InetSocketAddress.class)
public class InetSocketAddressOptionHandler extends OptionHandler<InetSocketAddress> {
	public InetSocketAddressOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super InetSocketAddress> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		try {
			final String[] pamareters = params.getParameter(0).split(":");
			setter.addValue(new InetSocketAddress(pamareters[0], Integer.valueOf(pamareters[1])));
		} catch (Exception exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "INET_SOCKET_ADDRESS";
	}
}
