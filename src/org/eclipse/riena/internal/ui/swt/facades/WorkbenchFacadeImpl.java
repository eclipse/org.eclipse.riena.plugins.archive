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
package org.eclipse.riena.internal.ui.swt.facades;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Eclipse 3.x specific implementation.
 */
public class WorkbenchFacadeImpl extends WorkbenchFacade {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.internal.ui.swt.facades.RcpFacade#showView(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IViewReference)
	 */
	@Override
	public void showView(final IWorkbenchPage page, final IViewReference viewRef) {
		// this implementation is not relevant for E4
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade#getActiveShell()
	 */
	@Override
	public Shell getActiveShell() {
		final IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(Activator.getDefault().getContext());

		IEclipseContext root = serviceContext;
		IEclipseContext parent;
		// TODO is there a better way to find the root context?
		while ((parent = serviceContext.getParent()) != null) {
			root = parent;
		}

		return (Shell) root.get(IServiceConstants.ACTIVE_SHELL);
	}

}
