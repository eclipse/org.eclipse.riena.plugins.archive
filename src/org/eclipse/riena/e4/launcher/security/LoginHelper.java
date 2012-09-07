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
package org.eclipse.riena.e4.launcher.security;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.riena.navigation.ui.login.ILoginDialogView;
import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer.ILoginExecutor;
import org.eclipse.riena.navigation.ui.swt.splashHandlers.AbstractLoginSplashHandler;

/**
 * Helper class for login processing. Registers a global instance of {@link ILoginExecutor}.
 */
public class LoginHelper {

	private static final String ATTRIBUTE_NON_ACTIVITY_DURATION = "nonActivityDuration"; //$NON-NLS-1$

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	private Logger logger;

	@Inject
	private IEclipseContext eclipseContext;

	/**
	 * @return <code>null</code> if there is no configured login extension
	 */
	public ILoginExecutor<Integer> checkLogin() {
		ILoginExecutor<Integer> loginExecutor = null;
		try {
			loginExecutor = createLoginExecutor();
		} catch (final CoreException e) {
			// we will not start the application if something with the login executor went wrong
			logger.error(e);
			System.exit(0);
		}
		if (loginExecutor != null && !IApplication.EXIT_OK.equals(loginExecutor.performLogin())) {
			// login was not successful
			System.exit(0);
		}
		return loginExecutor;
	}

	private ILoginExecutor<Integer> createLoginExecutor() throws CoreException {
		final IConfigurationElement[] loginSplashExtensions = extensionRegistry
				.getConfigurationElementsFor("org.eclipse.riena.navigation.ui.swt.loginSplashViewDefinition"); //$NON-NLS-1$
		if (loginSplashExtensions.length > 0) {
			return loginExecutorForSplash(loginSplashExtensions[0]);
		}

		final IConfigurationElement[] loginExtensions = extensionRegistry
				.getConfigurationElementsFor("org.eclipse.riena.navigation.ui.loginDialogViewDefinition"); //$NON-NLS-1$
		if (loginExtensions.length > 0) {
			return loginExecutorForDialog(loginExtensions[0]);
		}

		return null;
	}

	/**
	 * Creates a new instance of {@link ILoginExecutor} for default login view without splash
	 */
	private ILoginExecutor<Integer> loginExecutorForDialog(final IConfigurationElement iConfigurationElement) throws CoreException {
		final String attribute = iConfigurationElement.getAttribute(ATTRIBUTE_NON_ACTIVITY_DURATION);
		final ILoginDialogView loginDialogView = (ILoginDialogView) iConfigurationElement.createExecutableExtension("viewClass"); //$NON-NLS-1$

		return new LoginExecutor(eclipseContext, Integer.parseInt(attribute)) {
			public Integer performLogin() {
				final Realm realm = SWTObservables.getRealm(Display.getCurrent());
				do {
					Realm.runWithDefault(realm, new Runnable() {
						public void run() {
							loginDialogView.build();
						}
					});
				} while (IApplication.EXIT_RESTART.equals(loginDialogView.getResult()));
				return loginDialogView.getResult();
			}
		};
	}

	/**
	 * Creates a new instance of {@link ILoginExecutor} for splash views
	 */
	private ILoginExecutor<Integer> loginExecutorForSplash(final IConfigurationElement iConfigurationElement) throws CoreException {
		final String attribute = iConfigurationElement.getAttribute(ATTRIBUTE_NON_ACTIVITY_DURATION);

		return new LoginExecutor(eclipseContext, Integer.parseInt(attribute)) {
			public Integer performLogin() {
				final AbstractLoginSplashHandler loginSplashHandler = getLoginSplashHandler();
				final Realm realm = SWTObservables.getRealm(Display.getCurrent());
				Realm.runWithDefault(realm, new Runnable() {
					public void run() {
						final Shell shell = new Shell(getDisplay(), SWT.NO_TRIM | SWT.APPLICATION_MODAL);
						initializeShellBackgroundImage(shell, getBackgroundImagePath());
						loginSplashHandler.init(shell);
						shell.setLocation((shell.getDisplay().getBounds().width - shell.getSize().x) / 2,
								(shell.getDisplay().getBounds().height - shell.getSize().y) / 2);
						shell.open();
						while (!shell.isDisposed()) {
							if (!getDisplay().readAndDispatch()) {
								getDisplay().sleep();
							}
						}
					}
				});

				return loginSplashHandler.getResult();
			}

			private void initializeShellBackgroundImage(final Shell shell, final String imageName) {
				final ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						((RegistryContributor) iConfigurationElement.getContributor()).getActualName(), getBackgroundImagePath());
				final Image image = imageDescriptor.createImage();
				shell.setSize(image.getBounds().width, image.getBounds().height);
				shell.setBackgroundImage(image);
			}

			private AbstractLoginSplashHandler getLoginSplashHandler() {
				return new AbstractLoginSplashHandler() {
				};
			}

			private String getBackgroundImagePath() {
				return "splash.bmp"; //$NON-NLS-1$
			}

			private Display getDisplay() {
				return Display.getCurrent();
			}
		};
	}

}
