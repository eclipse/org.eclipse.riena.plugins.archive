package org.eclipse.riena.e4.launcher.listener;

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import org.eclipse.riena.e4.launcher.Activator;
import org.eclipse.riena.e4.launcher.E4XMIConstants;
import org.eclipse.riena.e4.launcher.part.MainMenuPart;
import org.eclipse.riena.e4.launcher.part.MainToolBarPart;
import org.eclipse.riena.e4.launcher.part.PartWrapper;
import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.internal.navigation.ui.swt.handlers.NavigationSourceProvider;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubApplicationView;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 * This listener of a sub module ensures the preparation of nodes (if necessary).
 */
@SuppressWarnings("restriction")
public class MySubModuleNodeListener extends SubModuleNodeListener {

	/**
	 * 
	 */
	private static final String KEY_E4_PART_ID = "E4PartId"; //$NON-NLS-1$
	public static final String COMPOUND_ID_DELIMITER = ":"; //$NON-NLS-1$
	public static final String COUNTER_DELIMITER = "#"; //$NON-NLS-1$
	private int secondaryIdCounter = 0;

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
		final EModelService modelService = context.get(EModelService.class);
		final MApplication searchRoot = context.get(MApplication.class);

		final String partId = (String) source.getContext(KEY_E4_PART_ID);

		MPart partToActivate = null;
		if (null == partId) {
			// nothing found --> create new part
			partToActivate = createPart(source, modelService, searchRoot, partToActivate);
		} else {
			// search for part with partId
			final List<MPart> parts = modelService.findElements(searchRoot, partId, MPart.class, null, EModelService.IN_ANY_PERSPECTIVE);
			partToActivate = parts.get(0);

			/** handle shared views **/
			final Object viewInstance = partToActivate.getTransientData().get(PartWrapper.VIEW_KEY);
			if (viewInstance instanceof SubModuleView) {
				((SubModuleView) viewInstance).setNavigationNode(source);
				((SubModuleView) viewInstance).prepareNode(source);
			}
		}
		if (partToActivate == null) {
			throw new IllegalStateException("Part not found, partId: " + partId); //$NON-NLS-1$
		}
		partToActivate.getParent().setSelectedElement(partToActivate);
	}

	private MPart createPart(final ISubModuleNode source, final EModelService modelService, final MApplication searchRoot, MPart partToActivate) {
		final ISubApplicationNode subApplicationNode = source.getParentOfType(ISubApplicationNode.class);
		final String perspectiveId = SwtViewProvider.getInstance().getSwtViewId(subApplicationNode).getId();

		// find perspective to add part to
		final List<MPerspective> perspectives = modelService.findElements(searchRoot, perspectiveId, MPerspective.class, null);
		if (perspectives.isEmpty()) {
			// nothing found
			throw new IllegalStateException("Parent perspective not found. perspectiveId: " + perspectiveId); //$NON-NLS-1$ 
		}

		// find stack to add part to
		final List<MPartStack> stacks = modelService.findElements(perspectives.get(0), E4XMIConstants.CONTENT_PART_STACK_ID, MPartStack.class, null);
		if (stacks.isEmpty()) {
			// nothing found
			throw new IllegalStateException("Part stack not found on parent perspective. perspectiveId: " + perspectiveId); //$NON-NLS-1$
		}

		// we only use one stack per perspective
		final MElementContainer parent = stacks.get(0);

		// everything ok until here. Now create part instance
		partToActivate = MBasicFactory.INSTANCE.createPart();
		final String partId = createPartId(source);
		partToActivate.setElementId(partId);
		source.setContext(KEY_E4_PART_ID, partId);

		// construct the String as in Application.e4xmi
		final String pluginId = Activator.getDefault().getBundleContext().getBundle().getSymbolicName();
		partToActivate.setContributionURI("bundleclass://" + pluginId + "/" + PartWrapper.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$

		// set parent and add as child
		parent.getChildren().add(partToActivate);
		partToActivate.setParent(parent);
		return partToActivate;
	}

	private String createPartId(final ISubModuleNode node) {
		final String basicId = SwtViewProvider.getInstance().getSwtViewId(node).getCompoundId();
		final String fqId = basicId + COUNTER_DELIMITER + increasePartCounter();
		return fqId;
	}

	private String increasePartCounter() {
		return String.valueOf(secondaryIdCounter++);
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
