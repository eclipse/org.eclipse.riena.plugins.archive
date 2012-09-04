package org.eclipse.riena.e4.launcher.listener;

import org.eclipse.riena.navigation.IModuleNode;
import org.eclipse.riena.navigation.listener.ModuleNodeListener;

/**
 * This listener of a module ensures the preparation of nodes (if necessary).
 */
public class MyModuleNodeListener extends ModuleNodeListener {

	/**
	 * {@inheritDoc}
	 * <p>
	 * After activation of a module prepare - if necessary - every child (sub module) node.
	 */
	@Override
	public void activated(final IModuleNode source) {
		MySubApplicationNodeListener.prepare(source);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After the parent of a module changed prepare - if necessary - every child node.
	 */
	@Override
	public void parentChanged(final IModuleNode source) {
		MySubApplicationNodeListener.prepare(source);
	}

}