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

import java.net.URI;

import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.menus.CommandContributionItem;

import org.eclipse.riena.core.singleton.SessionSingletonProvider;
import org.eclipse.riena.core.singleton.SingletonProvider;
import org.eclipse.riena.core.util.FileUtils;
import org.eclipse.riena.core.util.StringUtils;
import org.eclipse.riena.navigation.ui.swt.views.ImageReplacer;
import org.eclipse.riena.ui.swt.utils.ImageFileExtension;
import org.eclipse.riena.ui.swt.utils.ImageStore;

/**
 * Class to replace in {@link CommandContributionItem} or {@link HandledContributionItem} not scaled images with scaled images.
 */
public class E4ImageReplacer extends ImageReplacer {

	private final static SingletonProvider<E4ImageReplacer> IR = new SessionSingletonProvider<E4ImageReplacer>(E4ImageReplacer.class);
	private final static String IMAGES_REPLACED_KEY = "e4_image_replacer_images_replaced"; //$NON-NLS-1$

	public static E4ImageReplacer getInstance() {
		return IR.getInstance();
	}

	@Override
	public void replaceImages(final IContributionManager contributionManager, final IContributionItem item) {

		if (item instanceof HandledContributionItem) {
			final MHandledItem handledItem = ((HandledContributionItem) item).getModel();
			replaceImages(handledItem);
		} else {
			super.replaceImages(contributionManager, item);
		}
	}

	/**
	 * Replaces all not scaled images (default, disable) of the given item.
	 * 
	 * @param item
	 *            item of the menu or tool bar
	 */
	private void replaceImages(final MHandledItem item) {

		final Object obj = item.getTransientData().get(IMAGES_REPLACED_KEY);
		if (Boolean.TRUE.equals(obj)) {
			return;
		}

		String iconUri = item.getIconURI();
		iconUri = getReplaceUri(iconUri);
		if (!StringUtils.isEmpty(iconUri)) {
			item.setIconURI(iconUri);
		}

		iconUri = getDisabledIconURI(item);
		iconUri = getReplaceUri(iconUri);
		if (!StringUtils.isEmpty(iconUri)) {
			setDisabledIconURI(item, iconUri);
		}

		item.getTransientData().put(IMAGES_REPLACED_KEY, true);

	}

	/**
	 * Returns the URI of the disabled icon in the given item.
	 * 
	 * @param item
	 *            item of the menu or tool bar
	 * @return URI of the disable icon or {@code null} if the icon does not exists.
	 */
	private String getDisabledIconURI(final MHandledItem item) {
		final Object obj = item.getTransientData().get(IPresentationEngine.DISABLED_ICON_IMAGE_KEY);
		return obj instanceof String ? (String) obj : ""; //$NON-NLS-1$
	}

	/**
	 * Sets the disabled icon for the given item.
	 * 
	 * @param item
	 *            item of the menu or tool bar
	 * @param iconUri
	 *            URI of the disable icon
	 */
	private void setDisabledIconURI(final MItem item, final String iconUri) {
		item.getTransientData().put(IPresentationEngine.DISABLED_ICON_IMAGE_KEY, iconUri);
	}

	private String getReplaceUri(final String iconUri) {

		if (!StringUtils.isEmpty(iconUri)) {
			URI uri = URI.create(iconUri);
			final String imageName = getImageName(uri);
			final String fileExtension = FileUtils.getFileExtension(uri.getPath());
			ImageFileExtension imageFileExtension = ImageFileExtension.getImageFileExtension(fileExtension);
			if (imageFileExtension == null) {
				imageFileExtension = ImageFileExtension.PNG;
			}
			uri = ImageStore.getInstance().getImageUri(imageName, imageFileExtension);
			if (uri != null) {
				return uri.toString();
			}
		}

		return null;

	}

}
