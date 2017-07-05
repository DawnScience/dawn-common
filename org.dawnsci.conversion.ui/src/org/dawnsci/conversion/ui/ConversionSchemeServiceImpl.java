package org.dawnsci.conversion.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionSchemeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionSchemeServiceImpl implements IConversionSchemeService {

	static {
		System.out.println("Starting conversion scheme service");
	}
	
	private List<IConversionScheme> schemes;
	private static final Logger logger = LoggerFactory.getLogger(ConversionSchemeServiceImpl.class);

	
	public ConversionSchemeServiceImpl() {
		// keep empty for OSGI
	}

	@Override
	public List<IConversionScheme> getSchemes() {
		checkConversionSchemes();
		return schemes;
	}

	@Override
	public List<IConversionScheme> getSchemes(boolean visibleOnly) {
		checkConversionSchemes();
		if (!visibleOnly)
			return schemes;
		return schemes.stream().filter(IConversionScheme::isUserVisible).collect(Collectors.toList());
	}

	@Override
	public IConversionScheme getScheme(String schemeClassName) {
		checkConversionSchemes();
		for (IConversionScheme scheme : schemes) {
			if (scheme.toString().equals(schemeClassName))
				return scheme;
		}
		return null;
	}

	@Override
	public <U extends IConversionScheme> U getScheme(Class<U> schemeClass) {
		checkConversionSchemes();
		for (IConversionScheme scheme : schemes) {
			if (schemeClass.isInstance(scheme))
				return schemeClass.cast(scheme);
		}
		return null;
	}
	
	private synchronized void checkConversionSchemes() {
		if (schemes != null)
			return;
		
		schemes = new ArrayList<>(20);
		
		final IConfigurationElement[] ce = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.conversion.ui.conversionPage");
		for (IConfigurationElement e : ce) {
			IConversionScheme scheme = null;
			try {
				scheme = (IConversionScheme) e.createExecutableExtension("conversion_scheme");
				if (scheme == null)
					continue;
				schemes.add(scheme);
			} catch (CoreException e2) {
					logger.error("Cannot get page "+e.getAttribute("conversion_scheme"), e2);
			}
		}
	}

}
