package org.eclipse.riena.e4.launcher.part;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.NavigationViewPart;
import org.eclipse.riena.navigation.ui.swt.views.ScrollButtonsNavigationCompositeDeligation;
import org.eclipse.riena.navigation.ui.swt.views.ScrollButtonsSupport;

public class NavigationPart {
	@Inject
	private MWindow window;

	@Inject
	private EModelService modelService;

	@Inject
	public void createUI(final Composite parent) {
		new NavigationViewPart() {
			@Override
			public ISubApplicationNode getSubApplicationNode() {
				final MPerspective activePerspective = modelService.getActivePerspective(window);
				final String perspectiveId = activePerspective.getElementId();
				return SwtViewProvider.getInstance().getNavigationNode(perspectiveId, ISubApplicationNode.class);
			};

			/**
			 * Hack for scrolling support in navigation. TODO refactor
			 */
			@Override
			protected org.eclipse.riena.navigation.ui.swt.views.INavigationCompositeDelegation createButtonsNavigationCompositeDelegation(final Composite parent) {
				return new ScrollButtonsNavigationCompositeDeligation(parent.getParent(), parent, this) {

					@Override
					protected org.eclipse.riena.navigation.ui.swt.views.ScrollButtonsSupport createScrollingSupport() {
						return new ScrollButtonsSupport(getSuperParent(), getNavigationProvider()) {
							@Override
							protected org.eclipse.swt.widgets.Shell getActiveShell() {
								return (Shell) window.getWidget();
							};
						};
					};
				};

			};
		}.createPartControl(parent);
	}
}
