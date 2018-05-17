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

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.navigation.ApplicationNodeManager;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
import org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget;
import org.eclipse.riena.ui.ridgets.IStatuslineRidget;
import org.eclipse.riena.ui.swt.DefaultStatuslineContentFactory;
import org.eclipse.riena.ui.swt.GrabCorner;
import org.eclipse.riena.ui.swt.IStatusLineContentFactory;
import org.eclipse.riena.ui.swt.Statusline;
import org.eclipse.riena.ui.swt.StatuslineSpacer;
import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

/**
 * Creates the Riena status line.
 * 
 * @author jdu
 * @since 6.1
 * 
 */
public class StatusLinePart {
	public static final int BOTTOM_OFFSET = 3;

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	public void create(final Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FormLayout());

		GrabCorner grabCorner = null;

		if (org.eclipse.riena.ui.swt.GrabCorner.isResizeable() && LnfManager.getLnf().getBooleanSetting(LnfKeyConstants.SHELL_HIDE_OS_BORDER)) {
			grabCorner = new GrabCorner(c, SWT.DOUBLE_BUFFERED, true);
			final FormData layoutData = (FormData) grabCorner.getLayoutData();
			layoutData.right.offset = 0;
			layoutData.bottom.offset = 0;
		}
		final Statusline statusLine = createStatusLine(c, grabCorner);
		final IStatuslineRidget statusLineRidget = addUIControl(statusLine, "statusline"); //$NON-NLS-1$

		// set the active node (if any) to the status line
		// TODO is this really part of Riena? very similar code can be found in SwtExampleApplication
		final ISubModuleNode activeSubModuleNode = ApplicationNodeManager.locateActiveSubModuleNode();
		final IStatuslineNumberRidget numberRidget = statusLineRidget.getStatuslineNumberRidget();
		if (activeSubModuleNode != null && numberRidget != null) {
			numberRidget.setNumberString(activeSubModuleNode.getLabel());
		}
	}

	private Statusline createStatusLine(final Composite parent, final Composite grabCorner) {
		//		final IStatusLineContentFactory statusLineFactory = getStatuslineContentFactory(); // TODO from extension point
		final IStatusLineContentFactory statusLineFactory = getStatusLineContentFactory();
		final Statusline statusLine = new Statusline(parent, SWT.None, StatuslineSpacer.class, statusLineFactory);
		final FormData fd = new FormData();
		//		final Rectangle navigationBounds = TitlelessStackPresentation.calcNavigationBounds(parent);
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		if (grabCorner != null) {
			fd.right = new FormAttachment(grabCorner, 0);
		} else {
			fd.right = new FormAttachment(100, 0);
		}
		fd.bottom = new FormAttachment(100, -BOTTOM_OFFSET);
		statusLine.setLayoutData(fd);

		LnFUpdater.getInstance().updateUIControls(statusLine, true);
		return statusLine;
	}

	private IStatuslineRidget addUIControl(final Statusline statusLine, final String bindingId) {
		final InjectSwtViewBindingDelegate binding = new InjectSwtViewBindingDelegate();
		binding.addUIControl(statusLine, bindingId);
		final ApplicationController applicationController = (ApplicationController) ApplicationNodeManager.getApplicationNode().getNavigationNodeController();
		binding.injectAndBind(applicationController);
		return applicationController.getStatusline();
	}

	/**
	 * TODO use riena injection mechanism
	 * 
	 * @return
	 */
	private IStatusLineContentFactory getStatusLineContentFactory() {
		final IConfigurationElement[] extensions = extensionRegistry.getConfigurationElementsFor("org.eclipse.riena.navigation.ui.swt.statusLine");
		if (extensions.length == 0) {
			return new DefaultStatuslineContentFactory();
		} else {
			try {
				return (IStatusLineContentFactory) extensions[0].createExecutableExtension("factory");
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new DefaultStatuslineContentFactory();
	}
}
