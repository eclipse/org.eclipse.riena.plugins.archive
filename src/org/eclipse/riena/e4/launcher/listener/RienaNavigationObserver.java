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
package org.eclipse.riena.e4.launcher.listener;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.riena.navigation.IApplicationNode;
import org.eclipse.riena.navigation.listener.NavigationTreeObserver;

/**
 * Convenience class for observation of the riena navigation model.
 */
public class RienaNavigationObserver {

	@Inject
	private IEclipseContext eclipseContext;

	/**
	 * Installs listeners for all riena navigation layers. These listeners dispatch application logic to the e4 application runtime model.
	 */
	public void install(final IApplicationNode applicationNode) {
		final NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
		navigationTreeObserver.addListener(createInstance(MyApplicationNodeListener.class, eclipseContext));
		navigationTreeObserver.addListener(createInstance(MySubApplicationNodeListener.class, eclipseContext));
		navigationTreeObserver.addListener(createInstance(MyModuleGroupNodeListener.class, eclipseContext));
		navigationTreeObserver.addListener(createInstance(MyModuleNodeListener.class, eclipseContext));
		navigationTreeObserver.addListener(createInstance(MySubModuleNodeListener.class, eclipseContext));
		navigationTreeObserver.addListenerTo(applicationNode);
	}

	private <T> T createInstance(final Class<T> clazz, final IEclipseContext context) {
		return ContextInjectionFactory.make(clazz, context);
	}

}
