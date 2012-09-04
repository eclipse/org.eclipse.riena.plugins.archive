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
package org.eclipse.riena.e4.launcher.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.riena.core.exception.IExceptionHandlerManager;
import org.eclipse.riena.core.wire.InjectService;

/**
 * Implementation of {@link UncaughtExceptionHandler} forwarding exceptions to the {@link IExceptionHandlerManager} service.
 */
public class E4UncaughtExceptionHandler implements UncaughtExceptionHandler {

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
		if (null == exceptionHanderManager) {
			return;
		}
		exceptionHanderManager.handleException(throwable);
	}

}
