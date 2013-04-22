package org.eclipse.riena.internal.ui.swt.facades;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static Activator plugin;
	private BundleContext context;

	public void start(final BundleContext context) throws Exception {
		this.context = context;
		plugin = this;
	}

	public void stop(final BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * @return the context
	 */
	public BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return plugin;
	}

}
