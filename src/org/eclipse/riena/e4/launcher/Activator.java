/*******************************************************************************
 * Copyright (c) 2007, 2012 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.e4.launcher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.eclipse.riena.core.wire.Wire;
import org.eclipse.riena.e4.launcher.exception.E4UncaughtExceptionHandler;

public class Activator implements BundleActivator {
	private BundleContext bundleContext;
	private static Activator singleton;

	public static Activator getDefault() {
		return singleton;
	}

	/**
	 * @return the bundleContext
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		bundleContext = context;
		singleton = this;
		Wire.instance(new E4UncaughtExceptionHandler().install()).andStart(getBundleContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		bundleContext = null;
		singleton = null;
	}

}
