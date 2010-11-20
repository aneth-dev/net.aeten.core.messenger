package net.aeten.core.args4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link InetAddress} {@link OptionHandler}.
 *
 * @author Thomas Pérennou
 */
public class InetAddressOptionHandler extends OptionHandler<InetAddress> {
    public InetAddressOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super InetAddress> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        try {
	        setter.addValue(InetAddress.getByName(params.getParameter(0)));
        } catch (UnknownHostException exception) {
        	throw new CmdLineException(this.owner, exception);
        }
        return 1;
    }

    @Override
    public String getDefaultMetaVariable() {
        return "INET_ADDRESS";
    }
}
