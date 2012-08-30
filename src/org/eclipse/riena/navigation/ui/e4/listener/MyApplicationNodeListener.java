package org.eclipse.riena.navigation.ui.e4.listener;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Display;

import org.eclipse.riena.navigation.IApplicationNode;
import org.eclipse.riena.navigation.listener.ApplicationNodeListener;
import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer;
import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer.ILoginExecutor;
import org.eclipse.riena.ui.filter.IUIFilter;

public class MyApplicationNodeListener extends ApplicationNodeListener {
	@Inject
	private IApplicationNode navigationNode;

	@Inject
	private ILoginExecutor<Integer> loginExecutor;

	@Override
	public void filterAdded(final IApplicationNode source, final IUIFilter filter) {
		show();
	}

	@Override
	public void filterRemoved(final IApplicationNode source, final IUIFilter filter) {
		show();
	}

	@Override
	public void afterActivated(final IApplicationNode source) {
		if (loginExecutor != null && loginExecutor.getNonActivityDuration() > 0) {
			startNonActivityTimer();
		}
	}

	private void startNonActivityTimer() {
		new LoginNonActivityTimer(Display.getCurrent(), loginExecutor, loginExecutor.getNonActivityDuration()).schedule();
	}

	private void show() {
		if (navigationNode == null || navigationNode.isDisposed()) {
			return;
		}
		throw new UnsupportedOperationException("TODO: show view");

	}
}
