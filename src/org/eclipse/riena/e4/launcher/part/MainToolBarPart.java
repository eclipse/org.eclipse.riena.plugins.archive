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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.e4.launcher.part.uielements.CoolBarComposite;
import org.eclipse.riena.navigation.ui.swt.component.IEntriesProvider;
import org.eclipse.riena.ui.swt.utils.SwtUtilities;

/**
 * Displays the window main tool bar
 */
public class MainToolBarPart {
	public static final String COOLBAR_COMPOSITE_KEY = MainToolBarPart.class.getName() + ".rienaCoolBarComposite"; //$NON-NLS-1$

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	private EModelService modelService;

	@Inject
	private MApplication application;

	@Inject
	public void create(final Composite parent, final MTrimmedWindow window, final MPart part) {
		final CoolBarComposite coolBarComposite = new CoolBarComposite(parent, new IEntriesProvider() {
			public IContributionItem[] getTopLevelEntries() {
				final ExpressionContext eContext = new ExpressionContext(eclipseContext.getParent());
				final ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();

				// the main toolbar id is "org.eclipse.ui.main.toolbar"
				// we need to find its children ids in order to filter only contributions to these children
				final List<String> parents = new ArrayList<String>();
				for (final MTrimContribution c : application.getTrimContributions()) {
					if (ContributionsAnalyzer.isVisible(c, eContext) && "org.eclipse.ui.main.toolbar".equals(c.getParentId())) {
						for (final MTrimElement e : c.getChildren()) {
							if (e instanceof MToolBar) {
								parents.add(e.getElementId());
							}
						}
					}
				}

				final MenuPartHelper helper = new MenuPartHelper(eclipseContext, modelService);

				// now consider only contributions to the parents found above
				// other contributions (e.g. view menu contributions) will be not considered
				final E4ImageReplacer imageReplacer = E4ImageReplacer.getInstance();
				for (final MToolBarContribution c : application.getToolBarContributions()) {
					if (ContributionsAnalyzer.isVisible(c, eContext) && parents.contains(c.getParentId())) {
						for (final MToolBarElement e : c.getChildren()) {
							if (e.getVisibleWhen() instanceof MCoreExpression
									&& !ContributionsAnalyzer.isVisible((MCoreExpression) e.getVisibleWhen(), eContext)) {
								// this element is filtered out
								continue;
							}
							if (e instanceof MHandledItem) {
								// => HandledContributionItem
								final IContributionItem item = helper.createHandledContributionItem((MHandledItem) e);
								if (SwtUtilities.isDpiScalingEnabled()) {
									imageReplacer.replaceImages(null, item);
								}
								items.add(item);
							} else if (e instanceof MToolBarSeparator) {
								// => Separator
								final Separator separator = new Separator();
								separator.setId(e.getElementId());
								items.add(separator);
							}
						}
					}
				}
				return items.toArray(new IContributionItem[items.size()]);
			}
		});
		part.getTransientData().put(COOLBAR_COMPOSITE_KEY, coolBarComposite);
	}

}
