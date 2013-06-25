/*******************************************************************************
 * Copyright (c) 2007, 2013 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.e4.launcher.part;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.jface.action.IContributionItem;

/**
 * Helper class for menu and tool bar.
 */
public class MenuPartHelper {

	private final IEclipseContext eclipseContext;
	private final EModelService modelService;

	public MenuPartHelper(final IEclipseContext eclipseContext, final EModelService modelService) {
		this.eclipseContext = eclipseContext;
		this.modelService = modelService;
	}

	public IContributionItem createHandledContributionItem(final MHandledItem handledItem) {

		final HandledContributionItem item = new HandledContributionItem();
		ContextInjectionFactory.inject(item, eclipseContext);

		// TODO remove the following three lines after bug #407318 is fixed!!!
		//		final IEclipseContext lclContext = getContext(handledItem);
		//		lclContext.set(EHandlerService.class, null);
		//		handledItem.setEnabled(false);

		item.setModel(handledItem);

		return item;

	}

	private IEclipseContext getContext(final MUIElement part) {
		if (part instanceof MContext) {
			return ((MContext) part).getContext();
		}
		return getContextForParent(part);
	}

	private IEclipseContext getContextForParent(final MUIElement element) {
		return modelService.getContainingContext(element);
	}

}
