package org.eclipse.riena.e4.launcher.part;

import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.graphics.Point;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.navigation.ui.swt.views.ImageReplacer;
import org.eclipse.riena.ui.swt.lnf.LnfManager;
import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
import org.eclipse.riena.ui.swt.utils.SwtUtilities;

/**
 * Tests of the class {@link E4ImageReplacer}.
 */
public class E4ImageReplacerTest extends TestCase {

	/**
	 * Tests the method {@code getInstance()}.
	 * 
	 * @throws Exception
	 *             handled by JUnit
	 */
	public void testGetInstance() throws Exception {

		final E4ImageReplacer replacer = E4ImageReplacer.getInstance();
		assertNotNull(replacer);

		assertSame(replacer, E4ImageReplacer.getInstance());

		assertNotSame(replacer, ImageReplacer.getInstance());

	}

	/**
	 * Tests the <i>private</i> method {@code getDisabledIconURI}.
	 * 
	 * @throws Exception
	 *             handled by JUnit
	 */
	public void testGetDisabledIconURI() throws Exception {

		final E4ImageReplacer replacer = E4ImageReplacer.getInstance();

		final MHandledItem item = MMenuFactory.INSTANCE.createHandledMenuItem();

		String uri = ReflectionUtils.invokeHidden(replacer, "getDisabledIconURI", item); //$NON-NLS-1$
		assertEquals("", uri); //$NON-NLS-1$

		final String dummyUri = "dummyDisabledUri"; //$NON-NLS-1$
		item.getTransientData().put(IPresentationEngine.DISABLED_ICON_IMAGE_KEY, dummyUri);
		uri = ReflectionUtils.invokeHidden(replacer, "getDisabledIconURI", item); //$NON-NLS-1$
		assertEquals(dummyUri, uri);

	}

	/**
	 * Tests the <i>private</i> method {@code getDisabledIconURI}.
	 * 
	 * @throws Exception
	 *             handled by JUnit
	 */
	public void testSetDisabledIconURI() throws Exception {

		final E4ImageReplacer replacer = E4ImageReplacer.getInstance();

		final MHandledItem item = MMenuFactory.INSTANCE.createHandledMenuItem();
		String uri = (String) item.getTransientData().get(IPresentationEngine.DISABLED_ICON_IMAGE_KEY);
		assertNull(uri);

		final String dummyUri = "dummyDisabledUri"; //$NON-NLS-1$
		ReflectionUtils.invokeHidden(replacer, "setDisabledIconURI", item, dummyUri); //$NON-NLS-1$
		uri = (String) item.getTransientData().get(IPresentationEngine.DISABLED_ICON_IMAGE_KEY);
		assertEquals(dummyUri, uri);

	}

	/**
	 * Tests the <i>private</i> method {@code getReplaceUri(String)}.
	 * 
	 * @throws Exception
	 *             handled by JUnit
	 */
	public void testGetReplaceUri() throws Exception {

		final RienaDefaultLnf originalLnf = LnfManager.getLnf();
		LnfManager.setLnf(new MyLnf());

		final E4ImageReplacer replacer = E4ImageReplacer.getInstance();

		URL url = this.getClass().getResource("/icons/testimagea00.png"); //$NON-NLS-1$
		String uri = url.toURI().toString();
		String path = URI.create(uri).getPath();

		String scaledUri = ReflectionUtils.invokeHidden(replacer, "getReplaceUri", uri); //$NON-NLS-1$
		String scaledPath = URI.create(scaledUri).getPath();
		assertEquals(scaledPath, path);

		final Point dpi = SwtUtilities.getDpi();
		ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(144, 144)); //$NON-NLS-1$

		scaledUri = ReflectionUtils.invokeHidden(replacer, "getReplaceUri", uri); //$NON-NLS-1$
		url = this.getClass().getResource("/icons/testimagea03.png"); //$NON-NLS-1$
		uri = url.toURI().toString();
		path = URI.create(uri).getPath();
		scaledPath = URI.create(scaledUri).getPath();
		assertEquals(scaledPath, path);

		ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", dpi); //$NON-NLS-1$
		LnfManager.setLnf(originalLnf);

	}

	private static class MyLnf extends RienaDefaultLnf {
		@Override
		public String getIconScaleSuffix(final Point dpi) {

			if (dpi == null) {
				return "00"; //$NON-NLS-1$
			}
			if (dpi.x <= 96) {
				return "00"; //$NON-NLS-1$
			}
			return "03"; //$NON-NLS-1$

		}
	}

}
