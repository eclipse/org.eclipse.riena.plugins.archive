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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.navigation.ui.swt.component.IEntriesProvider;
import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;

/**
 * Displays the window main menu
 */
public class MainMenuPart {
	public static final String MENU_COMPOSITE_KEY = MainMenuPart.class.getName() + ".rienaMenuCoolBarComposite"; //$NON-NLS-1$

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	private MApplication application;

	@Inject
	public void create(final Composite parent, final MTrimmedWindow window, final MPart part) {
		final MenuCoolBarComposite menuCoolBarComposite = new MenuCoolBarComposite(parent, SWT.NONE, new IEntriesProvider() {
			public IContributionItem[] getTopLevelEntries() {
				final Map<String, Collection<IContributionItem>> parentIdToElement = new HashMap<String, Collection<IContributionItem>>();

				// e4 specific menus
				final MMenu mainMenu = window.getMainMenu();
				if (mainMenu != null) {
					fill(mainMenu.getChildren(), parentIdToElement, "org.eclipse.ui.main.menu");
				}

				// 3.x specific menus
				final ExpressionContext eContext = new ExpressionContext(eclipseContext.getParent());
				for (final MMenuContribution c : application.getMenuContributions()) {
					if (ContributionsAnalyzer.isVisible(c, eContext)) {
						fill(c.getChildren(), parentIdToElement, c.getParentId());
					}
				}

				setParentChildRelation(parentIdToElement);

				for (final IContributionItem iContributionItem : getOrCreateMapElement(parentIdToElement, "org.eclipse.ui.main.toolbar")) {
					System.err.println(iContributionItem);
				}

				final Collection<IContributionItem> c = getOrCreateMapElement(parentIdToElement, "org.eclipse.ui.main.menu");
				return c.toArray(new IContributionItem[parentIdToElement.size()]);
			}
		});

		part.getTransientData().put(MENU_COMPOSITE_KEY, menuCoolBarComposite);
	}

	/**
	 * fill the given map with the {@link MMenu} items
	 * 
	 * @param idToElement
	 */
	private void fill(final List<MMenuElement> elements, final Map<String, Collection<IContributionItem>> parentIdToElement, final String parentId) {
		//		final List<MMenuElement> elements = source.getChildren();
		for (final MMenuElement e : elements) {
			final String label = e.getLabel();
			final String id = e.getElementId();
			if (e instanceof MMenu) {
				// => MenuManager
				getOrCreateMapElement(parentIdToElement, parentId).add(new MenuManager(label, id));
			} else if (e instanceof MHandledItem) {
				// => CommandContributionItem/ActionContributionItem
				final HandledContributionItem item = new HandledContributionItem();
				ContextInjectionFactory.inject(item, eclipseContext);
				item.setModel((MHandledItem) e);

				getOrCreateMapElement(parentIdToElement, parentId).add(item);
			} else if (e instanceof MMenuSeparator) {
				// => AbstractGroupMarker
				final AbstractGroupMarker separator = new Separator();
				separator.setId(id);
				getOrCreateMapElement(parentIdToElement, parentId).add(separator);
			}
		}
	}

	private Collection<IContributionItem> getOrCreateMapElement(final Map<String, Collection<IContributionItem>> parentIdToElement, final String parentId) {
		Collection<IContributionItem> elements = parentIdToElement.get(parentId);
		if (elements == null) {
			elements = new ArrayList<IContributionItem>();
			parentIdToElement.put(parentId, elements);
		}
		return elements;
	}

	private void setParentChildRelation(final Map<String, Collection<IContributionItem>> parentIdToElement) {
		for (final Entry<String, Collection<IContributionItem>> entry : new HashMap<String, Collection<IContributionItem>>(parentIdToElement).entrySet()) {
			for (final IContributionItem e : entry.getValue()) {
				if (e instanceof IContributionManager) {
					for (final IContributionItem child : getOrCreateMapElement(parentIdToElement, e.getId())) {
						((IContributionManager) e).add(child);
					}
				}
			}
		}
	}
}
