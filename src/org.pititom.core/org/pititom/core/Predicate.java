package org.pititom.core;

public interface Predicate<T> {
	public boolean matches(T element);
}