package org.dawb.common.ui.plot.tool;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.BoxLineType;

/**
 * Interface designed to hide special tool pages.
 * @author fcp94556
 *
 */
public interface IProfileToolPage extends IToolPage {

	/**
	 * Line type for Box Line Profiles
	 * @param horizontalType
	 */
	void setLineType(BoxLineType lineType);

}
