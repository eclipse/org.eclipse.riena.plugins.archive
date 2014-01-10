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
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 * Manages {@link SubModuleView} instances for {@link SubModuleNode}s.
 */
public class ViewInstanceProvider {

	private static final SingletonProvider<ViewInstanceProvider> UIS = new SingletonProvider<ViewInstanceProvider>(ViewInstanceProvider.class);

	private final Map<String, SubModuleView> views;
	private final Map<String, Composite> composites;
	private final Map<String, Integer> viewUsage;

	private ViewInstanceProvider() {
		views = new HashMap<String, SubModuleView>();
		composites = new HashMap<String, Composite>();
		viewUsage = new HashMap<String, Integer>();
	}

	/**
	 * @return the {@link SubModuleView} registered for the given typeId
	 */
	public SubModuleView getView(final String typeId) {
		return views.get(typeId);
	}

	/**
	 * @return the parent Composite of the {@link SubModuleView} registered for the given typeId
	 */
	public Composite getParentComposite(final String typeId) {
		return composites.get(typeId);
	}

	/**
	 * Registers the given {@link SubModuleView} for the given typeId
	 */
	public void registerView(final String typeId, final SubModuleView view) {
		views.put(typeId, view);
		increaseViewCounter(typeId);
	}

	public int increaseViewCounter(final String typeId) {
		Integer count = viewUsage.get(typeId);
		if (null == count) {
			count = 1;
		} else {
			count += 1;
		}
		viewUsage.put(typeId, count);
		return count;
	}

	public int decreaseViewCounter(final String typeId) {
		Integer count = viewUsage.get(typeId);
		if (null == count) {
			count = 1;
		} else {
			count -= 1;
		}
		viewUsage.put(typeId, count);
		return count;
	}

	public void registerParentComposite(final String typeId, final Composite parent) {
		composites.put(typeId, parent);
	}

	public void unregisterView(final String typeId) {
		views.remove(typeId);
	}

	public void unregisterTypeId(final String typeId) {
		unregisterParentComposite(typeId);
		unregisterView(typeId);
	}

	public void unregisterParentComposite(final String typeId) {
		composites.remove(typeId);
	}

	public static ViewInstanceProvider getInstance() {
		return UIS.getInstance();
	}

}
