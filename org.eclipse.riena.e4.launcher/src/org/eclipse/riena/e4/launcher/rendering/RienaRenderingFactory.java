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
package org.eclipse.riena.e4.launcher.rendering;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributedPartRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;

@SuppressWarnings("restriction")
public class RienaRenderingFactory extends WorkbenchRendererFactory {

	@Override
	public AbstractPartRenderer getRenderer(final MUIElement uiElement, final Object parent) {
		if (uiElement instanceof MWindow) {
			final RienaWBWRenderer renderer = new RienaWBWRenderer();
			initRenderer(renderer);
			return renderer;
		}
		if (uiElement instanceof MPartStack) {
			final PerspectiveStackRenderer renderer = new PerspectiveStackRenderer();
			initRenderer(renderer);
			return renderer;
		}
		if (uiElement instanceof MPerspective) {
			final PerspectiveRenderer renderer = new PerspectiveRenderer();
			initRenderer(renderer);
			return renderer;
		}

		if (uiElement instanceof MPart) {
			final ContributedPartRenderer renderer = new RienaPartRenderer();
			initRenderer(renderer);
			return renderer;
		}

		return super.getRenderer(uiElement, parent);
	}

}
