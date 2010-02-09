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
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingForwarder;

/**
 * 
 * @author Thomas Pérennou
 */
public class Service {

	static Map<Class<?>, Collection<Class<?>>> services = new HashMap<Class<?>, Collection<Class<?>>>();
	static Map<Class<?>, Collection<Object>> instances = new HashMap<Class<?>, Collection<Object>>();

	static {
		registerRootPackage("org.pititom.core");
	}

	static void registerRootPackage(String packageName) {

		try {
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageName.replace('.', '/'));
			while (urls.hasMoreElements()) {
				URL resource = urls.nextElement();
				String rootClassLoader = resource.getPath().substring(0, resource.getPath().length() - packageName.length());

				List<String> packages = findPackages(rootClassLoader, packageName, new File(resource.toString().startsWith("jar:") ? resource.toString() : resource.getPath()), new ArrayList<String>());
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
				Collection<Class<?>> providers = new ArrayList<Class<?>>();
				for (String pkg : packages) {
					if (pkg.endsWith(".provider")) {
						try {
							for (Class<?> contribution : getClasses(pkg)) {
								providers.add(contribution);
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
								LoggingForwarder.getInstance().forward(Service.class, LoggingEvent.ERROR, new LoggingData(exception));
							}
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<String> findPackages(String rootPath, String currentPackage, File currentDirectory, List<String> packageNames) {
		if (currentDirectory.toString().startsWith("jar:")) {
			try {
				URL url = new URL(currentDirectory.toString());
				JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
				Enumeration<JarEntry> entries = jarConnection.getJarFile().entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.isDirectory()) {
						String pkg = entry.getName().replace('/', '.');
						pkg = pkg.substring(0, pkg.length() - 1);
						if (pkg.startsWith(currentPackage)) {
							packageNames.add(pkg);
						}

					} else {

						if (entry.getName().matches(".*/service/[^/]+\\.class")) {
							try {
								services.put(Class.forName(entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6)), new ArrayList<Class<?>>());
							} catch (ClassNotFoundException exception) {
								// TODO Auto-generated catch block
								exception.printStackTrace();
							}
						}
					}
				}

				entries = jarConnection.getJarFile().entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (!entry.isDirectory()) {
						if (entry.getName().matches(".*/provider/[^/]+\\.class")) {
							try {
								Class<?> provider = Class.forName(entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6));
								for (Map.Entry<Class<?>, Collection<Class<?>>> service : services.entrySet()) {
									if (service.getKey().isAssignableFrom(provider)) {
										try {
											service.getValue().add(provider);
										} catch (Exception exception) {
											LoggingForwarder.getInstance().forward(Service.class, LoggingEvent.ERROR, new LoggingData(exception));
										}
									}
								}
							} catch (ClassNotFoundException exception) {
								// TODO Auto-generated catch block
								exception.printStackTrace();
							}
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return packageNames;
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
	 *            The base package
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
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
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

	public static Collection<Class<?>> getServices() {
		return services.keySet();
	}

	@SuppressWarnings("unchecked")
	public static <S> Collection<S> getProviders(Class<S> service) {
		Collection<S> providers = (Collection<S>) instances.get(service);
		if (providers == null) {
			providers = new ArrayList<S>();
			for (Class<?> clazz : services.get(service)) {
				try {
					providers.add((S) clazz.newInstance());
				} catch (Exception exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
			}
		}

		return providers;
	}
}
