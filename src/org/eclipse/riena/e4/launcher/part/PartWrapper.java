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
package org.eclipse.riena.e4.launcher.part;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;

public class PartWrapper {
	public static final String VIEW_KEY = PartWrapper.class.getName() + ".rienaNavigationNodeView"; //$NON-NLS-1$

	private static final String VIEWS_EXT_POINT = "org.eclipse.ui.views"; //$NON-NLS-1$

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	public void create(final Composite parent, final MPart part) {
		for (final IConfigurationElement e : extensionRegistry.getConfigurationElementsFor(VIEWS_EXT_POINT)) {
			if (part.getElementId().equals(e.getAttribute("id"))) {
				try {
					final Object viewInstance = e.createExecutableExtension("class");
					final ISubModuleNode node = SwtViewProvider.getInstance().getNavigationNode(part.getElementId(), ISubModuleNode.class);
					ReflectionUtils.invoke(viewInstance, "setNavigationNode", node);
					ReflectionUtils.invokeHidden(viewInstance, "setShellProvider", toShellProvider(parent.getShell()));
					ReflectionUtils.invoke(viewInstance, "createPartControl", parent);
					part.getTransientData().put(VIEW_KEY, viewInstance);
				} catch (final CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param shell
	 * @return
	 */
	private IShellProvider toShellProvider(final Shell shell) {
		return new IShellProvider() {
			public Shell getShell() {
				return shell;
			}
		};
	}
}
