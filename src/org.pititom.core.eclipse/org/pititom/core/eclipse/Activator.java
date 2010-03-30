package org.pititom.core.eclipse;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.pititom.core.ClassLoader;
import org.pititom.core.Service;


public class Activator extends Plugin {
	   // The plug-in ID
	   public static final String PLUGIN_ID = "org.pititom.core";

	   // The shared instance
	   private static Activator plugin;

	   /**
	    * The constructor
	    */
	   public Activator() {
		   ClassLoader.setProxy(new EclipseClassLoader());
		   Service.setProxy(new EclipseServiceLoader());
	   }

	   /*
	    * (non-Javadoc)
	    * 
	    * @see
	    * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	    * )
	    */
	   @Override
	   public void start(BundleContext context) throws Exception {
	      super.start(context);
	      plugin = this;
	   }

	   /*
	    * (non-Javadoc)
	    * 
	    * @see
	    * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	    * )
	    */
	   @Override
	   public void stop(BundleContext context) throws Exception {
	      plugin = null;
	      super.stop(context);
	   }

	   /**
	    * Returns the shared instance
	    * 
	    * @return the shared instance
	    */
	   public static Activator getDefault() {
	      return plugin;
	   }

}
