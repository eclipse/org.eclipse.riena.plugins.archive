package org.eclipse.riena.e4.launcher.rendering;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.riena.e4.launcher.E4XMIConstants;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

/**
 *
 */
public class PerspectiveRenderer extends SWTPartRenderer {

	private Composite navigationPart;
	private Composite contents;

	@Override
	public Widget createWidget(final MUIElement element, final Object parent) {
		if (!(element instanceof MPerspective) || !(parent instanceof Composite)) {
			return null;
		}

		final Composite c = new Composite((Composite) parent, SWT.NONE);
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.marginRight = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.TITLELESS_SHELL_SUB_MODULE_HORIZONTAL_GAP);
		layout.marginTop = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.TOOLBAR_WORK_AREA_VERTICAL_GAP);
		c.setLayout(layout);

		navigationPart = new Composite(c, SWT.NONE);
		final GridData navigationLayoutData = new GridData(GridData.FILL_VERTICAL);
		navigationLayoutData.widthHint = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.NAVIGATION_WIDTH);
		navigationLayoutData.horizontalIndent = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.TITLELESS_SHELL_NAVIGATION_HORIZONTAL_GAP);
		navigationPart.setLayoutData(navigationLayoutData);
		navigationPart.setLayout(new FillLayout());

		contents = new Composite(c, SWT.NONE);
		final GridData contentsLayoutData = new GridData(GridData.FILL_BOTH);
		contentsLayoutData.horizontalIndent = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.NAVIGATION_SUB_MODULE_GAP);
		contents.setLayoutData(contentsLayoutData);
		contents.setLayout(new FillLayout());

		final IStylingEngine stylingEngine = (IStylingEngine) getContext(element).get(IStylingEngine.SERVICE_NAME);
		stylingEngine.setClassname(c, "perspectiveLayout"); //$NON-NLS-1$

		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#processContents (org.eclipse.e4.ui.model.application.ui.MElementContainer)
	 */
	@Override
	public void processContents(final MElementContainer<MUIElement> container) {
		// TODO Auto-generated method stub
		super.processContents(container);

		final IPresentationEngine renderer = (IPresentationEngine) context.get(IPresentationEngine.class.getName());

		final MPerspective persp = (MPerspective) ((MUIElement) container);
		final Shell shell = ((Composite) persp.getWidget()).getShell();
		for (final MWindow dw : persp.getWindows()) {
			renderer.createGui(dw, shell, persp.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#getUIContainer (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public Object getUIContainer(final MUIElement element) {
		if (E4XMIConstants.NAVIGATION_PART_ID.equals(element.getElementId())) {
			return navigationPart;
		}
		if (E4XMIConstants.CONTENT_PART_STACK_ID.equals(element.getElementId())) {
			return contents;
		}

		return null;
	}
}
