package net.aeten.core.stream.editor;

import java.util.ArrayList;
import java.util.List;

import net.aeten.core.Lazy;

/**
 * 
 * @author Thomas Pérennou
 */
public class StreamEditorStack {

	private final List<Lazy<StreamEditor, ?>> stack = new ArrayList<Lazy<StreamEditor, ?>>();

	public List<Lazy<StreamEditor, ?>> getStack() {
		return this.stack;
	}
}