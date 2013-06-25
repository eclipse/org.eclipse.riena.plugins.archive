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
package org.eclipse.riena.e4.launcher.listener;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Display;

import org.eclipse.riena.navigation.IApplicationNode;
import org.eclipse.riena.navigation.listener.ApplicationNodeListener;
import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer;
import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer.ILoginExecutor;

/**
 * This listener starts the LoginNonActivityTimer after the application was activated.
 */
public class NonActivityApplicationNodeListener extends ApplicationNodeListener {

	@Inject
	private ILoginExecutor<Integer> loginExecutor;

	@Override
	public void afterActivated(final IApplicationNode source) {
		if (loginExecutor != null && loginExecutor.getNonActivityDuration() > 0) {
			startNonActivityTimer();
		}
	}

	private void startNonActivityTimer() {
		new LoginNonActivityTimer(Display.getCurrent(), loginExecutor, loginExecutor.getNonActivityDuration()).schedule();
	}

}
