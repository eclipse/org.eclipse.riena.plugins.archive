package org.eclipse.riena.e4.launcher.listener;

import org.eclipse.riena.navigation.IModuleGroupNode;
import org.eclipse.riena.navigation.listener.ModuleGroupNodeListener;

/**
 * This listener of a module group ensures the preparation of nodes (if necessary).
 */
public class MyModuleGroupNodeListener extends ModuleGroupNodeListener {

	/**
	 * {@inheritDoc}
	 * <p>
	 * After activation of a module group prepare - if necessary - every child (sub module) node.
	 */
	@Override
	public void activated(final IModuleGroupNode source) {
		MySubApplicationNodeListener.prepare(source);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After the parent of a module group changed prepare - if necessary - every child node.
	 */
	@Override
	public void parentChanged(final IModuleGroupNode source) {
		MySubApplicationNodeListener.prepare(source);
	}

}