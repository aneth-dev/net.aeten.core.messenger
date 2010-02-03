package org.pititom.core.messenger.extension;

import java.nio.charset.Charset;

import org.pititom.core.stream.extension.StreamEditor;

public interface MessengerEditor extends StreamEditor {
	public static final Charset CLASS_NAME_CHARSET = Charset.forName("UTF-8");
}
