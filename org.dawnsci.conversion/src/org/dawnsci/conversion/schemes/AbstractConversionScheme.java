package org.dawnsci.conversion.schemes;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.conversion.IConversion;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;

public abstract class AbstractConversionScheme implements IConversionScheme {
	private final String  uiLabel;
	private final int[]   preferredRanks;
	private final boolean userVisible;
	private final boolean nexusOnly;
	private final boolean nexusSourceAllowed;
	private final Class<? extends IConversion> conversion;
	
	protected AbstractConversionScheme(
			final Class<? extends IConversion> conversion,
			final String uiLabel,
			final boolean userVisible,
			final boolean nexusOnly,
			final boolean nexusSourceAllowed,
			final int... preferredRanks) {
		
		this.conversion = conversion;
		this.uiLabel = uiLabel;
		this.preferredRanks = preferredRanks;
		this.userVisible = userVisible;
		this.nexusOnly = nexusOnly;
		this.nexusSourceAllowed = nexusSourceAllowed;
	}
	
	protected AbstractConversionScheme(
			final Class<? extends IConversion> conversion,
			final String uiLabel,
			final boolean userVisible,
			final int... preferredRanks) {
		this(conversion, uiLabel, userVisible, true, preferredRanks);
	}
	
	protected AbstractConversionScheme(
			final Class<? extends IConversion> conversion,
			final String uiLabel,
			final boolean userVisible,
			final boolean nexusOnly,
			final int... preferredRanks) {
		this(conversion, uiLabel, userVisible, nexusOnly, true, preferredRanks);
	}
	
	public String getUiLabel() {
		return uiLabel;
	}
	
	public int[] getPreferredRanks() {
		return preferredRanks;
	}
	
	public boolean isRankSupported(int rank) {
		if (preferredRanks==null) return false;
		for (int i = 0; i < preferredRanks.length; i++) {
			if (preferredRanks[i]==rank) return true;
		}
		return false;
	}
	
	public boolean isUserVisible() {
		return userVisible;
	}

	public boolean isNexusOnly() {
		return nexusOnly;
	}
	
	public boolean isNexusSourceAllowed() {
		return nexusSourceAllowed;
	}
	
	public String getDescription() {
		final StringBuilder buf = new StringBuilder();
		buf.append("Conversion Name:\t");
		buf.append(uiLabel);
		buf.append("\n\n");
		
		buf.append("Data Source:\t");
		buf.append(isNexusOnly()?"HDF5 or Nexus files only":"Any loadable data of correct rank");
		buf.append("\n\n");
		
		buf.append("Supported Data Ranks:\t");
		buf.append(Arrays.toString(preferredRanks));
		buf.append("\n\n");
		
		return buf.toString();
	}
	
	public Class<? extends IConversion> getConversion() {
		return conversion;
	}
	
	@Override
	public String toString() {
        return getClass().getName();
    }
}
