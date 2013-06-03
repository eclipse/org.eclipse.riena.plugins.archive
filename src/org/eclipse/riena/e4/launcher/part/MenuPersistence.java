package org.eclipse.riena.e4.launcher.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.ui.internal.menus.ControlContributionRegistry;
import org.eclipse.ui.internal.menus.EditorAction;
import org.eclipse.ui.internal.menus.MenuFactoryGenerator;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * <p>
 * This class is not intended for use outside of the <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
final public class MenuPersistence extends RegistryPersistence {

	private final MApplication application;
	private final IEclipseContext appContext;
	private final ArrayList<MenuAdditionCacheEntry> cacheEntries = new ArrayList<MenuAdditionCacheEntry>();
	private final ArrayList<EditorAction> editorActionContributions = new ArrayList<EditorAction>();

	private final ArrayList<MMenuContribution> menuContributions = new ArrayList<MMenuContribution>();
	private final ArrayList<MToolBarContribution> toolBarContributions = new ArrayList<MToolBarContribution>();
	private final ArrayList<MTrimContribution> trimContributions = new ArrayList<MTrimContribution>();

	private final Comparator<IConfigurationElement> comparer = new Comparator<IConfigurationElement>() {
		public int compare(final IConfigurationElement c1, final IConfigurationElement c2) {
			return c1.getContributor().getName().compareToIgnoreCase(c2.getContributor().getName());
		}
	};
	private Pattern contributorFilter;

	/**
	 * @param application
	 * @param appContext
	 */
	public MenuPersistence(final MApplication application, final IEclipseContext appContext) {
		this.application = application;
		this.appContext = appContext;
	}

	public MenuPersistence(final MApplication application, final IEclipseContext appContext, final String filterRegex) {
		this(application, appContext);
		contributorFilter = Pattern.compile(filterRegex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.RegistryPersistence#dispose()
	 */
	@Override
	public void dispose() {
		ControlContributionRegistry.clear();
		application.getMenuContributions().removeAll(menuContributions);
		application.getToolBarContributions().removeAll(toolBarContributions);
		application.getTrimContributions().removeAll(trimContributions);
		menuContributions.clear();
		cacheEntries.clear();
		editorActionContributions.clear();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.tests.workbench.RegistryPersistence#isChangeImportant (org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	@Override
	protected boolean isChangeImportant(final IRegistryChangeEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public void reRead() {
		read();
	}

	@Override
	protected final void read() {
		super.read();

		readAdditions();
		// readActionSets();
		readEditorActions();

		final ArrayList<MMenuContribution> tmp = new ArrayList<MMenuContribution>(menuContributions);
		menuContributions.clear();
		ContributionsAnalyzer.mergeContributions(tmp, menuContributions);
		application.getMenuContributions().addAll(menuContributions);

		final ArrayList<MToolBarContribution> tmpToolbar = new ArrayList<MToolBarContribution>(toolBarContributions);
		toolBarContributions.clear();
		ContributionsAnalyzer.mergeToolBarContributions(tmpToolbar, toolBarContributions);
		application.getToolBarContributions().addAll(toolBarContributions);

		final ArrayList<MTrimContribution> tmpTrim = new ArrayList<MTrimContribution>(trimContributions);
		trimContributions.clear();
		ContributionsAnalyzer.mergeTrimContributions(tmpTrim, trimContributions);
		application.getTrimContributions().addAll(trimContributions);
	}

	private void readAdditions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		final IConfigurationElement[] menusExtensionPoint = registry.getConfigurationElementsFor(EXTENSION_MENUS);

		// Create a cache entry for every menu addition;
		for (final IConfigurationElement element : menusExtensionPoint) {
			if (PL_MENU_CONTRIBUTION.equals(element.getName())) {
				if (contributorFilter == null || contributorFilter.matcher(element.getContributor().getName()).matches()) {
					configElements.add(element);
				}
			}
		}
		Collections.sort(configElements, comparer);
		final Iterator<IConfigurationElement> i = configElements.iterator();
		while (i.hasNext()) {
			final IConfigurationElement configElement = i.next();

			if (isProgramaticContribution(configElement)) {
				final MenuFactoryGenerator gen = new MenuFactoryGenerator(application, appContext, configElement,
						configElement.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI));
				gen.mergeIntoModel(menuContributions, toolBarContributions, trimContributions);
			} else {
				final MenuAdditionCacheEntry menuContribution = new MenuAdditionCacheEntry(application, appContext, configElement,
						configElement.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI), configElement.getNamespaceIdentifier());
				cacheEntries.add(menuContribution);
				menuContribution.mergeIntoModel(menuContributions, toolBarContributions, trimContributions);
			}
		}
	}

	private void readEditorActions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		final IConfigurationElement[] extElements = registry.getConfigurationElementsFor(IWorkbenchRegistryConstants.EXTENSION_EDITOR_ACTIONS);
		for (final IConfigurationElement element : extElements) {
			if (contributorFilter == null || contributorFilter.matcher(element.getContributor().getName()).matches()) {
				configElements.add(element);
			}
		}

		Collections.sort(configElements, comparer);

		for (final IConfigurationElement element : configElements) {
			for (final IConfigurationElement child : element.getChildren()) {
				if (child.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION)) {
					final EditorAction editorAction = new EditorAction(application, appContext, element, child);
					editorActionContributions.add(editorAction);
					editorAction.addToModel(menuContributions, toolBarContributions, trimContributions);
				}
			}
		}
	}

	private boolean isProgramaticContribution(final IConfigurationElement menuAddition) {
		return menuAddition.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) != null;
	}
}
