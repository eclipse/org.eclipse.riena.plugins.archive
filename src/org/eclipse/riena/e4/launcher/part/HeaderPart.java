package org.eclipse.riena.e4.launcher.part;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.menus.MenuPersistence;
import org.eclipse.ui.internal.services.EvaluationService;
import org.eclipse.ui.services.IEvaluationService;

import org.eclipse.riena.navigation.ApplicationNodeManager;
import org.eclipse.riena.navigation.model.ApplicationNode;
import org.eclipse.riena.navigation.ui.swt.component.TitleComposite;

/**
 * Creates the Riena header.
 * 
 * @author jdu
 * 
 */
@SuppressWarnings("restriction")
public class HeaderPart {
	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	public void create(final Composite parent, final MTrimmedWindow window, final MPart part) {
		final Composite c = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(c);

		final TitleComposite titleComposite = new TitleComposite(c, (ApplicationNode) ApplicationNodeManager.getApplicationNode());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(titleComposite);

		copyLegacyExtensionsToModel(eclipseContext.getParent(), application);
	}

	/**
	 * this must run once to create application model elements for all contributions to <tt>org.eclipse.ui.menus</tt>
	 */
	private void copyLegacyExtensionsToModel(final IEclipseContext context, final MApplication application) {
		WorkbenchPlugin.getDefault().initializeContext(eclipseContext);
		new MenuPersistence(application, context).reRead();
		context.set(IEvaluationService.class, new EvaluationService(context));
		new LegacyHandlerService(context).readRegistry();
	}

}
