package org.pititom.core;


/**
 *
 * @author Thomas Pérennou
 */
public class Factory<T> {

	private final Class<? extends T> clazz;
	private final String configuration;
	private T bean = null;

	public Factory(Class<? extends T> clazz, String configuration) {
		this.clazz = clazz;
		this.configuration = configuration;
	}

	@SuppressWarnings("cast")
	public T getInstance() throws ConfigurationException {
		if (this.bean == null) {
			try {
				this.bean = (T) clazz.newInstance();
				if ((configuration != null) && (bean instanceof Configurable)) {
					((Configurable) bean).configure(configuration);
				}
			} catch (Exception exception) {
				throw new ConfigurationException(this.configuration, exception);
			}
		}
		return this.bean;
	}
	
	public static final class Null<T> extends Factory<T> {

		public Null() {
			super(null, null);
		}

		@Override
		public T getInstance() throws ConfigurationException {
			return null;
		}
	}


}
