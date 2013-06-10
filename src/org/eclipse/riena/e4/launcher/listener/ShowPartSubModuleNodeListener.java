package org.eclipse.riena.e4.launcher.listener;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.osgi.service.log.LogService;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.equinox.log.Logger;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.ISources;

import org.eclipse.riena.core.Log4r;
import org.eclipse.riena.e4.launcher.RienaE4MenuUtils;
import org.eclipse.riena.e4.launcher.part.RienaPartHelper;
import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.internal.navigation.ui.swt.handlers.NavigationSourceProvider;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.views.RienaMenuHelper;
import org.eclipse.riena.navigation.ui.swt.views.SubApplicationView;
import org.eclipse.riena.ui.ridgets.controller.IController;

/**
 * This listener of a sub module ensures the preparation of nodes (if necessary) and shows the part of the sub module.
 */
@SuppressWarnings("restriction")
public class ShowPartSubModuleNodeListener extends SubModuleNodeListener {

	private static final Logger LOGGER = Log4r.getLogger(ShowPartSubModuleNodeListener.class);

	private final PrepareNodeDelegate<ISubModuleNode> prepareNodeDelegate = new PrepareNodeDelegate<ISubModuleNode>();
	private final ISourceProviderListener menuSourceProviderListener = new MenuSourceProviderListener();
	private final RienaMenuHelper menuBindHelper = new RienaMenuHelper();

	private ISubModuleNode subModuleNode;

	@Inject
	private IEclipseContext context;

	@Inject
	private RienaPartHelper partHelper;

	/**
	 * {@inheritDoc}
	 * <p>
	 * After activation of a sub module prepare - if necessary - every child node.
	 */
	@Override
	public void activated(final ISubModuleNode source) {
		subModuleNode = source;
		prepareNodeDelegate.prepare(source);
		partHelper.showPart(source);
		updateNavigationSourceProvider(source);
		updateContextWithSourceProviders();
	}

	@Override
	public void afterDeactivated(final ISubModuleNode source) {
		menuBindHelper.removeSourceProviderListener(menuSourceProviderListener);
		super.afterDeactivated(source);
	}

	private void updateContextWithSourceProviders() {
		final ISourceProvider[] sourceProviders = new RienaMenuHelper().getSourceProviders();
		for (final ISourceProvider sourceProvider : sourceProviders) {
			final Set<Map.Entry<Object, Object>> entrySet = getCurrentState(sourceProvider);
			for (final Entry<Object, Object> entry : entrySet) {
				if (entry.getKey() instanceof String) {
					context.set((String) entry.getKey(), entry.getValue());
				}
			}
		}
	}

	private Set<Map.Entry<Object, Object>> getCurrentState(final ISourceProvider sourceProvider) {
		Map<Object, Object> state = null;
		try {
			state = sourceProvider.getCurrentState();
		} catch (final Exception ex) {
			final String msg = "SourceProvider \"" + sourceProvider.getClass().getSimpleName() + "\" is not supported!"; //$NON-NLS-1$//$NON-NLS-2$
			LOGGER.log(LogService.LOG_WARNING, msg);
		}
		if (state != null) {
			final Set<Map.Entry<Object, Object>> entrySet = state.entrySet();
			return entrySet;
		} else {
			return Collections.emptySet();
		}
	}

	private void updateNavigationSourceProvider(final ISubModuleNode source) {
		final ISourceProvider[] sourceProviders = menuBindHelper.getSourceProviders();
		for (final ISourceProvider sourceProvider : sourceProviders) {
			if (sourceProvider instanceof NavigationSourceProvider) {
				((NavigationSourceProvider) sourceProvider).activeNodeChanged(source);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After the parent of a sub module changed prepare - if necessary - every child node.
	 */
	@Override
	public void parentChanged(final ISubModuleNode source) {
		prepareNodeDelegate.prepare(source);
	}

	/**
	 * Code from {@link SubApplicationView}
	 */
	@Override
	public void afterActivated(final ISubModuleNode source) {
		menuBindHelper.addSourceProviderListener(menuSourceProviderListener);
		updateMenuBars();
	}

	private void updateMenuBars() {
		// update main menu items
		final MenuCoolBarComposite menuCoolBarComposite = RienaE4MenuUtils.getMenuCoolBarComposite(context);
		if (menuCoolBarComposite != null) {
			menuCoolBarComposite.updateMenuItems();
		}

		// update coolbar items
		final CoolBarComposite coolBarComposite = RienaE4MenuUtils.getCoolBarComposite(context);
		if (coolBarComposite != null) {
			coolBarComposite.updateItems();
		}

		if (getSubApplicationNode().getNavigationNodeController() instanceof IController) {
			final RienaMenuHelper bindHelper = new RienaMenuHelper();
			bindHelper.bindMenuAndToolItems((IController) getSubApplicationNode().getNavigationNodeController(), menuCoolBarComposite, coolBarComposite);
		}
	}

	@Override
	public void disposed(final ISubModuleNode source) {
		partHelper.disposeNode(source);
	}

	private ISubApplicationNode getSubApplicationNode() {
		return subModuleNode.getParentOfType(ISubApplicationNode.class);
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
				if (getSubApplicationNode().isSelected()) {
					updateContextWithSourceProviders();
					updateMenuBars();
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
