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
package org.eclipse.riena.e4.launcher.part;

import java.util.Collections;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceHandler;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.commands.internal.SetEnabled;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.5
 * 
 */
public class E4HandlerProxy implements IHandlerListener, IElementUpdater {
	public HandlerActivation activation = null;
	private final Command command;
	private final IHandler handler;

	public E4HandlerProxy(final Command command, final IHandler handler) {
		this.command = command;
		this.handler = handler;
		handler.addHandlerListener(this);
	}

	@CanExecute
	public boolean canExecute(final IEclipseContext context, @Optional
	final IEvaluationContext staticContext, final MApplication application) {
		if (handler instanceof IHandler2) {
			Object ctx = staticContext;
			if (ctx == null) {
				ctx = new ExpressionContext(application.getContext());
			}
			((IHandler2) handler).setEnabled(ctx);
		}
		return handler.isEnabled();
	}

	@Execute
	public Object execute(final IEclipseContext context, @Optional
	@Named(HandlerServiceImpl.PARM_MAP)
	final Map parms, @Optional
	final Event trigger, @Optional
	final IEvaluationContext staticContext) throws ExecutionException, NotHandledException {
		Activator.trace(Policy.DEBUG_CMDS, "execute " + command + " and " //$NON-NLS-1$ //$NON-NLS-2$
				+ handler + " with: " + context, null); //$NON-NLS-1$
		IEvaluationContext appContext = staticContext;
		if (appContext == null) {
			appContext = new ExpressionContext(context);
		}
		final ExecutionEvent event = new ExecutionEvent(command, parms == null ? Collections.EMPTY_MAP : parms, trigger, appContext);
		if (handler != null && handler.isHandled()) {
			final Object returnValue = handler.execute(event);
			return returnValue;
		}
		return null;
	}

	public IHandler getHandler() {
		return handler;
	}

	public void handlerChanged(final HandlerEvent handlerEvent) {
		final IHandler handler = command.getHandler();
		if (handler instanceof HandlerServiceHandler) {
			//			final IEclipseContext appContext = ((Workbench) PlatformUI.getWorkbench()).getApplication().getContext();
			final IEclipseContext appContext = getWorkbenchContext();
			if (HandlerServiceImpl.lookUpHandler(appContext, command.getId()) == this) {
				((HandlerServiceHandler) handler).fireHandlerChanged(handlerEvent);
			}
		}
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
	 * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui. menus.UIElement, java.util.Map)
	 */
	public void updateElement(final UIElement element, final Map parameters) {
		if (handler instanceof IElementUpdater) {
			((IElementUpdater) handler).updateElement(element, parameters);
		}
	}

	@SetEnabled
	void setEnabled(@Optional
	IEvaluationContext evalContext) {
		if (evalContext == null) {
			//			final IEclipseContext appContext = ((Workbench) PlatformUI.getWorkbench()).getApplication().getContext();
			final IEclipseContext appContext = getWorkbenchContext();
			evalContext = new ExpressionContext(appContext);
		}
		if (handler instanceof IHandler2) {
			((IHandler2) handler).setEnabled(evalContext);
		}
	}
}
