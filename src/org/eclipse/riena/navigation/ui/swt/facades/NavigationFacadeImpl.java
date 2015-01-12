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
package org.eclipse.riena.navigation.ui.swt.facades;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.application.WorkbenchAdvisor;

import org.eclipse.riena.internal.navigation.ui.swt.IAdvisorHelper;
import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
import org.eclipse.riena.navigation.ui.swt.IApplicationUtility;
import org.eclipse.riena.navigation.ui.swt.views.ModuleNavigationListener;

public class NavigationFacadeImpl extends NavigationFacade {

	@Override
	public void attachModuleNavigationListener(final Tree tree) {
		new ModuleNavigationListener(tree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.navigation.ui.swt.facades.NavigationFacade#createWorkbenchAdvisor(org.eclipse.riena.navigation.ui.controllers.ApplicationController,
	 * org.eclipse.riena.internal.navigation.ui.swt.IAdvisorHelper)
	 */
	@Override
	public WorkbenchAdvisor createWorkbenchAdvisor(final ApplicationController applicationController, final IAdvisorHelper advisorHelper) {
		throw new UnsupportedOperationException("This code should not be executed in Eclipse 4.x"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.navigation.ui.swt.facades.NavigationFacade#getApplicationUtility()
	 */
	@Override
	public IApplicationUtility getApplicationUtility() {
		return new ApplicationUtilityE4();
	}

}
