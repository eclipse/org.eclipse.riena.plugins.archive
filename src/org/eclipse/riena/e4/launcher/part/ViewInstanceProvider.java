package org.eclipse.riena.e4.launcher.part;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.core.singleton.SingletonProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 *
 */
public class ViewInstanceProvider {

	private static final SingletonProvider<ViewInstanceProvider> UIS = new SingletonProvider<ViewInstanceProvider>(ViewInstanceProvider.class);

	private final Map<String, SubModuleView> views;
	private final Map<String, Composite> composites;

	/**
	 * 
	 */
	public ViewInstanceProvider() {
		views = new HashMap<String, SubModuleView>();
		composites = new HashMap<String, Composite>();
	}

	public SubModuleView getView(final String typeId) {
		return views.get(typeId);
	}

	public Composite getParentComposite(final String typeId) {
		return composites.get(typeId);
	}

	public void registerView(final String typeId, final SubModuleView view) {
		views.put(typeId, view);
	}

	public void registerParentComposite(final String typeId, final Composite parent) {
		composites.put(typeId, parent);
	}

	public void unregisterView(final String typeId) {
		views.remove(typeId);
	}

	public void unregisterParentComposite(final String typeId) {
		composites.remove(typeId);
	}

	public static ViewInstanceProvider getInstance() {
		return UIS.getInstance();
	}

}
