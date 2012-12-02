package net.aeten.core.messenger.stream;

import java.nio.charset.Charset;

import net.aeten.core.stream.StreamEditor;

/**
*
* @author Thomas Pérennou
*/
public interface MessengerEditor extends
		StreamEditor {
	public static final Charset CLASS_NAME_CHARSET = Charset.forName ("UTF-8");
}
