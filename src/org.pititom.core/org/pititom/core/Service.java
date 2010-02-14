package org.pititom.core;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;

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
public class Service {

	static Map<Class<?>, Collection<Class<?>>> services = new HashMap<Class<?>, Collection<Class<?>>>();
	static Map<Class<?>, Collection<Object>> instances = new HashMap<Class<?>, Collection<Object>>();

	static {
		registerRootPackage("org.pititom.core");
	}

	/**
	 * Register root search package
	 * 
	 * @param rootPackageName
	 *            the root package name
	 */
	public static void registerRootPackage(String rootPackageName) {

		try {
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(rootPackageName.replace('.', '/'));
			while (urls.hasMoreElements()) {
				URL resource = urls.nextElement();
				if (resource.toString().startsWith("jar:")) {
					loadFromJarFileEntries(rootPackageName, resource, ResourceType.SERVICE);
				} else {
					loadServicesFromDirectory(rootPackageName, resource);
				}
			}

			urls = Thread.currentThread().getContextClassLoader().getResources(rootPackageName.replace('.', '/'));
			while (urls.hasMoreElements()) {
				URL resource = urls.nextElement();
				if (resource.toString().startsWith("jar:")) {
					loadFromJarFileEntries(rootPackageName, resource, ResourceType.PROVIDER);
				} else {
					loadProvidersFromDirectory(rootPackageName, resource);
				}
			}

		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	/**
	 * @return The registered services
	 */
	public static Iterable<Class<?>> getServices() {
		return services.keySet();
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	@SuppressWarnings("unchecked")
	public static <S> Iterable<S> getProviders(Class<S> service) {
		// Load conventional packaging providers
		Collection<S> providers = (Collection<S>) instances.get(service);
		if (providers == null) {
			providers = new ArrayList<S>();

			Collection<?> classes = services.get(service);
			if (classes != null) {
				for (Class<?> clazz : services.get(service)) {
					try {
						providers.add((S) clazz.newInstance());
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

	private static enum ResourceType {
		SERVICE,
		PROVIDER;
		
		private final String directory;
		private ResourceType() {
			this.directory = this.name().toLowerCase();
		}
		public String getDirectory() {
			return directory;
		}
	}
	
	private static void loadProvidersFromJarEntry(String entryName) {
		try {
			Class<?> provider = Class.forName(entryName.replace('/', '.').substring(0, entryName.length() - 6));
			for (Map.Entry<Class<?>, Collection<Class<?>>> service : services.entrySet()) {
				if (service.getKey().isAssignableFrom(provider)) {
					try {
						service.getValue().add(provider);
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
				}
			}
		} catch (ClassNotFoundException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}
	
	private static void loadServicesFromJarEntry(String entryName) {
		try {
			services.put(Class.forName(entryName.replace('/', '.').substring(0, entryName.length() - 6)), new ArrayList<Class<?>>());
		} catch (ClassNotFoundException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	private static void loadFromJarFileEntries(String rootPackage, URL resource, ResourceType resourceType) {
		String rootDirectory = rootPackage.replace('.', '/');
		try {
			JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
			Enumeration<JarEntry> entries = jarConnection.getJarFile().entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.isDirectory() && entry.getName().matches(rootDirectory + "((/.+/)|/)" + resourceType.getDirectory() + "/[^/^$]+\\.class$")) {
					switch (resourceType) {
						case PROVIDER:
							loadProvidersFromJarEntry(entry.getName());
							break;
						case SERVICE:
							loadServicesFromJarEntry(entry.getName());
							break;
					}
				}
			}
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}

	}

	private static void loadServicesFromDirectory(String rootPackage, URL resource) {
		String rootClassLoader = resource.getPath().substring(0, resource.getPath().length() - rootPackage.length());

		List<String> packages = findPackages(rootClassLoader, rootPackage, new File(resource.getPath()), new ArrayList<String>());
		for (String pkg : packages) {
			if (pkg.endsWith(".service")) {
				try {
					for (Class<?> extension : getClasses(pkg)) {
						services.put(extension, new ArrayList<Class<?>>());
					}
				} catch (Exception exception) {
				}
			}
		}
	}

	private static void loadProvidersFromDirectory(String rootPackage, URL resource) {
		String rootClassLoader = resource.getPath().substring(0, resource.getPath().length() - rootPackage.length());

		List<String> packages = findPackages(rootClassLoader, rootPackage, new File(resource.getPath()), new ArrayList<String>());
		Collection<Class<?>> providers = new ArrayList<Class<?>>();
		for (String pkg : packages) {
			if (pkg.endsWith(".provider")) {
				try {
					for (Class<?> provider : getClasses(pkg)) {
						providers.add(provider);
					}
				} catch (Exception exception) {
				}
			}
		}

		for (Class<?> provider : providers) {
			for (Map.Entry<Class<?>, Collection<Class<?>>> service : services.entrySet()) {
				if (service.getKey().isAssignableFrom(provider)) {
					try {
						service.getValue().add(provider);
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
				}
			}
		}
	}

	private static List<String> findPackages(String rootPath, String currentPackage, File currentDirectory, List<String> packageNames) {
		if (currentDirectory.toString().startsWith("jar:")) {
		}
		File[] files = currentDirectory.listFiles();
		if (files == null) {
			return packageNames;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				String pkg = currentPackage + "." + file.getName();
				packageNames.add(pkg);
				findPackages(rootPath, pkg, file, packageNames);
			}
		}
		return packageNames;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            the base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			String fileName = resource.getFile();
			String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
			dirs.add(new File(fileNameDecoded));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * ubdirs.
	 * 
	 * @param directory
	 *            the base directory
	 * @param packageName
	 *            the package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (file.isDirectory()) {
				assert !fileName.contains(".");
				classes.addAll(findClasses(file, packageName + "." + fileName));
			} else if (fileName.endsWith(".class") && !fileName.contains("$")) {
				Class _class;
				try {
					_class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
				} catch (ExceptionInInitializerError e) {
					// happen, for example, in classes, which depend on
					// Spring to inject some beans, and which fail,
					// if dependency is not fulfilled
					_class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6), false, Thread.currentThread().getContextClassLoader());
				}
				classes.add(_class);
			}
		}
		return classes;
	}
}