package org.eclipse.riena.e4.launcher.listener;

import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.riena.e4.launcher.RienaE4MenuUtils;
import org.eclipse.riena.e4.launcher.part.RienaPartHelper;
import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.internal.navigation.ui.swt.handlers.NavigationSourceProvider;
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

	private final NavigationSourceProvider navigationSourceProvider = new NavigationSourceProvider();
	private final PrepareNodeDelegate<ISubModuleNode> prepareNodeDelegate = new PrepareNodeDelegate<ISubModuleNode>();

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
		prepareNodeDelegate.prepare(source);
		partHelper.showPart(source);
		updateNavigationSourceProvider(source);
	}

	private void updateNavigationSourceProvider(final ISubModuleNode source) {
		navigationSourceProvider.activeNodeChanged(source);
		for (final Entry<String, Object> e : navigationSourceProvider.getCurrentState().entrySet()) {
			context.set(e.getKey(), e.getValue());
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

		if (source.getNavigationNodeController() instanceof IController) {
			final RienaMenuHelper bindHelper = new RienaMenuHelper();
			bindHelper.bindMenuAndToolItems((IController) source.getNavigationNodeController(), menuCoolBarComposite, coolBarComposite);
		}

	}

	@Override
	public void disposed(final ISubModuleNode source) {
		partHelper.disposeNode(source);
	}

}
