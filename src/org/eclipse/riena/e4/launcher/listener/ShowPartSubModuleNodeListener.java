package org.eclipse.riena.e4.launcher.listener;

import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import org.eclipse.riena.e4.launcher.E4XMIConstants;
import org.eclipse.riena.e4.launcher.part.MainMenuPart;
import org.eclipse.riena.e4.launcher.part.MainToolBarPart;
import org.eclipse.riena.e4.launcher.part.RienaPartHelper;
import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.internal.navigation.ui.swt.handlers.NavigationSourceProvider;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.views.SubApplicationView;

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
		final EModelService modelService = context.get(EModelService.class);
		// update main menu items
		final MApplication mApplication = context.get(MApplication.class);
		final MPart menuPart = (MPart) modelService.find(E4XMIConstants.MAIN_MENU_PART_ID, mApplication);
		final Object m = menuPart.getTransientData().get(MainMenuPart.MENU_COMPOSITE_KEY);
		if (m instanceof MenuCoolBarComposite) {
			((MenuCoolBarComposite) m).updateMenuItems();
		}

		// update coolbar items
		final MPart coolbarPart = (MPart) modelService.find(E4XMIConstants.MAIN_TOOL_BAR_PART_ID, mApplication);
		final Object c = coolbarPart.getTransientData().get(MainToolBarPart.COOLBAR_COMPOSITE_KEY);
		if (c instanceof CoolBarComposite) {
			((CoolBarComposite) c).updateItems();
		}
	}

	@Override
	public void disposed(final ISubModuleNode source) {
		partHelper.disposeNode(source);
	}

}
