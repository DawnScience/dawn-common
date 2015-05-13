package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.metadata.MaskMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;

public class OperationToPythonUtils {

	public static final String DATA = "data";
	public static final String XAXIS = "xaxis";
	public static final String YAXIS = "yaxis";
	public static final String MASK = "mask";
	public static final String ERROR = "error";
	public static final String FILE_PATH = "file_path";
	public static final String DATASET_NAME = "dataset_name";
	public static final String CURRENT_SLICE = "current_slice";
	public static final String DATA_DIMENSIONS = "data_dimensions";
	public static final String TOTAL = "total";

	

	public static Map<String, Object> packImage(IDataset input) {
		Map<String,Object> inputs = new HashMap<String,Object>();
		inputs.put("data", input);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		IDataset xa = null;
		IDataset ya = null;
		if (axes != null) {

			if (axes[0] != null) ya = axes[0].getSlice().squeeze();
			if (axes[1] != null) xa = axes[1].getSlice().squeeze();
			
		}

		inputs.put(XAXIS, xa);
		inputs.put(YAXIS, ya);
		
		populateSliceFromSeriesMetadata(AbstractOperation.getSliceSeriesMetadata(input),inputs);
		
		ILazyDataset mask = AbstractOperation.getFirstMask(input);
		if (mask != null) inputs.put(MASK, mask.getSlice());
		
		return inputs;
	}
	
	public static OperationData unpackImage(Map<String, Object> output) {
		
		IDataset d = (IDataset)output.get(DATA);
		
		if (output.containsKey(XAXIS) || output.containsKey(YAXIS)) {
			AxesMetadataImpl ax = new AxesMetadataImpl(2);
			if (output.containsKey(XAXIS)) ax.addAxis(1, (IDataset)output.get(XAXIS));
			if (output.containsKey(YAXIS)) ax.addAxis(0, (IDataset)output.get(YAXIS));
			
			d.addMetadata(ax);
		}
		
		if (output.containsKey(MASK)) {
			MaskMetadataImpl mask = new MaskMetadataImpl((IDataset)output.get(MASK));
			d.setMetadata(mask);
		}
		
		return new OperationData(d);
	}
	
	public static OperationData unpackXY(Map<String, Object> output) {
		
		IDataset data = (IDataset)output.get(DATA);
		
		if (output.containsKey(XAXIS)) {
			AxesMetadataImpl ax = new AxesMetadataImpl(1);
			ax.addAxis(0, (IDataset)output.get(XAXIS));
			data.addMetadata(ax);
		}
		
		return new OperationData(data);
	}

	public static Map<String, Object> packXY(IDataset input) {
		
		
		Map<String,Object> inputs = new HashMap<String,Object>();
		
		inputs.put(DATA, input);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		if (axes != null && axes[0] != null) {
			IDataset ax = axes[0].getSlice();
			inputs.put(XAXIS, ax);
		} else {
			inputs.put(XAXIS, null);
		}
		
		populateSliceFromSeriesMetadata(AbstractOperation.getSliceSeriesMetadata(input),inputs);
		
		return inputs;
	}
	
	private static void populateSliceFromSeriesMetadata(SliceFromSeriesMetadata meta, Map<String,Object> inputs) {
		
		inputs.put(FILE_PATH, meta.getFilePath());
		inputs.put(DATASET_NAME, meta.getDatasetName());
		inputs.put(CURRENT_SLICE,Slice.createString(meta.getSliceFromInput()));
		inputs.put(DATA_DIMENSIONS, meta.getDataDimensions());
		inputs.put(TOTAL,meta.getTotalSlices());
		
	}
}
