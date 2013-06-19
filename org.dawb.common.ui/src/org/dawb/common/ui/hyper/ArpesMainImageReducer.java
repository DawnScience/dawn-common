package org.dawb.common.ui.hyper;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;

public class ArpesMainImageReducer implements IDatasetROIReducer{
	
	private final RegionType regionType = RegionType.LINE;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<ILazyDataset> axes,
			int dim, IROI roi) {
		if (roi instanceof LinearROI) {
			int[] dims = ROISliceUtils.getImageAxis(dim);
			final IDataset image = ((AbstractDataset)ROISliceUtils.getDataset(data, (LinearROI)roi, dims)).transpose();
			
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
		int[] imageAxis = ROISliceUtils.getImageAxis(dim);
		
		IDataset x = axes.get(imageAxis[1]).getSlice();
		IDataset y = axes.get(imageAxis[0]).getSlice();
		
		double xMin = x.min().doubleValue();
		double xMax = x.max().doubleValue();
		
		double yMin = y.min().doubleValue();
		double yMax = y.max().doubleValue();
		
		double[] start = new double[]{yMin,xMin};
		double[] end = new double[]{yMax/10,xMax/10};
		
		return new LinearROI(start, end);
	}

	@Override
	public boolean supportsMultipleRegions() {
		return false;
	}
}
