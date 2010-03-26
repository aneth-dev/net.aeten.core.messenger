package org.pititom.core.eclipse;

import java.util.ArrayList;
import java.util.Collection;

import org.osgi.framework.Bundle;
import org.pititom.core.ClassLoader;

class EclipseClassLoader extends ClassLoader {
	protected static Collection<Bundle> bundles = new ArrayList<Bundle>();


	@Override
	protected Class<?> loadClassImplementation(String className) throws ClassNotFoundException {
		for (Bundle bundle : bundles) {
			try {
				return bundle.loadClass(className);
			} catch (ClassNotFoundException exception) {}
		}
		throw new ClassNotFoundException();
	}

}
