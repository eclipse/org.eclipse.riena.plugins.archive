/*******************************************************************************
 * Copyright (c) 2007, 2013 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.e4.launcher.rendering;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.navigation.ApplicationNodeManager;
import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ShellRenderer;
import org.eclipse.riena.navigation.ui.swt.views.ShellPaintListener;
import org.eclipse.riena.ui.swt.InfoFlyout;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;
import org.eclipse.riena.ui.swt.utils.ImageStore;
import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
import org.eclipse.riena.ui.swt.utils.WidgetIdentificationSupport;

/**
 * Handles the Riena application window presentation.
 */
public class ApplicationView {

	/**
	 * Binding IDs
	 */
	private static final String SHELL_BINDING_ID = "applicationWindow"; //$NON-NLS-1$
	private static final String INFO_FLYOUT_BINDING_ID = "infoFlyout"; //$NON-NLS-1$
	/**
	 * System property defining the minimum width of the application window.
	 */
	private static final String PROPERTY_RIENA_APPLICATION_MINIMUM_WIDTH = "riena.application.minimum.width"; //$NON-NLS-1$
	/**
	 * System property defining the minimum height of the application window.
	 */
	private static final String PROPERTY_RIENA_APPLICATION_MINIMUM_HEIGHT = "riena.application.minimum.height"; //$NON-NLS-1$
	/**
	 * System property defining the initial width of the application window.
	 */
	private static final String PROPERTY_RIENA_APPLICATION_WIDTH = "riena.application.width"; //$NON-NLS-1$
	/**
	 * System property defining the initial height of the application window.
	 */
	private static final String PROPERTY_RIENA_APPLICATION_HEIGHT = "riena.application.height"; //$NON-NLS-1$

	private final InjectSwtViewBindingDelegate binding = new InjectSwtViewBindingDelegate();

	private Point applicationSizeMinimum;

	/**
	 * Initializes the given shell.
	 * 
	 * @param shell
	 *            shell to initialize
	 */
	Rectangle initShell(final Shell shell) {
		shell.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.TITLELESS_SHELL_BACKGROUND));
		shell.addPaintListener(new ShellPaintListener());

		final String iconName = ApplicationNodeManager.getApplicationNode().getIcon();
		shell.setImage(ImageStore.getInstance().getImage(iconName));
		shell.setMinimumSize(getApplicationSizeMinimum());

		// prepare shell for binding
		addUIControl(shell, SHELL_BINDING_ID);

		if (getShellRenderer() != null) {
			getShellRenderer().setShell(shell);
		}

		// TODO check if this is the main window. maybe support more then one workbench window.
		WidgetIdentificationSupport.setIdentification(shell);

		return computeDesiredShellBounds(shell.getDisplay());
	}

	/**
	 * @param display
	 * @return
	 */
	private Rectangle computeDesiredShellBounds(final Display display) {
		final Rectangle screenSize = display.getBounds();
		final Point windowSize = initApplicationSize();

		// center
		final int x = (screenSize.width - windowSize.x) / 2;
		final int y = (screenSize.height - windowSize.y) / 2;

		return new Rectangle(x, y, windowSize.x, windowSize.y);
	}

	/**
	 * Reads the two properties for the initial width and the initial height of the application.
	 * 
	 */
	private Point initApplicationSize() {
		int width = Integer.getInteger(PROPERTY_RIENA_APPLICATION_WIDTH, getApplicationSizeMinimum().x);
		if (width < getApplicationSizeMinimum().x) {
			width = getApplicationSizeMinimum().x;
			//			LOGGER.log(LogService.LOG_WARNING, "The initial width of the application is less than the minimum width which is " //$NON-NLS-1$
			//					+ getApplicationSizeMinimum().x);
		}

		int height = Integer.getInteger(PROPERTY_RIENA_APPLICATION_HEIGHT, getApplicationSizeMinimum().y);
		if (height < getApplicationSizeMinimum().y) {
			height = getApplicationSizeMinimum().y;
			//			LOGGER.log(LogService.LOG_WARNING, "The initial height of the application is less than the minimum height which is " //$NON-NLS-1$
			//					+ getApplicationSizeMinimum().y);
		}

		return new Point(width, height);
	}

	void doInitialBinding() {
		final ApplicationController controller = (ApplicationController) ApplicationNodeManager.getApplicationNode().getNavigationNodeController();
		binding.injectAndBind(controller);
		controller.afterBind();
	}

	private Point getApplicationSizeMinimum() {
		if (applicationSizeMinimum == null) {
			initApplicationSizeMinimum();
		}

		return applicationSizeMinimum;
	}

	private void initApplicationSizeMinimum() {
		final int widthMinimum = Integer.getInteger(PROPERTY_RIENA_APPLICATION_MINIMUM_WIDTH, getApplicationDefaultSizeMinimum().x);
		final int heightMinimum = Integer.getInteger(PROPERTY_RIENA_APPLICATION_MINIMUM_HEIGHT, getApplicationDefaultSizeMinimum().y);
		applicationSizeMinimum = new Point(widthMinimum, heightMinimum);
	}

	private Point getApplicationDefaultSizeMinimum() {
		return (Point) LnfManager.getLnf().getSetting(LnfKeyConstants.APPLICATION_MIN_SIZE);
	}

	/**
	 * Returns the renderer of the shell.
	 * 
	 * @return renderer
	 */
	private ShellRenderer getShellRenderer() {
		final ShellRenderer shellRenderer = (ShellRenderer) LnfManager.getLnf().getRenderer(LnfKeyConstants.TITLELESS_SHELL_RENDERER);
		return shellRenderer;
	}

	public void addUIControl(final Object uiElement, final String propertyName) {
		binding.addUIControl(uiElement, propertyName);
	}

	/**
	 * @param parent
	 * @return
	 */
	public InfoFlyout createInfoFlyout(final Composite parent) {
		final InfoFlyout infoFlyout = UIControlsFactory.createInfoFlyout(parent);
		addUIControl(infoFlyout, INFO_FLYOUT_BINDING_ID);
		return infoFlyout;
	}

}
