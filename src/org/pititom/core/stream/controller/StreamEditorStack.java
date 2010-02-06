package org.pititom.core.stream.controller;


import java.util.ArrayList;
import java.util.List;
import org.pititom.core.Factory;

import org.pititom.core.stream.controller.StreamEditor;

class StreamEditorStack {
	
	private final List<Factory<StreamEditor>> stack = new ArrayList<Factory<StreamEditor>>();
	
	public List<Factory<StreamEditor>> getStack() {
		return this.stack;
	}	
}