package org.dawb.common.ui.hyper;

import java.util.List;

import org.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

public interface IDatasetROIReducer {

	
public boolean isOutput1D();
	
	public IDataset reduce(ILazyDataset data, List<AbstractDataset> axes, IROI roi, Slice[] slices, int[] order);
	
	public List<RegionType> getSupportedRegionType();
	
	public IROI getInitialROI(List<AbstractDataset> axes, int[] order);
	
	public boolean supportsMultipleRegions();
	
	public List<IDataset> getAxes();
	
}
