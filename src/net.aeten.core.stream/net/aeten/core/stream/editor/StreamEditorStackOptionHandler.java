package net.aeten.core.stream.editor;

import net.aeten.core.Lazy;

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
public class StreamEditorStackOptionHandler extends OptionHandler<StreamEditorStack> {
	public static final String EDITOR_OPTION_NAME = "-se";
	public static final String[] EDITOR_OPTION_ALIASES = { "--stream-editor" };
	public static final String EDITOR_CONFIGURATION_OPTION_NAME = "-c";
	public static final String[] EDITOR_CONFIGURATION_OPTION_ALIASES = { "--configuration" };

	public StreamEditorStackOptionHandler(CmdLineParser parser, OptionDef option, Setter<StreamEditorStack> setter) {
		super(parser, option, setter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int i = 0;
		try {
			StreamEditorStack editorStackOption = new StreamEditorStack();
			Class<?> clazz = null;
			for (;; i++) {
				try {
					if (EDITOR_OPTION_NAME.equals(params.getParameter(i)) || contains(params.getParameter(i), EDITOR_OPTION_ALIASES)) {
						++i;
					}
					clazz = Thread.currentThread().getContextClassLoader().loadClass(params.getParameter(i));
				} catch (CmdLineException exception) {
					break;
				}

				String configuration;

				try {
					if (EDITOR_CONFIGURATION_OPTION_NAME.equals(params.getParameter(i + 1)) || contains(params.getParameter(i + 1), EDITOR_CONFIGURATION_OPTION_ALIASES)) {
						i += 2;
						configuration = params.getParameter(i);
					} else {
						configuration = null;
					}
				} catch (CmdLineException exception) {
					configuration = null;
				}

				editorStackOption.getStack().add(Lazy.build((Class<StreamEditor>) clazz, configuration));
			}
			setter.addValue(editorStackOption);
		} catch (ClassNotFoundException exception) {
			throw new CmdLineException(this.owner, exception);
		}
		return i;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "EDITOR_STACK";
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