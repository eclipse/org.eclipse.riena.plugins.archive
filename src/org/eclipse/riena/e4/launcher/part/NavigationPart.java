/*******************************************************************************
 * Copyright (c) 2007, 2014 compeople AG and others.
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

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.NavigationViewPart;

public class NavigationPart {
	@Inject
	private MWindow window;

	@Inject
	private EModelService modelService;

	@Inject
	public void createUI(final Composite parent) {
		new NavigationViewPart() {
			@Override
			public ISubApplicationNode getSubApplicationNode() {
				final MPerspective activePerspective = modelService.getActivePerspective(window);
				final String perspectiveId = activePerspective.getElementId();
				return SwtViewProvider.getInstance().getNavigationNode(perspectiveId, ISubApplicationNode.class);
			};
		}.createPartControl(parent);
	}
}
