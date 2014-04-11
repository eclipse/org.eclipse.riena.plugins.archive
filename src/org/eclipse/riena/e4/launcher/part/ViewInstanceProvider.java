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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.core.singleton.SingletonProvider;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 * Manages {@link SubModuleView} instances for {@link SubModuleNode}s.
 */
public class ViewInstanceProvider {

	private static final SingletonProvider<ViewInstanceProvider> UIS = new SingletonProvider<ViewInstanceProvider>(ViewInstanceProvider.class);

	private final Map<SwtViewId, SubModuleView> views;
	private final Map<SwtViewId, Composite> composites;
	private final Map<SwtViewId, Integer> viewUsage;

	private ViewInstanceProvider() {
		views = new HashMap<SwtViewId, SubModuleView>();
		composites = new HashMap<SwtViewId, Composite>();
		viewUsage = new HashMap<SwtViewId, Integer>();
	}

	/**
	 * @return the {@link SubModuleView} registered for the given typeId
	 */
	public SubModuleView getView(final SwtViewId swtViewId) {
		return views.get(swtViewId);
	}

	/**
	 * @return the parent Composite of the {@link SubModuleView} registered for the given typeId
	 */
	public Composite getParentComposite(final SwtViewId swtViewId) {
		return composites.get(swtViewId);
	}

	/**
	 * Registers the given {@link SubModuleView} for the given typeId
	 */
	public void registerView(final SwtViewId swtViewId, final SubModuleView view) {
		views.put(swtViewId, view);
		increaseViewCounter(swtViewId);
	}

	public int increaseViewCounter(final SwtViewId swtViewId) {
		Integer count = viewUsage.get(swtViewId);
		if (null == count) {
			count = 1;
		} else {
			count += 1;
		}
		viewUsage.put(swtViewId, count);
		return count;
	}

	public int decreaseViewCounter(final SwtViewId swtViewId) {
		Integer count = viewUsage.get(swtViewId);
		if (null == count) {
			count = 1; // <---- why?
		} else {
			count -= 1;
		}
		viewUsage.put(swtViewId, count);
		return count;
	}

	public void registerParentComposite(final SwtViewId swtViewId, final Composite parent) {
		composites.put(swtViewId, parent);
	}

	public void unregisterView(final SwtViewId swtViewId) {
		views.remove(swtViewId);
	}

	public void unregisterTypeId(final SwtViewId swtViewId) {
		unregisterParentComposite(swtViewId);
		unregisterView(swtViewId);
	}

	public void unregisterParentComposite(final SwtViewId swtViewId) {
		composites.remove(swtViewId);
	}

	public static ViewInstanceProvider getInstance() {
		return UIS.getInstance();
	}

}
