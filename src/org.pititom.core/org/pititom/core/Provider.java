package org.pititom.core;

/**
 *
 * @author Thomas Pérennou
 */
public interface Provider {
	
	public boolean isProvides(Class<?> clazz);

    public <T> T get(Class<T> clazz);

}
