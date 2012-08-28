package org.dawb.common.ui.plot.tool;

import ncsa.hdf.object.Group;

import org.dawb.hdf5.IHierarchicalDataFile;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * Interface used  to define this tool as a data reduction tool. 
 * 
 * Data Reduction tools generally reduce data from nD to (n-1)D,
 * for instance radial integration of an image (2d) to 1d. They can also be applied
 * n+1 data dimensions using the @see DataReductionWizard
 * 
 * @author fcp94556
 *
 */
public interface IDataReductionToolPage extends IToolPage {

	/**
	 * Export the tool results to an hdf5 file under the passed in group.
	 * 
	 * This method is used to run the tool multiple times on different slices of the data.
	 * 
	 * This method will not be called on the UI thread in most instances.
	 * 
	 * @param hf
	 * @param group
	 * @param set -  the data to run the tool on.
	 */
	public void export(IHierarchicalDataFile hf, Group parent, AbstractDataset set) throws Exception;

}
