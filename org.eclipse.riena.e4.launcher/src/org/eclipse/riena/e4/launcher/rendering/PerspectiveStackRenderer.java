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
package org.eclipse.riena.e4.launcher.rendering;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleViewRenderer;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

/**
 * {@link StackRenderer} with modified {@link CTabFolder}
 */
public class PerspectiveStackRenderer extends StackRenderer {

	@Override
	public Object createWidget(final MUIElement element, final Object parent) {
		final RienaTabFolder folder = new RienaTabFolder((Composite) parent, SWT.BORDER);
		bindWidget(element, folder);
		ReflectionUtils.invokeHidden(this, "addTopRight", folder); //$NON-NLS-1$
		folder.setTabHeight(0);
		folder.setMaximizeVisible(false);
		folder.setMinimizeVisible(false);
		folder.setBorderVisible(false);
		folder.addPaintListener(new BorderPaintListener());
		return folder;
	}

	private class RienaTabFolder extends CTabFolder {

		public RienaTabFolder(final Composite parent, final int style) {
			super(parent, style);
		}

		@Override
		public void setSelection(final int index) {
			super.setSelection(index);
			if (index >= getItemCount()) {
				return;
			}
			final CTabItem selectedItem = getItem(index);
			final Control control = selectedItem.getControl();
			if (null == control) {
				return;
			}
			control.setVisible(true);
			control.setBounds(getClientArea());
			showItem(selectedItem);
			redraw();
		}

	}

	private static class BorderPaintListener implements PaintListener {
		private SubModuleViewRenderer renderer;

		public void paintControl(final PaintEvent e) {
			final SubModuleViewRenderer viewRenderer = getRenderer();
			if (viewRenderer != null) {
				final Rectangle bounds = ((Control) e.widget).getParent().getClientArea();
				viewRenderer.setBounds(bounds);
				viewRenderer.paint(e.gc, null);
			}
		}

		/**
		 * Returns the renderer of the sub-module view.<br>
		 * Renderer renders the border of the sub-module view and not the content of the view.
		 * 
		 * @return renderer of sub-module view
		 */
		private SubModuleViewRenderer getRenderer() {
			if (renderer == null) {
				renderer = (SubModuleViewRenderer) LnfManager.getLnf().getRenderer("SubModuleView.renderer"); //$NON-NLS-1$
			}
			return renderer;
		}
	}
}
