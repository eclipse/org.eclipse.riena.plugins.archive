/*******************************************************************************
 * Copyright (c) 2007, 2013 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.e4.launcher.listener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.riena.navigation.INavigationNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.ui.workarea.IWorkareaDefinition;
import org.eclipse.riena.ui.workarea.WorkareaManager;

/**
 *
 */
public class PrepareNodeDelegate<C extends INavigationNode<?>> {

	public void prepare(final C node) {
		prepareSubModuleNodes(node);
	}

	/**
	 * Prepares every sub-module whose definition requires preparation.
	 * 
	 * @param node
	 *            navigation node
	 */
	private void prepareSubModuleNodes(final INavigationNode<?> node) {
		if (node == null) {
			return;
		}

		if (node instanceof ISubModuleNode) {
			final ISubModuleNode subModuleNode = (ISubModuleNode) node;
			final IWorkareaDefinition definition = WorkareaManager.getInstance().getDefinition(subModuleNode);
			if ((definition != null) && definition.isRequiredPreparation() && subModuleNode.isCreated()) {
				subModuleNode.prepare();
			}
		}

		/*
		 * The number of children can change while iterating. Only observe the node children !before! the iteration begins. Any child added while iterating will
		 * be handled automatically if preparation is required. Just ensure that there will be no concurrent modification of the children list while iterating
		 * over it. Conclusion is a copy..
		 */
		final List<INavigationNode<?>> children = new ArrayList<INavigationNode<?>>(node.getChildren());
		for (final INavigationNode<?> child : children) {
			prepareSubModuleNodes(child);
		}
	}

}
