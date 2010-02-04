package org.pititom.core;

import org.pititom.core.extersion.Configurable;

/**
 *
 * @author Thomas Pérennou
 */
public class ContributionFactory<T> {

	private Class<T> clazz;
	private String configuration;
	private T bean = null;

	public ContributionFactory(Class<T> clazz, String configuration) {
		this.clazz = clazz;
		this.configuration = configuration;
	}

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
}
