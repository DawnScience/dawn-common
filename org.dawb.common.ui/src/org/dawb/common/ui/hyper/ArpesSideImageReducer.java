package org.dawb.common.ui.hyper;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.YAxisLineBoxROI;

public class ArpesSideImageReducer implements IDatasetROIReducer {
	
	private final RegionType regionType = RegionType.YAXIS_LINE;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<ILazyDataset> axes,
			int dim, IROI roi) {
		if (roi instanceof RectangularROI) {
			final IDataset image = ROISliceUtils.getAxisDataset(data, (RectangularROI)roi, dim);
			
			return image;
		}
		
		return null;
	}

	@Override
	public boolean isOutput1D() {
		return false;
	}

	@Override
	public List<RegionType> getSupportedRegionType() {
		
		List<IRegion.RegionType> regionList = new ArrayList<IRegion.RegionType>();
		regionList.add(regionType);
		
		return regionList;
	}
	
	@Override
	public IROI getInitialROI(List<ILazyDataset> axes, int dim) {
		
		double min = axes.get(dim).getSlice().min().doubleValue();
		double max = axes.get(dim).getSlice().max().doubleValue();
		
		return new YAxisLineBoxROI(0,(max-min)/10,0,(max-min)/10, 0);
	}
	
	@Override
	public boolean supportsMultipleRegions() {
		return false;
	}
}

