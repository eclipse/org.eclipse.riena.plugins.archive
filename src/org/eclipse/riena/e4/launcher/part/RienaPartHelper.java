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

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.e4.launcher.listener.MySubModuleNodeListener;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.ui.workarea.WorkareaManager;

/**
 * Utility class for the connection of {@link PartImpl} to {@link SubModuleNode}s
 */
public class RienaPartHelper {

	public static String[] extractRienaCompoundId(final MApplicationElement part) {
		return part.getElementId().split(MySubModuleNodeListener.COUNTER_DELIMITER)[0].split(MySubModuleNodeListener.COMPOUND_ID_DELIMITER);
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
