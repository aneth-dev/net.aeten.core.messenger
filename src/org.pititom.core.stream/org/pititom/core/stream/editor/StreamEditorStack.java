package org.pititom.core.stream.editor;


import java.util.ArrayList;
import java.util.List;
import org.pititom.core.Factory;

/**
 *
 * @author Thomas Pérennou
 */
public class StreamEditorStack {
	
	private final List<Factory<StreamEditor>> stack = new ArrayList<Factory<StreamEditor>>();
	
	public List<Factory<StreamEditor>> getStack() {
		return this.stack;
	}	
}