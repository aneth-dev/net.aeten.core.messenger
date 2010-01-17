package org.pititom.core.stream.dada;


import java.util.ArrayList;
import java.util.List;

import org.pititom.core.stream.extension.StreamEditor;

public class StreamEditorStack {
	
	private final List<EditorEntry> stack = new ArrayList<EditorEntry>();
	
	public List<EditorEntry> getStack() {
		return this.stack;
		
	}
	
	public static class EditorEntry {
		private final Class<? extends StreamEditor> editor;
		private final String configuration;
		
		public EditorEntry(Class<? extends StreamEditor> editor, String configuration) {
	        super();
	        this.configuration = configuration;
	        this.editor = editor;
        }
		
		public Class<? extends StreamEditor> getEditor() {
        	return editor;
        }
		
		public String getConfiguration() {
        	return configuration;
        }
	}
	
}