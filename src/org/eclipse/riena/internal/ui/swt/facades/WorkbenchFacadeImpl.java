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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
		final IEclipseContext root = getWorkbenchContext();
		return root == null ? null : (Shell) root.get(IServiceConstants.ACTIVE_SHELL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade#closeWorkbench()
	 */
	@Override
	public boolean closeWorkbench() {
		final IEclipseContext root = getWorkbenchContext();
		return root == null ? false : root.get(IWorkbench.class).close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade#getActiveWindowShell()
	 */
	@Override
	public Shell getActiveWindowShell() {
		final IEclipseContext root = getWorkbenchContext();

		if (root == null) {
			return null;
		}

		final MPart part = (MPart) root.get(IServiceConstants.ACTIVE_PART);
		return part == null ? null : ((Control) part.getWidget()).getShell();
	}

	private IEclipseContext getWorkbenchContext() {
		final org.eclipse.e4.ui.internal.workbench.Activator plugin = org.eclipse.e4.ui.internal.workbench.Activator.getDefault();
		if (plugin == null) {
			return null;
		}
		final IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(plugin.getContext());
		return serviceContext.getActiveLeaf();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade#getWorkbenchDisplay()
	 */
	@Override
	public Display getWorkbenchDisplay() {
		final Shell activeWindowShell = getActiveWindowShell();
		return activeWindowShell == null ? null : activeWindowShell.getDisplay();
	}

}
