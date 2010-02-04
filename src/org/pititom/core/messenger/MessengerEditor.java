package org.pititom.core.messenger;

import java.nio.charset.Charset;

import org.pititom.core.stream.controller.StreamEditor;

/**
*
* @author Thomas Pérennou
*/
public interface MessengerEditor extends StreamEditor {
	public static final Charset CLASS_NAME_CHARSET = Charset.forName("UTF-8");
}
