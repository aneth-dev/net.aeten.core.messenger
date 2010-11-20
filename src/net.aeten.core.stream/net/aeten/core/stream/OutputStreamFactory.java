package net.aeten.core.stream;

import java.io.OutputStream;
import java.lang.reflect.Constructor;

import net.aeten.core.Configurable;
import net.aeten.core.ConfigurationException;

/**
 *
 * @author Thomas Pérennou
 */
public class OutputStreamFactory<T extends OutputStream> {

	private Class<T> clazz;
	private String configuration;
	private OutputStream bean = null;

	public OutputStreamFactory(Class<T> clazz, String configuration) {
		this.clazz = clazz;
		this.configuration = configuration;
	}

	@SuppressWarnings("unchecked")
	public OutputStream getInstance() throws ConfigurationException {
		if (this.bean == null) {
			try {
				this.bean = clazz.newInstance();
				if ((configuration != null) && (bean instanceof Configurable)) {
					((Configurable<String>) bean).configure(configuration);
				}
			} catch (Exception defaultConstructorException) {
				if (configuration != null) {
					// TODO: Make it recursive and use eventual configuration
					try {
						Constructor<T> constructor = clazz.getConstructor(OutputStream.class);
						this.bean = constructor.newInstance(Thread.currentThread().getContextClassLoader().loadClass(configuration));
					} catch (Exception exception) {
						throw new ConfigurationException(this.configuration, new Exception(exception));
					}
				}
			}
		}
		return this.bean;
	}

}
