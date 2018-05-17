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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceHandler;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.expressions.AndExpression;
import org.eclipse.ui.internal.expressions.WorkbenchWindowExpression;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.EvaluationService;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * @since 3.5
 * 
 */
public class LegacyHandlerService implements IHandlerService {

	private static final String[] SELECTION_VARIABLES = { ISources.ACTIVE_CURRENT_SELECTION_NAME, ISources.ACTIVE_FOCUS_CONTROL_ID_NAME,
			ISources.ACTIVE_FOCUS_CONTROL_NAME, ISources.ACTIVE_MENU_EDITOR_INPUT_NAME, ISources.ACTIVE_MENU_NAME, ISources.ACTIVE_MENU_SELECTION_NAME };

	public final static String LEGACY_H_ID = "legacy::handler::"; //$NON-NLS-1$

	static class HandlerSelectionFunction extends ContextFunction {

		private final String commandId;

		/**
		 * 
		 */
		public HandlerSelectionFunction(final String commandId) {
			this.commandId = commandId;
		}

		@Override
		public Object compute(final IEclipseContext context, final String contextKey) {

			final HashSet<HandlerActivation> activationSet = new HashSet<HandlerActivation>();
			IEclipseContext current = context;
			while (current != null) {
				final List<HandlerActivation> handlerActivations = (List<HandlerActivation>) current.getLocal(LEGACY_H_ID + commandId);
				if (handlerActivations != null) {
					activationSet.addAll(handlerActivations);
				}
				current = current.getParent();
			}

			if (activationSet.isEmpty()) {
				return null;
			}

			HandlerActivation bestActivation = null;

			final ExpressionContext legacyEvalContext = new ExpressionContext(context);

			HandlerActivation conflictBest = null;
			HandlerActivation conflictOther = null;
			for (final HandlerActivation handlerActivation : activationSet) {
				if (!handlerActivation.participating) {
					continue;
				}
				if (handlerActivation.evaluate(legacyEvalContext)) {
					if (bestActivation == null) {
						bestActivation = handlerActivation;
					} else {
						final int comparison = bestActivation.compareTo(handlerActivation);
						if (comparison < 0) {
							bestActivation = handlerActivation;
						} else if (comparison == 0) {
							conflictBest = bestActivation;
							conflictOther = handlerActivation;
						}
					}
				}
			}

			if (bestActivation != null) {
				if (bestActivation == conflictBest) {
					WorkbenchPlugin.log("Conflicting handlers for " + commandId + ": {" //$NON-NLS-1$ //$NON-NLS-2$
							+ conflictBest.getHandler() + "} vs {" //$NON-NLS-1$
							+ conflictOther.getHandler() + "}"); //$NON-NLS-1$
				}
				return bestActivation.proxy;
			}

			return null;
		}
	}

	private static IHandlerActivation systemHandlerActivation;

	public static IHandlerActivation registerLegacyHandler(final IEclipseContext context, final String id, final String cmdId, final IHandler handler,
			final Expression activeWhen) {

		final ECommandService cs = (ECommandService) context.get(ECommandService.class.getName());
		final Command command = cs.getCommand(cmdId);
		final boolean handled = command.isHandled();
		final boolean enabled = command.isEnabled();
		final E4HandlerProxy handlerProxy = new E4HandlerProxy(command, handler);
		final HandlerActivation activation = new HandlerActivation(context, cmdId, handler, handlerProxy, activeWhen);
		addHandlerActivation(activation);
		final EHandlerService hs = context.get(EHandlerService.class);
		hs.activateHandler(cmdId, new HandlerSelectionFunction(cmdId));
		final boolean handledChanged = handled != command.isHandled();
		final boolean enabledChanged = enabled != command.isEnabled();
		if (handledChanged || enabledChanged) {
			// IHandler proxy = command.getHandler();
			// TODO do we need to fire a handler changed event?
		}
		return activation;
	}

	static void addHandlerActivation(final HandlerActivation eActivation) {
		List handlerActivations = (List) eActivation.context.getLocal(LEGACY_H_ID + eActivation.getCommandId());
		if (handlerActivations == null) {
			handlerActivations = new ArrayList();
		} else {
			if (handlerActivations.contains(eActivation)) {
				return;
			}
			handlerActivations = new ArrayList(handlerActivations);
		}
		handlerActivations.add(eActivation);
		// setting this so that we trigger invalidations
		eActivation.context.set(LEGACY_H_ID + eActivation.getCommandId(), handlerActivations);
	}

	static void removeHandlerActivation(final HandlerActivation eActivation) {
		List handlerActivations = (List) eActivation.context.getLocal(LEGACY_H_ID + eActivation.getCommandId());
		if (handlerActivations == null) {
			handlerActivations = new ArrayList();
		} else {
			handlerActivations = new ArrayList(handlerActivations);
		}
		handlerActivations.remove(eActivation);
		// setting this so that we trigger invalidations
		eActivation.context.set(LEGACY_H_ID + eActivation.getCommandId(), handlerActivations);
	}

	private final IEclipseContext eclipseContext;
	private final IEvaluationContext evalContext;
	private Expression defaultExpression = null;

	public LegacyHandlerService(final IEclipseContext context) {
		eclipseContext = context;
		evalContext = new ExpressionContext(eclipseContext);
		final IWorkbenchWindow window = (IWorkbenchWindow) eclipseContext.get(IWorkbenchWindow.class.getName());
		if (window != null) {
			defaultExpression = new WorkbenchWindowExpression(window);
		}
	}

	public LegacyHandlerService(final IEclipseContext context, final Expression defaultExpression) {
		eclipseContext = context;
		evalContext = new ExpressionContext(eclipseContext);
		this.defaultExpression = defaultExpression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse .ui.ISourceProvider)
	 */
	public void addSourceProvider(final ISourceProvider provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org. eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(final ISourceProvider provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		E4Util.message("LegacyHandlerService.dispose: should it do something?"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(org.eclipse.ui .handlers.IHandlerActivation)
	 */
	public IHandlerActivation activateHandler(final IHandlerActivation activation) {
		if (activation == systemHandlerActivation) {
			return activation;
		}
		final HandlerActivation handlerActivation = (HandlerActivation) activation;
		handlerActivation.participating = true;
		addHandlerActivation(handlerActivation);
		return activation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(java.lang.String, org.eclipse.core.commands.IHandler)
	 */
	public IHandlerActivation activateHandler(final String commandId, final IHandler handler) {
		return activateHandler(commandId, handler, defaultExpression, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(java.lang.String, org.eclipse.core.commands.IHandler,
	 * org.eclipse.core.expressions.Expression)
	 */
	public IHandlerActivation activateHandler(final String commandId, final IHandler handler, final Expression expression) {
		return activateHandler(commandId, handler, expression, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(java.lang.String, org.eclipse.core.commands.IHandler,
	 * org.eclipse.core.expressions.Expression, boolean)
	 */
	public IHandlerActivation activateHandler(final String commandId, final IHandler handler, final Expression expression, final boolean global) {
		if (global || defaultExpression == null) {
			return registerLegacyHandler(eclipseContext, commandId, commandId, handler, expression);
		}
		final AndExpression andExpr = new AndExpression();
		andExpr.add(expression);
		andExpr.add(defaultExpression);
		return registerLegacyHandler(eclipseContext, commandId, commandId, handler, andExpr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(java.lang.String, org.eclipse.core.commands.IHandler,
	 * org.eclipse.core.expressions.Expression, int)
	 */
	public IHandlerActivation activateHandler(final String commandId, final IHandler handler, final Expression expression, final int sourcePriorities) {
		return activateHandler(commandId, handler, expression, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#createExecutionEvent(org.eclipse .core.commands.Command, org.eclipse.swt.widgets.Event)
	 */
	public ExecutionEvent createExecutionEvent(final Command command, final Event event) {
		final EvaluationContext legacy = new EvaluationContext(evalContext, evalContext.getDefaultVariable());
		final ExecutionEvent e = new ExecutionEvent(command, Collections.EMPTY_MAP, event, legacy);
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#createExecutionEvent(org.eclipse .core.commands.ParameterizedCommand, org.eclipse.swt.widgets.Event)
	 */
	public ExecutionEvent createExecutionEvent(final ParameterizedCommand command, final Event event) {
		final EvaluationContext legacy = new EvaluationContext(evalContext, evalContext.getDefaultVariable());
		final ExecutionEvent e = new ExecutionEvent(command.getCommand(), command.getParameterMap(), event, legacy);
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#deactivateHandler(org.eclipse .ui.handlers.IHandlerActivation)
	 */
	public void deactivateHandler(final IHandlerActivation activation) {
		// null is not allowed, but some people put it anyway :( see bug 326406
		if (activation != null && activation != systemHandlerActivation) {
			final HandlerActivation eActivation = (HandlerActivation) activation;
			eActivation.participating = false;
			removeHandlerActivation(eActivation);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#deactivateHandlers(java.util. Collection)
	 */
	public void deactivateHandlers(final Collection activations) {
		final Object[] array = activations.toArray();
		// set all activations to not be participating first so that they ignore
		// the upcoming context change events
		for (int i = 0; i < array.length; i++) {
			((HandlerActivation) array[i]).participating = false;
		}

		for (final Object element : array) {
			deactivateHandler((IHandlerActivation) element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#executeCommand(java.lang.String, org.eclipse.swt.widgets.Event)
	 */
	public Object executeCommand(final String commandId, final Event event) throws ExecutionException, NotDefinedException, NotEnabledException,
			NotHandledException {
		final ECommandService cs = eclipseContext.get(ECommandService.class);
		final Command command = cs.getCommand(commandId);
		return executeCommand(ParameterizedCommand.generateCommand(command, null), event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#executeCommand(org.eclipse.core .commands.ParameterizedCommand, org.eclipse.swt.widgets.Event)
	 */
	public Object executeCommand(final ParameterizedCommand command, final Event event) throws ExecutionException, NotDefinedException, NotEnabledException,
			NotHandledException {
		final EHandlerService hs = eclipseContext.get(EHandlerService.class);
		final IEclipseContext staticContext = EclipseContextFactory.create();
		if (event != null) {
			staticContext.set(Event.class, event);
		}
		try {
			final Object rc = hs.executeHandler(command, staticContext);
			final Object obj = staticContext.get(HandlerServiceImpl.HANDLER_EXCEPTION);
			if (obj instanceof ExecutionException) {
				throw (ExecutionException) obj;
			} else if (obj instanceof NotDefinedException) {
				throw (NotDefinedException) obj;
			} else if (obj instanceof NotEnabledException) {
				throw (NotEnabledException) obj;
			} else if (obj instanceof NotHandledException) {
				throw (NotHandledException) obj;
			} else if (obj instanceof Exception) {
				WorkbenchPlugin.log((Exception) obj);
			}
			return rc;
		} catch (final InjectionException e) {
			rethrow(e);
			throw e;
		} finally {
			staticContext.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#executeCommandInContext(org.eclipse .core.commands.ParameterizedCommand, org.eclipse.swt.widgets.Event,
	 * org.eclipse.core.expressions.IEvaluationContext)
	 */
	public Object executeCommandInContext(final ParameterizedCommand command, final Event event, final IEvaluationContext context) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {

		final IHandler handler = command.getCommand().getHandler();
		final boolean enabled = handler.isEnabled();
		IEclipseContext staticContext = null;
		Object defaultVar = null;
		if (context instanceof ExpressionContext) {
			// create a child context so that the primary context doesn't get
			// populated by parameters by the EHS
			staticContext = ((ExpressionContext) context).eclipseContext.createChild();
		} else {
			staticContext = eclipseContext.getActiveLeaf().createChild("snapshotContext"); //$NON-NLS-1$
			if (event != null) {
				staticContext.set(Event.class, event);
			}
			staticContext.set(IEvaluationContext.class, context);
			defaultVar = context.getDefaultVariable();
			if (defaultVar != null && defaultVar != IEvaluationContext.UNDEFINED_VARIABLE) {
				staticContext.set(EvaluationService.DEFAULT_VAR, defaultVar);
			}
		}
		final IEclipseContext lookupContext = staticContext;
		// the IEvaluationContext snapshot is not part of our runtime
		// IEclipseContext hierarchy. In order to work. we need the variables
		// from the snapshot available in the static context.
		populateSnapshot(context, staticContext);
		final EHandlerService hs = lookupContext.get(EHandlerService.class);
		try {
			final Object rc = hs.executeHandler(command, staticContext);
			final Object obj = staticContext.get(HandlerServiceImpl.HANDLER_EXCEPTION);
			if (obj instanceof ExecutionException) {
				throw (ExecutionException) obj;
			} else if (obj instanceof NotDefinedException) {
				throw (NotDefinedException) obj;
			} else if (obj instanceof NotEnabledException) {
				throw (NotEnabledException) obj;
			} else if (obj instanceof NotHandledException) {
				throw (NotHandledException) obj;
			} else if (obj instanceof Exception) {
				WorkbenchPlugin.log((Exception) obj);
			}
			return rc;
		} catch (final InjectionException e) {
			rethrow(e);
			throw e;
		} finally {
			if (handler.isEnabled() != enabled && handler instanceof HandlerServiceHandler) {
				((HandlerServiceHandler) handler).overrideEnabled(enabled);
			}
			staticContext.dispose();
		}
	}

	/**
	 * @param context
	 * @param staticContext
	 */
	private void populateSnapshot(final IEvaluationContext context, final IEclipseContext staticContext) {
		IEvaluationContext ctxPtr = context;
		while (ctxPtr != null && !(ctxPtr instanceof ExpressionContext)) {
			final Map vars = getVariables(ctxPtr);
			if (vars != null) {
				final Iterator i = vars.entrySet().iterator();
				while (i.hasNext()) {
					final Map.Entry entry = (Map.Entry) i.next();
					if (staticContext.getLocal(entry.getKey().toString()) == null) {
						staticContext.set(entry.getKey().toString(), entry.getValue());
					}
				}
			}
			ctxPtr = ctxPtr.getParent();
		}
	}

	private Map getVariables(final IEvaluationContext ctx) {
		final Field vars = getContextVariablesField();
		if (vars != null) {
			try {
				return (Map) vars.get(ctx);
			} catch (final IllegalArgumentException e) {

			} catch (final IllegalAccessException e) {

			}
		}
		return null;
	}

	private static Field contextFVariables = null;

	private static Field getContextVariablesField() {
		if (contextFVariables == null) {
			try {
				contextFVariables = EvaluationContext.class.getField("fVariables"); //$NON-NLS-1$
				contextFVariables.setAccessible(true);
			} catch (final SecurityException e) {

			} catch (final NoSuchFieldException e) {

			}
		}
		return contextFVariables;
	}

	/**
	 * Checks the cause of the provided exception and rethrows the cause instead if it was one of the expected exception types.
	 */
	private void rethrow(final InjectionException e) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		final Throwable cause = e.getCause();
		if (cause instanceof ExecutionException) {
			throw (ExecutionException) cause;
		} else if (cause instanceof NotDefinedException) {
			throw (NotDefinedException) cause;
		} else if (cause instanceof NotEnabledException) {
			throw (NotEnabledException) cause;
		} else if (cause instanceof NotHandledException) {
			throw (NotHandledException) cause;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#createContextSnapshot(boolean)
	 */
	public IEvaluationContext createContextSnapshot(final boolean includeSelection) {
		final IEvaluationContext tmpContext = getCurrentState();
		final IEvaluationContext context = new EvaluationContext(null, IEvaluationContext.UNDEFINED_VARIABLE);

		if (includeSelection) {
			for (final String variable : SELECTION_VARIABLES) {
				copyVariable(context, tmpContext, variable);
			}
		}

		final ISourceProviderService sp = eclipseContext.get(ISourceProviderService.class);
		for (final ISourceProvider provider : sp.getSourceProviders()) {
			final String[] names = provider.getProvidedSourceNames();
			for (final String name : names) {
				if (!isSelectionVariable(name)) {
					copyVariable(context, tmpContext, name);
				}
			}
		}

		return context;
	}

	private boolean isSelectionVariable(final String name) {
		for (final String variable : SELECTION_VARIABLES) {
			if (variable.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private void copyVariable(final IEvaluationContext context, final IEvaluationContext tmpContext, final String var) {
		final Object o = tmpContext.getVariable(var);
		if (o != null) {
			context.addVariable(var, o);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return new EvaluationContext(evalContext, evalContext.getDefaultVariable());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#readRegistry()
	 */
	public void readRegistry() {
		readDefaultHandlers();
		readHandlers();
	}

	private void readHandlers() {
		final IExtensionRegistry registry = (IExtensionRegistry) eclipseContext.get(IExtensionRegistry.class.getName());
		final IExtensionPoint extPoint = registry.getExtensionPoint(IWorkbenchRegistryConstants.EXTENSION_HANDLERS);
		final IConfigurationElement[] elements = extPoint.getConfigurationElements();
		for (final IConfigurationElement configElement : elements) {
			final String commandId = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
			if (commandId == null || commandId.length() == 0) {
				continue;
			}
			final String defaultHandler = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
			if ((defaultHandler == null) && (configElement.getChildren(IWorkbenchRegistryConstants.TAG_CLASS).length == 0)) {
				continue;
			}
			Expression activeWhen = null;
			final IConfigurationElement[] awChildren = configElement.getChildren(IWorkbenchRegistryConstants.TAG_ACTIVE_WHEN);
			if (awChildren.length > 0) {
				final IConfigurationElement[] subChildren = awChildren[0].getChildren();
				if (subChildren.length != 1) {
					Activator.trace(Policy.DEBUG_CMDS, "Incorrect activeWhen element " + commandId, null); //$NON-NLS-1$
					continue;
				}
				final ElementHandler elementHandler = ElementHandler.getDefault();
				final ExpressionConverter converter = ExpressionConverter.getDefault();
				try {
					activeWhen = elementHandler.create(converter, subChildren[0]);
				} catch (final CoreException e) {
					Activator.trace(Policy.DEBUG_CMDS, "Incorrect activeWhen element " + commandId, e); //$NON-NLS-1$
				}
			}
			Expression enabledWhen = null;
			final IConfigurationElement[] ewChildren = configElement.getChildren(IWorkbenchRegistryConstants.TAG_ENABLED_WHEN);
			if (ewChildren.length > 0) {
				final IConfigurationElement[] subChildren = ewChildren[0].getChildren();
				if (subChildren.length != 1) {
					Activator.trace(Policy.DEBUG_CMDS, "Incorrect enableWhen element " + commandId, null); //$NON-NLS-1$
					continue;
				}
				final ElementHandler elementHandler = ElementHandler.getDefault();
				final ExpressionConverter converter = ExpressionConverter.getDefault();
				try {
					enabledWhen = elementHandler.create(converter, subChildren[0]);
				} catch (final CoreException e) {
					Activator.trace(Policy.DEBUG_CMDS, "Incorrect enableWhen element " + commandId, e); //$NON-NLS-1$
				}
			}
			registerLegacyHandler(eclipseContext, commandId, commandId, new HandlerProxy(commandId, configElement, IWorkbenchRegistryConstants.ATT_CLASS,
					enabledWhen, eclipseContext.get(IEvaluationService.class)), activeWhen);
		}
	}

	private void readDefaultHandlers() {
		final IExtensionRegistry registry = (IExtensionRegistry) eclipseContext.get(IExtensionRegistry.class.getName());
		final IExtensionPoint extPoint = registry.getExtensionPoint(IWorkbenchRegistryConstants.EXTENSION_COMMANDS);
		final IConfigurationElement[] elements = extPoint.getConfigurationElements();
		for (final IConfigurationElement configElement : elements) {
			final String id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
			if (id == null || id.length() == 0) {
				continue;
			}
			final String defaultHandler = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_DEFAULT_HANDLER);
			if ((defaultHandler == null) && (configElement.getChildren(IWorkbenchRegistryConstants.TAG_DEFAULT_HANDLER).length == 0)) {
				continue;
			}
			registerLegacyHandler(eclipseContext, id, id, new HandlerProxy(id, configElement, IWorkbenchRegistryConstants.ATT_DEFAULT_HANDLER), null);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerService#setHelpContextId(org.eclipse. core.commands.IHandler, java.lang.String)
	 */
	public void setHelpContextId(final IHandler handler, final String helpContextId) {
		// TODO Auto-generated method stub

	}
}
