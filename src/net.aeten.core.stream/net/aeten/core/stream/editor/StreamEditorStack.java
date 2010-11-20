package net.aeten.core.stream.editor;


import java.util.ArrayList;
import java.util.List;

import net.aeten.core.Singleton;

/**
 *
 * @author Thomas Pérennou
 */
public class StreamEditorStack {
	
	private final List<Singleton<StreamEditor>> stack = new ArrayList<Singleton<StreamEditor>>();
	
	public List<Singleton<StreamEditor>> getStack() {
		return this.stack;
	}	
}