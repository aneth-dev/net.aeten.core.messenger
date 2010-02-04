package org.pititom.core.stream.controller;


import java.util.ArrayList;
import java.util.List;
import org.pititom.core.ContributionFactory;

import org.pititom.core.stream.controller.StreamEditor;

public class StreamEditorStack {
	
	private final List<ContributionFactory<StreamEditor>> stack = new ArrayList<ContributionFactory<StreamEditor>>();
	
	public List<ContributionFactory<StreamEditor>> getStack() {
		return this.stack;
	}	
}