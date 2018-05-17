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
package org.eclipse.riena.e4.launcher;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.e4.launcher.part.MainMenuPart;
import org.eclipse.riena.e4.launcher.part.MainToolBarPart;
import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;

/**
 * @since 6.0
 *
 */
public class RienaE4MenuUtils {

	private RienaE4MenuUtils() {
		// utility class
	}

	public static CoolBarComposite getCoolBarComposite(final IEclipseContext context) {
		final Composite composite = getComposite(context, E4XMIConstants.MAIN_TOOL_BAR_PART_ID, MainToolBarPart.COOLBAR_COMPOSITE_KEY);
		if (composite instanceof CoolBarComposite) {
			return ((CoolBarComposite) composite);
		} else {
			return null;
		}
	}

	public static MenuCoolBarComposite getMenuCoolBarComposite(final IEclipseContext context) {
		final Composite composite = getComposite(context, E4XMIConstants.MAIN_MENU_PART_ID, MainMenuPart.MENU_COMPOSITE_KEY);
		if (composite instanceof MenuCoolBarComposite) {
			return ((MenuCoolBarComposite) composite);
		} else {
			return null;
		}
	}

	private static Composite getComposite(final IEclipseContext context, final String elementId, final String compositeKey) {
		final EModelService modelService = context.get(EModelService.class);
		final MApplication mApplication = context.get(MApplication.class);
		final MPart menuPart = (MPart) modelService.find(elementId, mApplication);
		if (menuPart == null) {
			return null;
		}
		final Object m = menuPart.getTransientData().get(compositeKey);
		if (m instanceof Composite) {
			return ((Composite) m);
		} else {
			return null;
		}

	}

}
