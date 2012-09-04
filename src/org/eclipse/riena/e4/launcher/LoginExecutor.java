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
package org.eclipse.riena.e4.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.equinox.app.IApplication;

import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer.ILoginExecutor;

/**
 *
 */
public abstract class LoginExecutor implements ILoginExecutor<Integer> {
	private final List<MWindow> showAfterLogin = new ArrayList<MWindow>();
	private final IEclipseContext eclipseContext;
	private final int nonActivityDuration;

	public LoginExecutor(final IEclipseContext eclipseContext, final int nonActivityDuration) {
		this.nonActivityDuration = nonActivityDuration;
		Assert.isNotNull(eclipseContext);
		this.eclipseContext = eclipseContext;
	}

	public void prePerformLogin() throws Exception {
		showAfterLogin.clear();

		// hide all windows
		final EModelService modelService = eclipseContext.get(EModelService.class);
		final List<MWindow> windows = modelService.findElements(eclipseContext.get(MApplication.class), null, MWindow.class, null);
		for (final MWindow w : windows) {
			if (w.isVisible()) {
				w.setVisible(false);
				showAfterLogin.add(w);
			}
		}
	}

	public void postPerformLogin(final Integer result) throws Exception {
		if (IApplication.EXIT_OK.equals(result)) {
			// show all windows
			for (final MWindow w : showAfterLogin) {
				w.setVisible(true);
			}
			showAfterLogin.clear();
		} else {
			System.exit(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer.ILoginExecutor#getNonActivityDuration()
	 */
	public int getNonActivityDuration() {
		return nonActivityDuration;
	}

}
