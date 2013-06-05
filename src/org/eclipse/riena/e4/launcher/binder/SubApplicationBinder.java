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
package org.eclipse.riena.e4.launcher.binder;

import java.util.Map;

import javax.inject.Inject;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.ISources;

import org.eclipse.riena.e4.launcher.RienaE4MenuUtils;
import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.e4.launcher.rendering.RienaWBWRenderer;
import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.UIProcessRidget;
import org.eclipse.riena.navigation.IModuleGroupNode;
import org.eclipse.riena.navigation.IModuleNode;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.NavigationNodeId;
import org.eclipse.riena.navigation.listener.SubApplicationNodeListener;
import org.eclipse.riena.navigation.ui.controllers.SubApplicationController;
import org.eclipse.riena.navigation.ui.swt.binding.DelegatingRidgetMapper;
import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.RienaMenuHelper;
import org.eclipse.riena.navigation.ui.swt.views.SubApplicationView;
import org.eclipse.riena.ui.ridgets.controller.IController;
import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
import org.eclipse.riena.ui.swt.uiprocess.UIProcessControl;

/**
 * Creates and binds an instance of {@link SubApplicationController}. This is a replacement for riena {@link SubApplicationView}. Moreover {@link #bind()}
 * honors the possibility of a non existing {@link Shell}. For that case the {@link IEventBroker} is used. As soon as the {@link Shell} is created by the
 * {@link RienaWBWRenderer} the listener gets notified.
 */
@SuppressWarnings("restriction")
public class SubApplicationBinder {

	@Inject
	private IEclipseContext context;

	@Inject
	private IEventBroker eventBroker;

	private final ISubApplicationNode subApplicationNode;
	private final AbstractViewBindingDelegate binding;
	private final RienaMenuHelper menuBindHelper;
	private final ISourceProviderListener menuSourceProviderListener;
	private SubApplicationListener subApplicationListener;
	private SubApplicationController subApplicationController;

	public SubApplicationBinder(final ISubApplicationNode subApplicationNode) {
		this.subApplicationNode = subApplicationNode;
		binding = createBinding();
		menuBindHelper = new RienaMenuHelper();
		menuSourceProviderListener = new MenuSourceProviderListener();
	}

	protected AbstractViewBindingDelegate createBinding() {
		final DelegatingRidgetMapper ridgetMapper = new DelegatingRidgetMapper(SwtControlRidgetMapper.getInstance());
		ridgetMapper.addMapping(UIProcessControl.class, UIProcessRidget.class);
		return new InjectSwtViewBindingDelegate(ridgetMapper);
	}

	private void createUIProcessControl(final Shell shell) {
		final UIProcessControl uiControl = new UIProcessControl(shell);
		uiControl.setPropertyName("uiProcessRidget"); //$NON-NLS-1$
		binding.addUIControl(uiControl);
	}

	/**
	 * Binds the navigation node to the view. Creates the widgets and the controller if necessary.<br>
	 * Also the menus and the tool bar items are binded.
	 * 
	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org.eclipse.riena.navigation.INavigationNode)
	 */
	public void bind() {
		subApplicationController = new SubApplicationController(subApplicationNode);
		subApplicationController.installNavigationListeners();
		final Object shell = context.get(IServiceConstants.ACTIVE_SHELL);
		if (null == shell) {
			eventBroker.subscribe(RienaWBWRenderer.SHELL_CREATED, new LazyBinder());
			return;
		}
		internalBind(shell);
	}

	/**
	 * Lazy binding for the case that the {@link Shell} is not available yet.
	 */
	private final class LazyBinder implements EventHandler {
		public void handleEvent(final Event event) {
			internalBind(event.getProperty(IEventBroker.DATA));
		}
	}

	private void internalBind(final Object shell) {
		createUIProcessControl((Shell) shell);
		bindController();
	}

	private void bindController() {
		if (subApplicationController.getRidgets().isEmpty()) {
			binding.injectRidgets(subApplicationController);
			binding.bind(subApplicationController);
			observeSubApplicationNode();
			menuBindHelper.addSourceProviderListener(menuSourceProviderListener);
			subApplicationController.afterBind();
		}
	}

	private void observeSubApplicationNode() {
		subApplicationListener = new SubApplicationListener();
		getNavigationNode().addListener(subApplicationListener);
	}

	private class SubApplicationListener extends SubApplicationNodeListener {
		@Override
		public void block(final ISubApplicationNode source, final boolean block) {
			super.block(source, block);
			for (final IModuleGroupNode group : source.getChildren()) {
				for (final IModuleNode module : group.getChildren()) {
					module.setBlocked(block);
				}
			}
		}

		@Override
		public void disposed(final ISubApplicationNode source) {
			unbind();
		}

		@Override
		public void nodeIdChange(final ISubApplicationNode source, final NavigationNodeId oldId, final NavigationNodeId newId) {
			SwtViewProvider.getInstance().replaceNavigationNodeId(source, oldId, newId);
		}
	}

	public void unbind() {
		if (getNavigationNode() != null) {
			getNavigationNode().removeListener(subApplicationListener);
			menuBindHelper.removeSourceProviderListener(menuSourceProviderListener);
			if (getNavigationNode().getNavigationNodeController() instanceof IController) {
				final IController controller = (IController) getNavigationNode().getNavigationNodeController();
				binding.unbind(controller);
			}
		}
	}

	private ISubApplicationNode getNavigationNode() {
		return subApplicationNode;
	}

	private void updateMenuBar() {

		// update main menu items
		final MenuCoolBarComposite menuCoolBarComposite = RienaE4MenuUtils.getMenuCoolBarComposite(context);
		if (menuCoolBarComposite != null) {
			menuCoolBarComposite.updateMenuItems();
		}
		final CoolBarComposite coolBarComposite = RienaE4MenuUtils.getCoolBarComposite(context);
		final IController controller = (IController) getNavigationNode().getNavigationNodeController();
		menuBindHelper.bindMenuAndToolItems(controller, menuCoolBarComposite, coolBarComposite);
	}

	/**
	 * After changing a source the menu bar of this sub-application is updated.
	 */
	private class MenuSourceProviderListener implements ISourceProviderListener {

		/**
		 * Updates the menu bar (only if the priority is correct and this sub-application is selected).
		 * 
		 * @param sourcePriority
		 *            A bit mask of all the source priorities that have changed.
		 */
		private void update(final int sourcePriority) {
			if ((sourcePriority & ISources.ACTIVE_MENU) == ISources.ACTIVE_MENU) {
				if (getNavigationNode().isSelected()) {
					updateMenuBar();
				}
			}
		}

		public void sourceChanged(final int sourcePriority, final Map sourceValuesByName) {
			update(sourcePriority);
		}

		public void sourceChanged(final int sourcePriority, final String sourceName, final Object sourceValue) {
			update(sourcePriority);
		}

	}

}
