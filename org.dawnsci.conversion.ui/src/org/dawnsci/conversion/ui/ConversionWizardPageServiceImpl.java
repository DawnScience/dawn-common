package org.dawnsci.conversion.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dawnsci.conversion.ui.api.IConversionWizardPage;
import org.dawnsci.conversion.ui.api.IConversionWizardPageService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionWizardPageServiceImpl implements IConversionWizardPageService {

	static {
		System.out.println("Starting conversion wizardpage service");
	}
	
	private Map<IConversionScheme, IConversionWizardPage> conversionPages;
	private Map<IConversionScheme, ImageDescriptor> conversionImages;
	private static final Logger logger = LoggerFactory.getLogger(ConversionWizardPageServiceImpl.class);
	
	public ConversionWizardPageServiceImpl() {
		// Intentionally do nothing -> OSGI magic!
	}
	
	@Override
	public IConversionWizardPage getPage(IConversionScheme scheme) {
		checkConversionWizardPages();
		return conversionPages.get(scheme)/*.getClass().newInstance()*/;
	}

	@Override
	public IConversionWizardPage getPage(String label) {
		checkConversionWizardPages();
		return conversionPages.get(fromLabel(label))/*.getClass().newInstance()*/;
	}
	
	@Override
	public IConversionScheme fromLabel(String label) {
		checkConversionWizardPages();
		for (IConversionScheme scheme: conversionPages.keySet()) {
			String uiLabel = scheme.getUiLabel();
			if (uiLabel.equals(label))
				return scheme;
		}
		return null;
	}
	
	@Override
	public List<IConversionScheme> getSchemes(boolean visibleOnly) {
		checkConversionWizardPages();
		if (!visibleOnly)
			return conversionPages.keySet().stream().collect(Collectors.toList());
		return conversionPages
			.keySet()
			.stream()
			.filter(IConversionScheme::isUserVisible)
			.collect(Collectors.toList());
	}
	
	@Override
	public List<IConversionScheme> getSchemes() {
		return getSchemes(false);
	}
	
	@Override
	public String[] getLabels(boolean visibleOnly) {
		checkConversionWizardPages();
		if (!visibleOnly)
			return conversionPages
					.keySet()
					.stream()
					.map(IConversionScheme::getUiLabel)
					.toArray(String[]::new);
		return conversionPages
				.keySet()
				.stream()
				.filter(IConversionScheme::isUserVisible)
				.map(IConversionScheme::getUiLabel)
				.toArray(String[]::new);
	}
	
	@Override
	public IConversionWizardPage[] getPages(boolean visibleOnly) {
		checkConversionWizardPages();
		if (!visibleOnly)
			return conversionPages.values().toArray(new IConversionWizardPage[conversionPages.size()]);
		return conversionPages.keySet()
				.stream()
				.filter(IConversionScheme::isUserVisible)
				.map(key -> conversionPages.get(key))
				.toArray(IConversionWizardPage[]::new);
	}
	
	@Override
	public IConversionWizardPage[] getPages() {
		return getPages(false);
	}
	
	@Override
	public String[] getLabels() {
		return getLabels(false);
	}
	
	private synchronized void checkConversionWizardPages() {
		if (conversionPages != null)
			return;
		
		conversionPages = new HashMap<>(20);
		conversionImages = new HashMap<>(20);
		final IConfigurationElement[] ce = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.conversion.ui.conversionPage");
		for (IConfigurationElement e : ce) {
			IConversionScheme scheme = null;
			try {
				scheme = (IConversionScheme) e.createExecutableExtension("conversion_scheme");
				if (scheme == null)
					continue;
			} catch (CoreException e2) {
					logger.error("Cannot get page "+e.getAttribute("conversion_scheme"), e2);
			}
			try {
				final IConversionWizardPage p = (IConversionWizardPage)e.createExecutableExtension("conversion_page");
				conversionPages.put(scheme, p);
			} catch (CoreException e1) {
				logger.error("Cannot get page "+e.getAttribute("conversion_page"), e1);
			}
			String imageString = e.getAttribute("image");
			ImageDescriptor imageDescriptor = Activator.imageDescriptorFromPlugin(e.getContributor().getName(), imageString);
			if (imageDescriptor != null)
				conversionImages.put(scheme, imageDescriptor);
		}
		
	}

	@Override
	public ImageDescriptor getImage(IConversionScheme scheme) {
		return conversionImages.get(scheme);
	}

	@Override
	public <U extends IConversionScheme> U getSchemeForClass(Class<U> klazz) {
		for (IConversionScheme scheme : conversionPages.keySet()) {
			if (klazz.isInstance(scheme))
				return klazz.cast(scheme); 
		}
		return null;
	}

}
