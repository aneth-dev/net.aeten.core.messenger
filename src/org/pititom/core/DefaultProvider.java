package org.pititom.core;

public class DefaultProvider implements Provider {
	private final Object[] providedObjectList;
	public DefaultProvider(Object... objects) {
		this.providedObjectList = objects;
	}
	public boolean isProvides(Class<?> clazz) {
		for (Object providedObject: this.providedObjectList)
			if (clazz.isInstance(providedObject))
				return true;
		return false;
	}
	
	@SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
		for (Object providedObject: this.providedObjectList)
			if (clazz.isInstance(providedObject))
				return (T)providedObject;
		return null;
	}
}
