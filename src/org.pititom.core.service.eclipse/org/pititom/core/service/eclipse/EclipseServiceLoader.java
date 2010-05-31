package org.pititom.core.service.eclipse;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pititom.core.Configurable;
import org.pititom.core.args4j.Yaml2Args;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.parsing.provider.PmlParser;
import org.pititom.core.parsing.service.Parser;
import org.pititom.core.service.DefaultServiceLoader;
import org.pititom.core.service.Service;

@SuppressWarnings("restriction")
public class EclipseServiceLoader extends DefaultServiceLoader {
	
	private static final ClassLoader CLASS_LOADER_REPLACEMENT = new ClassLoader() {
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			for (Bundle bundle : bundles) {
				try {
					return bundle.loadClass(name);
				} catch (ClassNotFoundException exception) {
					
				}
			}
			throw new ClassNotFoundException(name);
		}
	};

	
	public static Collection<Bundle> bundles = new ArrayList<Bundle>();

	protected Map<Class<?>, BundleContext> serviceBundleContexts;
	public EclipseServiceLoader() {}

	public synchronized void registerRootPackage(String bundleName) {
		if (serviceBundleContexts == null) {
			serviceBundleContexts = new HashMap<Class<?>, BundleContext>();
		}
		for (Bundle bundle : InternalPlatform.getDefault().getBundleContext().getBundles()) {
			if (bundle.getSymbolicName().startsWith(bundleName)) {
				registerBundle(bundle);
			}
		}
	}

	public synchronized void registerBundle(Bundle bundle) {
		if (bundle.getResource("/META-INF/MANIFEST.MF") != null) {
			bundles.add(bundle);
			loadFromBundle(bundle, ResourceType.SERVICE);
			loadFromBundle(bundle, ResourceType.PROVIDER);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFromBundle(Bundle bundle, ResourceType resourceType) {
		Enumeration<URL> urls = bundle.findEntries("/", "*.class", true);

		if (urls == null) {
			return;
		}
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			
			String entryName = url.getFile().toString();
			String pattern = ".+/";
			switch (resourceType) {
				case PROVIDER:
					pattern += "provider";
					break;
				case SERVICE:
					pattern += "service";
					break;
			}
			pattern += "/[^/^$]+\\.class";
			if (entryName.matches(pattern)) {
				Class<?> clazz;
				try {
					clazz = bundle.loadClass(entryName.replace('/', '.').substring(entryName.startsWith("/bin/") ? 5 : 1, entryName.length() - 6));
					switch (resourceType) {
						case PROVIDER:
							this.registerProvider(clazz);
							break;
						case SERVICE:
							super.registerService(clazz);
							this.serviceBundleContexts.put(clazz, (bundle.getBundleContext() == null) ? InternalPlatform.getDefault().getBundleContext() : bundle.getBundleContext());
							break;
					}
				} catch (ClassNotFoundException exception) {
					Logger.log(this, LogLevel.ERROR, exception);
				}
			}

		}
	}

	@Override
	public synchronized void registerService(Class<?> service) {
		super.registerService(service);
		this.serviceBundleContexts.put(service, InternalPlatform.getDefault().getBundleContext());
	}
	
	@Override
	public synchronized <T> void registerProvider(Class<T> service, T provider) {
		super.registerProvider(service, provider);
		InternalPlatform.getDefault().getBundleContext().registerService(service.getName(), provider, null);
		BundleContext bundleContext = this.serviceBundleContexts.get(service);
		if (bundleContext == null) {
			bundleContext = InternalPlatform.getDefault().getBundleContext();
		}
		bundleContext.registerService(service.getName(), provider, null);
		
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <S> Iterable<S> getProviders(Class<S> service) {
		// Load conventional packaging providers
		
		Collection<S> providers = (Collection<S>) instances.get(service);
		if (providers == null) {
			providers = new HashSet<S>();

			// Switch to customized class loader
			ClassLoader savedClassloader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(CLASS_LOADER_REPLACEMENT);

				Collection<Class<?>> classes = services.get(service);
			if (classes != null) {
				for (Class<?> clazz : classes) {
					try {
						if (Configurable.class.isAssignableFrom(clazz)) {
							for (Bundle bundle : bundles) {
								Enumeration<URL> files = bundle.findEntries("/META-INF/provider/" + clazz.getName(), "*", false);
								if (files == null) {
									continue;
								}
								while (files.hasMoreElements()) {
									URL resource = FileLocator.toFileURL(files.nextElement());
									File file = new File(resource.getPath());
									if (!file.isDirectory()) {
										Configurable provider = (Configurable) clazz.newInstance();
										provider.configure(Yaml2Args.convert(file, Service.getProvider(Parser.class, PmlParser.class.getName())));
										
										Logger.log(this, LogLevel.TRACE, "Instantiate provider " + provider + " for service " + service.getName());
										this.serviceBundleContexts.get(service).registerService(service.getName(), provider, null);
										providers.add((S) provider);
									}
								}
							}

						} else {
							S provider = (S) clazz.newInstance();
							Logger.log(this, LogLevel.TRACE, "Instantiate provider " + provider + " for service " + service.getName());
							providers.add(provider);
						}
					} catch (Exception exception) {
						Logger.log(this, LogLevel.ERROR, "Unable to load provider " + clazz.getName() + " for service " + service.getName(), exception);
					}
				}
			}
			instances.put(service, (Collection<Object>) providers);
			
			for (Iterator<S> serviceIterator = ServiceLoader.load(service).iterator(); serviceIterator.hasNext();) {
				providers.add(serviceIterator.next());
			}
			// Restore class loader
			Thread.currentThread().setContextClassLoader(savedClassloader);
		}

		// Load OSGI providers
		for (Bundle bundle : bundles) {
			try {
				ServiceReference[] serviceReferences = (bundle.getBundleContext() == null) ? null : bundle.getBundleContext().getServiceReferences(service.getName(), null);
				if (serviceReferences != null) {
					for (ServiceReference serviceReference : serviceReferences) {
						Object bean = bundle.getBundleContext().getService(serviceReference);
						if (service.isAssignableFrom(bean.getClass())) {
							providers.add((S) bean);
						}
					}
				}
			} catch (InvalidSyntaxException exception) {
				// Should not append (Filter is null)
				Logger.log(this, LogLevel.ERROR, exception);
			}
		}

		// Load standard java.util.ServiceLoader providers
		for (Iterator<S> serviceIterator = ServiceLoader.load(service).iterator(); serviceIterator.hasNext();) {
			providers.add(serviceIterator.next());
		}		
		
		return providers;
	}

}