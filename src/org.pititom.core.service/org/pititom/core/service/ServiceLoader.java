package org.pititom.core.service;

import org.pititom.core.Identifiable;


/**
 * <p>
 * This service manager is based on package naming convention :
 * <li>Services interfaces must be located in package named *.service</li>
 * <li>Providers classes must be located in package named *.provider</li>
 * </p>
 * <p>
 * It is also able to load providers throw java.util.ServiceLoader.
 * </p>
 * 
 * Root search package must be registered like : <blockquote>
 * 
 * <pre>
 * Service.registerRootPackage(&quot;org.pititom.core&quot;);
 * </pre>
 * 
 * </blockquote> Witch is already done.
 * 
 * @author Thomas Pérennou
 */
public interface ServiceLoader {
	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public <S extends Identifiable> S getProvider(Class<S> service, String identifier);
	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public <S> Iterable<S> getProviders(Class<S> service);
	/**
	 * @return The registered services
	 */
	public Iterable<Class<?>> getServices();
	public void registerService(Class<?> service);
	public <T> void registerProvider(Class<T> service, T provider);
	public void registerRootPackage(String rootPackage);
	public void excludePackage(String packageNamePattern);

}
