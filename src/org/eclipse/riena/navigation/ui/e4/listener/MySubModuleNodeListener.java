package org.eclipse.riena.navigation.ui.e4.listener;

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import org.eclipse.riena.internal.navigation.ui.swt.handlers.NavigationSourceProvider;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
import org.eclipse.riena.navigation.ui.e4.Activator;
import org.eclipse.riena.navigation.ui.e4.E4XMIConstants;
import org.eclipse.riena.navigation.ui.e4.part.MainMenuPart;
import org.eclipse.riena.navigation.ui.e4.part.MainToolBarPart;
import org.eclipse.riena.navigation.ui.e4.part.PartWrapper;
import org.eclipse.riena.navigation.ui.e4.part.uielements.CoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubApplicationView;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
import org.eclipse.riena.ui.workarea.WorkareaManager;

/**
 * This listener of a sub module ensures the preparation of nodes (if necessary).
 */
@SuppressWarnings("restriction")
public class MySubModuleNodeListener extends SubModuleNodeListener {
	private static final String SECONDARY_ID = "org.eclipse.riena.secondary.viewId"; //$NON-NLS-1$
	private static final String VIEWS_EXT_POINT = "org.eclipse.ui.views"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$

	private final NavigationSourceProvider navigationSourceProvider = new NavigationSourceProvider();

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger logger;

	/**
	 * {@inheritDoc}
	 * <p>
	 * After activation of a sub module prepare - if necessary - every child node.
	 */
	@Override
	public void activated(final ISubModuleNode source) {
		MySubApplicationNodeListener.prepare(source);
		showPart(source);
		updateNavigationSourceProvider(source);
	}

	private void showPart(final ISubModuleNode source) {
		final SwtViewId swtViewId = SwtViewProvider.getInstance().getSwtViewId(source);
		final String partId = swtViewId.getId();
		final EModelService modelService = context.get(EModelService.class);
		final MApplication searchRoot = context.get(MApplication.class);
		final List<MPart> parts = modelService.findElements(searchRoot, partId, MPart.class, null, EModelService.IN_ANY_PERSPECTIVE);
		final Object parameter = swtViewId.getSecondary();

		MPart partToActivate = null;
		if (parts.isEmpty()) {
			partToActivate = createPartToActivate(source, partId, modelService, searchRoot, partToActivate);
		} else {
			for (final MPart p : parts) {
				final Object pp = p.getTransientData().get(SECONDARY_ID);
				if (pp == null && parameter == null || pp != null && pp.equals(parameter)) {
					partToActivate = p;
					break;
				}
			}

			if (partToActivate != null) {
				/** handle shared views **/
				final Object viewInstance = partToActivate.getTransientData().get(PartWrapper.VIEW_KEY);
				if (viewInstance instanceof SubModuleView) {
					((SubModuleView) viewInstance).setNavigationNode(source);
					((SubModuleView) viewInstance).prepareNode(source);
				}
			} else {
				partToActivate = createPartToActivate(source, partId, modelService, searchRoot, partToActivate);
			}
		}
		if (partToActivate == null) {
			throw new IllegalStateException("Part not found, partId: " + partId); //$NON-NLS-1$
		}

		partToActivate.getTransientData().put(SECONDARY_ID, parameter);
		partToActivate.getParent().setSelectedElement(partToActivate);
	}

	/**
	 * @param source
	 * @param partId
	 * @param modelService
	 * @param searchRoot
	 * @param partToActivate
	 * @return
	 */
	private MPart createPartToActivate(final ISubModuleNode source, final String partId, final EModelService modelService, final MApplication searchRoot,
			MPart partToActivate) {
		final ISubApplicationNode subApplicationNode = source.getParentOfType(ISubApplicationNode.class);
		final String perspectiveId = SwtViewProvider.getInstance().getSwtViewId(subApplicationNode).getId();
		final List<MPerspective> perspectives = modelService.findElements(searchRoot, perspectiveId, MPerspective.class, null);
		if (perspectives.isEmpty()) {
			throw new IllegalStateException("Parent perspective not found. partId: " + partId + ", perspectiveId: " + perspectiveId); //$NON-NLS-1$ //$NON-NLS-2$
		}
		final List<MPartStack> stacks = modelService.findElements(perspectives.get(0), E4XMIConstants.CONTENT_PART_STACK_ID, MPartStack.class, null);
		if (stacks.isEmpty()) {
			throw new IllegalStateException("Part stack not found on parent perspective. partId: " + partId + ", perspectiveId: " + perspectiveId); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final MElementContainer parent = stacks.get(0);
		try {
			partToActivate = createFromPluginXmlContribution(partId);

			// construct the String as in Application.e4xmi
			final String pluginId = Activator.getDefault().getBundleContext().getBundle().getSymbolicName();
			partToActivate.setContributionURI("bundleclass://" + pluginId + "/" + PartWrapper.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$

			// set parent and add as child
			parent.getChildren().add(partToActivate);
			partToActivate.setParent(parent);
		} catch (final CoreException e) {
			logger.error(e);
		}

		// remove the part from the E4 model when the riena navigation node is disposed
		final MPart toRemoveOnDispose = partToActivate;
		source.addListener(new SubModuleNodeListener() {
			@Override
			public void afterDisposed(final ISubModuleNode source) {
				if (isSharedView(source)) {
					toRemoveOnDispose.getTransientData().remove(SECONDARY_ID);
				} else {
					parent.getChildren().remove(toRemoveOnDispose);
				}
			}
		});
		return partToActivate;
	}

	/**
	 * @param source
	 * @return
	 */
	private boolean isSharedView(final ISubModuleNode source) {
		return WorkareaManager.getInstance().getDefinition(source).isViewShared();
	}

	private void updateNavigationSourceProvider(final ISubModuleNode source) {
		navigationSourceProvider.activeNodeChanged(source);
		for (final Entry<String, Object> e : navigationSourceProvider.getCurrentState().entrySet()) {
			context.set(e.getKey(), e.getValue());
		}
	}

	private MPart createFromPluginXmlContribution(final String partId) throws CoreException {
		final IExtensionRegistry extensionRegistry = context.get(IExtensionRegistry.class);
		for (final IConfigurationElement e : extensionRegistry.getConfigurationElementsFor(VIEWS_EXT_POINT)) {
			if (partId.equals(e.getAttribute(ID))) {
				final MPart part = MBasicFactory.INSTANCE.createPart();
				part.setElementId(partId);
				return part;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After the parent of a sub module changed prepare - if necessary - every child node.
	 */
	@Override
	public void parentChanged(final ISubModuleNode source) {
		MySubApplicationNodeListener.prepare(source);
	}

	/**
	 * Code from {@link SubApplicationView}
	 */
	@Override
	public void afterActivated(final ISubModuleNode source) {
		final EModelService modelService = context.get(EModelService.class);
		// update main menu items
		final Object m = ((MPart) modelService.find(E4XMIConstants.MAIN_MENU_PART_ID, context.get(MApplication.class))).getTransientData().get(
				MainMenuPart.MENU_COMPOSITE_KEY);
		if (m instanceof MenuCoolBarComposite) {
			((MenuCoolBarComposite) m).updateMenuItems();
		}

		// update coolbar items
		final Object c = ((MPart) modelService.find(E4XMIConstants.MAIN_TOOL_BAR_PART_ID, context.get(MApplication.class))).getTransientData().get(
				MainToolBarPart.COOLBAR_COMPOSITE_KEY);
		if (c instanceof CoolBarComposite) {
			((CoolBarComposite) c).updateItems();
		}
	}
}
