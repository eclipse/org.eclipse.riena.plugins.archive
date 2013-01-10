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
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 * Wraps the {@link SubModuleView}.
 */
@SuppressWarnings("restriction")
public class PartWrapper {

	public static final String VIEW_KEY = PartWrapper.class.getName() + ".rienaNavigationNodeView"; //$NON-NLS-1$
	private static final String VIEWS_EXT_POINT = "org.eclipse.ui.views"; //$NON-NLS-1$

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	public void create(final Composite parent, final MPart part) {
		/*
		 * the elementId of the part has the following format: typeId:secondayId#partCounter
		 */

		final String[] rienaCompoundId = RienaPartHelper.extractRienaCompoundId(part);
		final String typeId = rienaCompoundId[0];
		final String secondayId = rienaCompoundId[1];

		final ISubModuleNode node = SwtViewProvider.getInstance().getNavigationNode(typeId, secondayId, ISubModuleNode.class);

		if (RienaPartHelper.isSharedView(node)) {
			final SubModuleView viewInstance = ViewInstanceProvider.getInstance().getView(typeId);
			if (null != viewInstance) {
				return;
			}
		}
		createView(typeId);
	}

	private SubModuleView createView(final String typeId) {
		final IConfigurationElement[] configurationElements = extensionRegistry.getConfigurationElementsFor(VIEWS_EXT_POINT);
		for (final IConfigurationElement element : configurationElements) {
			if (typeId.equals(element.getAttribute("id"))) { //$NON-NLS-1$
				try {
					final SubModuleView viewInstance = (SubModuleView) element.createExecutableExtension("class"); //$NON-NLS-1$
					ViewInstanceProvider.getInstance().registerView(typeId, viewInstance);
					return viewInstance;
				} catch (final CoreException e1) {
					e1.printStackTrace();
				}
			}

		}
		return null;

	}

}
