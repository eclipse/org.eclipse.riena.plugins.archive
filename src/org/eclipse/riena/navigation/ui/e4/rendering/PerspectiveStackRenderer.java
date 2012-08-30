package org.eclipse.riena.navigation.ui.e4.rendering;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleViewRenderer;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

/**
 *
 */
public class PerspectiveStackRenderer extends StackRenderer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer#createWidget(org.eclipse.e4.ui.model.application.ui.MUIElement, java.lang.Object)
	 */
	@Override
	public Object createWidget(final MUIElement element, final Object parent) {
		final Object createWidget = super.createWidget(element, parent);
		final CTabFolder folder = (CTabFolder) createWidget;
		folder.setTabHeight(0);
		folder.setMaximizeVisible(false);
		folder.setMinimizeVisible(false);
		folder.setBorderVisible(false);
		folder.addPaintListener(new BorderPaintListener());
		return createWidget;
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
