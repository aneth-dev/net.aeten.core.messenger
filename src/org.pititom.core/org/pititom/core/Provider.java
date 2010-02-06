package org.pititom.core;

public interface Provider {
	
	public boolean isProvides(Class<?> clazz);

    public <T> T get(Class<T> clazz);

}
