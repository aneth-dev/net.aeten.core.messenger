package net.aeten.core.stream.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Thomas Pérennou
 */
public class StreamEditorStack {

	private final List<StreamEditor> stack = new ArrayList<>();

	public List<StreamEditor> getStack() {
		return this.stack;
	}
}