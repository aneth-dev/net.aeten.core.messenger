package org.pititom.core.eclipse;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.pititom.core.Configurable;
import org.pititom.core.DefaultServiceLoader;
import org.pititom.core.args4j.Yaml2Args;

@SuppressWarnings("restriction")
class EclipseServiceLoader extends DefaultServiceLoader {
	public EclipseServiceLoader() {
	}

	public synchronized void registerRootPackage(String bundleName) {
		for (Bundle bundle : InternalPlatform.getDefault().getBundleContext().getBundles()) {
			if (bundle.getSymbolicName().startsWith(bundleName)) {
				registerBundle(bundle);
			}
		}
	}

	public synchronized void registerBundle(Bundle bundle) {

		if (bundle.getResource("/META-INF/MANIFEST.MF") != null) {
			EclipseClassLoader.bundles.add(bundle);
			loadFromBundle(bundle, ResourceType.SERVICE);
			loadFromBundle(bundle, ResourceType.PROVIDER);
		}
	}

	private void loadFromBundle(Bundle bundle, ResourceType resourceType) {
		Enumeration<URL> urls = bundle.findEntries("/bin", "*.class", true);

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
					clazz = bundle.loadClass(entryName.replace('/', '.').substring(5, entryName.length() - 6));
					switch (resourceType) {
						case PROVIDER:
							registerProvider(clazz);
							break;
						case SERVICE:
							registerService(clazz);
							break;
					}
				} catch (ClassNotFoundException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
			}

		}
	}

	public synchronized <S> Iterable<S> getProviders(Class<S> service) {
		// Load conventional packaging providers

		Collection<S> providers = (Collection<S>) instances.get(service);
		if (providers == null) {
			providers = new ArrayList<S>();

			Collection<?> classes = services.get(service);
			if (classes != null) {
				for (Class<?> clazz : services.get(service)) {
					try {
						if (Configurable.class.isAssignableFrom(clazz)) {
							for (Bundle bundle : EclipseClassLoader.bundles) {
								Enumeration<URL> files = bundle.findEntries("/META-INF/provider/" + clazz.getName(), "*", false);
								if (files == null) {
									continue;
								}
								while (files.hasMoreElements()) {
									URL resource = FileLocator.toFileURL(files.nextElement());
									File file = new File(resource.getPath());
									Configurable provider = (Configurable) clazz.newInstance();
									provider.configure(Yaml2Args.convert(file));
									providers.add((S) provider);
								}
							}

						} else {
							providers.add((S) clazz.newInstance());
						}
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
				}
			}
			instances.put(service, (Collection<Object>) providers);

		}

		// Load standard java.util.ServiceLoader providers
		for (Iterator<S> serviceIterator = ServiceLoader.load(service).iterator(); serviceIterator.hasNext();) {
			providers.add(serviceIterator.next());
		}

		return providers;
	}

}