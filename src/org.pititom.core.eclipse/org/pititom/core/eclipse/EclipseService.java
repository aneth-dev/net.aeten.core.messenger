package org.pititom.core.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.pititom.core.Configurable;
import org.pititom.core.Service;
import org.pititom.core.args4j.Yaml2Args;

public class EclipseService extends Service {
	private static Collection<Bundle> bundles = new ArrayList<Bundle>();
	
	private EclipseService() {
		registerBundle("org.pititom.core");
	}

	public static Service getInstance() {
		if (instance == null) {
			instance = new EclipseService();
		}
		return instance;
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		for (Bundle bundle : bundles) {
			try {
	            return bundle.loadClass(className);
            } catch (ClassNotFoundException exception) {}
		}
		throw new ClassNotFoundException();
	}

	public synchronized void registerBundle(String bundleName) {
		Bundle bundle = Platform.getBundle(bundleName);
		bundles.add(bundle);
		loadFromBundle(bundle, ResourceType.SERVICE);
		loadFromBundle(bundle, ResourceType.PROVIDER);
	}

	private void loadFromBundle(Bundle bundle, ResourceType resourceType) {
		Enumeration<URL> urls = bundle.findEntries("/bin", "*.class", true);

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
					clazz = Class.forName(entryName.replace('/', '.').substring(5, entryName.length() - 6));
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
							for (Bundle bundle : bundles) {
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
		}

		// Load standard java.util.ServiceLoader providers
		for (Iterator<S> serviceIterator = ServiceLoader.load(service).iterator(); serviceIterator.hasNext();) {
			providers.add(serviceIterator.next());
		}

		return providers;
	}

}
