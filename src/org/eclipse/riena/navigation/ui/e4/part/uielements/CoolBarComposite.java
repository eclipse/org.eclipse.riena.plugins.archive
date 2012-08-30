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
package org.eclipse.riena.navigation.ui.e4.part.uielements;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.riena.navigation.ui.swt.component.IEntriesProvider;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

public class CoolBarComposite extends Composite {
	private final IEntriesProvider provider;

	public CoolBarComposite(final Composite parent, final IEntriesProvider provider) {
		super(parent, SWT.NONE);
		this.provider = provider;
		final RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.spacing = 0;
		setLayout(layout);

		updateItems();
	}

	public List<ToolItem> updateItems() {
		for (final Control c : getChildren()) {
			c.dispose();
		}

		for (final IContributionItem i : provider.getTopLevelEntries()) {
			if (i instanceof Separator) {
				continue;
			}

			final ToolBar toolbar = new ToolBar(this, SWT.FLAT);
			toolbar.setFont(getToolbarFont());
			final ToolBarManager manager = new ToolBarManager(toolbar);
			manager.add(i);
			manager.update(true);
		}
		return Collections.EMPTY_LIST;
	}

	private static Font getToolbarFont() {
		return LnfManager.getLnf().getFont(LnfKeyConstants.TOOLBAR_FONT);
	}

}
