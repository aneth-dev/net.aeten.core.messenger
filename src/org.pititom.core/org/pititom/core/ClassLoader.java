package org.pititom.core;

public abstract class ClassLoader {
	private static ClassLoader proxy = new DefaultClassLoader();

	public static void setProxy(ClassLoader proxy) {
		ClassLoader.proxy = proxy;
	}

	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		return proxy.loadClassImplementation(className);
	}
	protected abstract Class<?> loadClassImplementation(String className) throws ClassNotFoundException;
	
	private static class DefaultClassLoader extends ClassLoader {
		protected Class<?> loadClassImplementation(String className) throws ClassNotFoundException {
			return Class.forName(className);
		}
	}
}
