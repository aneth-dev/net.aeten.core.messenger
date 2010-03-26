package org.pititom.core;

/**
 * <p>
 * This service manager is based on package naming convention :
 * <li>Services interfaces must be located in package named *.service</li>
 * <li>Providers classes must be located in package named *.provider</li>
 * </p>
 * 
 * Root search package must be registered like : <blockquote>
 * 
 * <pre>
 * Service.registerRootPackage(&quot;org.pititom.core&quot;);
 * </pre>
 * 
 * </blockquote> What is already done for it.
 * 
 * @author Thomas Pérennou
 */
public class Service {
	private static ServiceLoader proxy = new DefaultServiceLoader();

	public static void setProxy(ServiceLoader serviceLoaderProxy) {
		Service.proxy = serviceLoaderProxy;
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public static <S extends Identifiable> S getProvider(Class<S> service, String identifier) {
		return proxy.getProvider(service, identifier);
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public static <S> Iterable<S> getProviders(Class<S> service) {
		return proxy.getProviders(service);
	}

	/**
	 * @return The registered services
	 */
	public static Iterable<Class<?>> getServices() {
		return proxy.getServices();
	}

	public void registerService(Class<?> service) {
		proxy.registerService(service);
	}

	public static <T> void registerProvider(Class<T> service, T provider) {
		proxy.registerProvider(service, provider);
	}
	public static void registerRootPackage(String rootPackage) {
		proxy.registerRootPackage(rootPackage);
	}

}
