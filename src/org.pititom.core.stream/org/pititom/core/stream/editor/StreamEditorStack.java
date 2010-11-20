package org.pititom.core.stream.editor;


import java.util.ArrayList;
import java.util.List;

import org.pititom.core.Singleton;

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