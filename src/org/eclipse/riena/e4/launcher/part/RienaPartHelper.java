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
package org.eclipse.riena.e4.launcher.part;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.e4.launcher.Activator;
import org.eclipse.riena.e4.launcher.E4XMIConstants;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.model.SubModuleNode;
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
	private Logger logger;

	private static int secondaryIdCounter = 0;

	public void showPart(final ISubModuleNode source) {
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

	public static String[] extractRienaCompoundId(final MApplicationElement part) {
		return part.getElementId().split(COUNTER_DELIMITER)[0].split(COMPOUND_ID_DELIMITER);
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
