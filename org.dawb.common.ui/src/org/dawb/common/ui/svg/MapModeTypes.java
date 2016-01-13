package org.dawb.common.ui.svg;



/**
 * @author sshaw
 * Class that defines different <code>IMapMode</code> types available for use
 *
 */
public class MapModeTypes {

	/**
	 * Constant <code>IMapMode</code> class for HiMetric coordinate mapping
	 */
	static public IMapMode HIMETRIC_MM  = new HiMetricMapMode();
	
	/**
	 * Constant <code>IMapMode</code> class for Identity coordinate mapping
	 */
	static public IMapMode IDENTITY_MM = new IdentityMapMode();
	
	/**
	 * Constant <code>IMapMode</code> class default coordinate mapping (HiMetric is
	 * current default).
	 */
	static public IMapMode DEFAULT_MM = HIMETRIC_MM;
}
