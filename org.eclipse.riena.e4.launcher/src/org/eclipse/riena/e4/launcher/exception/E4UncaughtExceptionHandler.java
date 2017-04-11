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
package org.eclipse.riena.e4.launcher.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import org.osgi.service.log.LogService;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.equinox.log.Logger;

import org.eclipse.riena.core.Log4r;
import org.eclipse.riena.core.exception.IExceptionHandlerManager;
import org.eclipse.riena.core.wire.InjectService;
import org.eclipse.riena.e4.launcher.Activator;

/**
 * Implementation of {@link UncaughtExceptionHandler} forwarding exceptions to the {@link IExceptionHandlerManager} service.
 */
public class E4UncaughtExceptionHandler extends StatusReporter implements UncaughtExceptionHandler {
	private final static Logger LOGGER = Log4r.getLogger(E4UncaughtExceptionHandler.class);

	private IExceptionHandlerManager exceptionHanderManager;

	/**
	 * Installs this instance as global {@link UncaughtExceptionHandler}. Note: This {@link UncaughtExceptionHandler} will be used by all Threads.
	 */
	public E4UncaughtExceptionHandler install() {
		Thread.setDefaultUncaughtExceptionHandler(this);
		return this;
	}

	@InjectService
	public void bind(final IExceptionHandlerManager exceptionHanderManager) {
		this.exceptionHanderManager = exceptionHanderManager;
	}

	public void unbind(final IExceptionHandlerManager exceptionHanderManager) {
		if (this.exceptionHanderManager == exceptionHanderManager) {
			this.exceptionHanderManager = null;
		}
	}

	public void uncaughtException(final Thread thread, final Throwable throwable) {
		handleException(throwable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.IExceptionHandler#handleException(java.lang.Throwable)
	 */
	public void handleException(final Throwable throwable) {
		if (null == exceptionHanderManager) {
			return;
		}
		exceptionHanderManager.handleException(throwable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.statusreporter.StatusReporter#report(org.eclipse.core.runtime.IStatus, int, java.lang.Object[])
	 */
	@Override
	public void report(final IStatus status, final int style, final Object... information) {
		final Throwable throwable = status.getException();
		if (throwable != null) {
			handleException(throwable);
		} else if (!status.isOK()) {
			LOGGER.log(LogService.LOG_WARNING, "Status was not OK, but there was no exception: " + status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.statusreporter.StatusReporter#newStatus(int, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public IStatus newStatus(final int severity, final String message, final Throwable exception) {
		return new Status(severity, Activator.PLUGIN_ID, message, exception);
	}

}
