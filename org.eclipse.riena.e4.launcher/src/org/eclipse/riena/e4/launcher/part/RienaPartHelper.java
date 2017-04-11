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

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.e4.launcher.Activator;
import org.eclipse.riena.e4.launcher.E4XMIConstants;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
import org.eclipse.riena.ui.workarea.WorkareaManager;

/**
 * Utility class for the connection of {@link PartImpl} to {@link SubModuleNode}s
 */
@SuppressWarnings("restriction")
public class RienaPartHelper {
	public static final String COMPOUND_ID_DELIMITER = ":"; //$NON-NLS-1$
	public static final String COUNTER_DELIMITER = "#"; //$NON-NLS-1$
	private static final String KEY_E4_PART_ID = "E4PartId"; //$NON-NLS-1$

	@Inject
	private IEclipseContext context;

	@Inject
	private EModelService modelService;

	private static int secondaryIdCounter = 0;

	public void showPart(final ISubModuleNode source) {

		final String partId = (String) source.getContext(KEY_E4_PART_ID);

		MPart partToActivate = null;
		if (null == partId) {
			// not registered yet --> create new part
			partToActivate = createPart(source);
		} else {
			// search for part
			partToActivate = findPart(source);

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

	private MPart findPart(final ISubModuleNode node) {
		final String partId = (String) node.getContext(KEY_E4_PART_ID);
		if (null == partId) {
			return null;
		}
		final List<MPart> parts = modelService.findElements(context.get(MApplication.class), partId, MPart.class, null, EModelService.IN_ANY_PERSPECTIVE);
		return parts.get(0);
	}

	private MPart createPart(final ISubModuleNode source) {
		final ISubApplicationNode subApplicationNode = source.getParentOfType(ISubApplicationNode.class);
		final MElementContainer parent = findStackPart(subApplicationNode);

		// everything ok until here. Now create part instance
		final MPart partToActivate = MBasicFactory.INSTANCE.createPart();
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

	/**
	 * Returns the stack part of the Riena working area for the given sub-application.
	 * 
	 * @param subApplicationNode
	 *            node of the sub-application
	 * @return stack part
	 */
	private MPartStack findStackPart(final ISubApplicationNode subApplicationNode) {

		final String perspectiveId = SwtViewProvider.getInstance().getSwtViewId(subApplicationNode).getId();

		// find perspective to add part to
		final List<MPerspective> perspectives = modelService.findElements(context.get(MApplication.class), perspectiveId, MPerspective.class, null);
		if (perspectives.isEmpty()) {
			// nothing found
			throw new IllegalStateException("Parent perspective not found. perspectiveId: " + perspectiveId); //$NON-NLS-1$ 
		}

		// find stack to add part to
		final MPerspective subApplicationPerspective = perspectives.get(0);
		final List<MPartStack> stacks = modelService.findElements(subApplicationPerspective, E4XMIConstants.CONTENT_PART_STACK_ID, MPartStack.class, null);
		if (stacks.isEmpty()) {
			// nothing found
			throw new IllegalStateException("Part stack not found on parent perspective. perspectiveId: " + perspectiveId); //$NON-NLS-1$
		}

		// we only use one stack per perspective
		final MPartStack parent = stacks.get(0);
		return parent;

	}

	private String createPartId(final ISubModuleNode node) {
		final String basicId = SwtViewProvider.getInstance().getSwtViewId(node).getCompoundId();
		final String fqId = basicId + COUNTER_DELIMITER + increasePartCounter(); // TODO rename fqId ?
		return fqId;
	}

	private String increasePartCounter() {
		return String.valueOf(secondaryIdCounter++);
	}

	public void disposeNode(final ISubModuleNode source) {
		final MPart part = findPart(source);
		if (part != null) {
			final SwtViewId swtViewId = extractRienaCompoundId(part);
			final Composite widget = (Composite) part.getWidget();
			unregisterPart(part);
			final int remainingCount = ViewInstanceProvider.getInstance().decreaseViewCounter(swtViewId);
			// honor reference counter (important for shared views)
			if (remainingCount == 0) {
				// kill zombie widget
				widget.dispose();
				ViewInstanceProvider.getInstance().unregisterParentComposite(swtViewId);
			}
		}
	}

	/**
	 * Unregisters the given {@link MPart} from the e4 application model
	 */
	private void unregisterPart(final MPart part) {
		part.setWidget(null);
		part.getParent().getChildren().remove(part);
	}

	public static SwtViewId extractRienaCompoundId(final MUIElement part) {
		final String[] split = part.getElementId().split(COUNTER_DELIMITER)[0].split(COMPOUND_ID_DELIMITER);
		return new SwtViewId(split[0], split.length > 1 ? split[1] : null);
	}

	/**
	 * 
	 * @return true if the given {@link ISubModuleNode} related to a shared view
	 */
	public static boolean isSharedView(final ISubModuleNode source) {
		return WorkareaManager.getInstance().getDefinition(source).isViewShared();
	}

	/**
	 * 
	 * @return a new instance of {@link IShellProvider} for the give {@link Shell}
	 */
	public static IShellProvider toShellProvider(final Shell shell) {
		return new IShellProvider() {
			public Shell getShell() {
				return shell;
			}
		};
	}

}
