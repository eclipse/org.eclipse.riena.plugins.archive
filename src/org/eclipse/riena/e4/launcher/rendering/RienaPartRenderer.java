package org.eclipse.riena.e4.launcher.rendering;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributedPartRenderer;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.e4.launcher.part.RienaPartHelper;
import org.eclipse.riena.e4.launcher.part.ViewInstanceProvider;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 * Riena specific {@link ContributedPartRenderer} honoring shared views. Shared views work by definition of a 1-n relationship between a {@link SubModuleView}
 * and {@link SubModuleNode}s. Every {@link SubModuleNode} is associated to one {@link PartImpl}. Multiple {@link PartImpl}s share one {@link SubModuleView}.
 */
@SuppressWarnings("restriction")
public final class RienaPartRenderer extends ContributedPartRenderer {

	@Override
	public Object createWidget(final MUIElement element, final Object parent) {

		final MApplicationElement part = element;
		final String[] rienaCompoundId = RienaPartHelper.extractRienaCompoundId(part);
		Composite parentComposite = null;

		// is node part?
		if (isSubmoduleNodePart(rienaCompoundId)) {
			final String typeId = rienaCompoundId[0];
			final String secondayId = rienaCompoundId[1];
			final ISubModuleNode node = SwtViewProvider.getInstance().getNavigationNode(typeId, secondayId, ISubModuleNode.class);

			// if the node belongs to a shared view try to lookup the view
			if (RienaPartHelper.isSharedView(node)) {
				parentComposite = ViewInstanceProvider.getInstance().getParentComposite(typeId);
				if (null != parentComposite) {
					// shared view found
					if (null == element.getWidget()) {
						// register the widget at the current element. This happens only one time for each part.
						element.setWidget(parentComposite);
						ViewInstanceProvider.getInstance().increaseViewCounter(typeId);
					}
				}
			}

			// nothing found in cache
			if (null == parentComposite) {
				// create new view
				parentComposite = (Composite) super.createWidget(element, parent);
				// initialize and build view
				initializeView(typeId, parentComposite, node);
			} else {
				// Cached view found! update node in view
				updateViewNode(typeId, node);
			}
			// observe nodes related to shared views
			if (RienaPartHelper.isSharedView(node)) {
				node.addListener(new SharedViewNodeBinder(typeId));
			}
		} else {
			// not related to a SubmoduleNode. Nothing special here. Just create composite
			parentComposite = (Composite) super.createWidget(element, parent);
		}

		return parentComposite;

	}

	private boolean isSubmoduleNodePart(final String[] rienaCompoundId) {
		return rienaCompoundId.length > 1;
	}

	/**
	 * Updates the {@link ISubModuleNode} in the {@link SubModuleView}
	 */
	private void updateViewNode(final String typeId, final ISubModuleNode node) {
		final SubModuleView viewInstance = ViewInstanceProvider.getInstance().getView(typeId);
		viewInstance.setNavigationNode(node);
	}

	/**
	 * Initializes and builds the related {@link SubModuleView}. This involves calling {@link SubModuleView#createPartControl(Composite)}
	 * 
	 */
	private void initializeView(final String typeId, final Composite parentComposite, final ISubModuleNode node) {
		final SubModuleView viewInstance = ViewInstanceProvider.getInstance().getView(typeId);
		viewInstance.setE4Runtime(true);
		ViewInstanceProvider.getInstance().registerParentComposite(typeId, parentComposite);
		updateViewNode(typeId, node);
		ReflectionUtils.invokeHidden(viewInstance, "setShellProvider", RienaPartHelper.toShellProvider(parentComposite.getShell())); //$NON-NLS-1$
		viewInstance.createPartControl(parentComposite);
	}

	/**
	 * After activation of a {@link SubModuleNode} this listener binds the corresponding shared view to the node
	 */
	private final class SharedViewNodeBinder extends SubModuleNodeListener {

		private final String typeId;

		private SharedViewNodeBinder(final String typeId) {
			this.typeId = typeId;
		}

		@Override
		public void activated(final ISubModuleNode source) {
			final SubModuleView viewInstance = ViewInstanceProvider.getInstance().getView(typeId);
			viewInstance.setNavigationNode(source);
			viewInstance.bind(source);
		}
	}

}