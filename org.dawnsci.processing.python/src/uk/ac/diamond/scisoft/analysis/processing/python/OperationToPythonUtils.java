package uk.ac.diamond.scisoft.analysis.processing.python;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
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
	public static final String AUXILIARY = "auxiliary";
	

	

	public static Map<String, Object> packImage(IDataset input) {
		Map<String,Object> inputs = new HashMap<String,Object>();
		inputs.put("data", input);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		IDataset xa = null;
		IDataset ya = null;
		if (axes != null) {

			if (axes[0] != null) {
				try {
					ya = axes[0].getSlice().squeeze();
				} catch (DatasetException e1) {
				}
			}
			if (axes[1] != null) {
				try {
					xa = axes[1].getSlice().squeeze();
				} catch (DatasetException e1) {
				}
			}
			
		}

		inputs.put(XAXIS, xa);
		inputs.put(YAXIS, ya);
		
		populateSliceFromSeriesMetadata(AbstractOperation.getSliceSeriesMetadata(input),inputs);
		
		ILazyDataset mask = AbstractOperation.getFirstMask(input);
		if (mask != null) {
			try {
				inputs.put(MASK, mask.getSlice());
			} catch (DatasetException e1) {
			}
		}
		ILazyDataset e = input.getError();
		if (e != null) {
			try {
				inputs.put(ERROR, e.getSlice());
			} catch (DatasetException e1) {
			}
		}
		
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
		
		if (output.containsKey(ERROR)) {
			
			d.setError((IDataset)output.get(ERROR));
		}
		
		IDataset[] aux = unpackAuxiliary(output);
		
		return aux == null ? new OperationData(d) : new OperationData(d, (Serializable[])aux);
	}
	
	public static OperationData unpackXY(Map<String, Object> output) {
		
		IDataset data = (IDataset)output.get(DATA);
		
		if (output.containsKey(XAXIS)) {
			AxesMetadataImpl ax = new AxesMetadataImpl(1);
			ax.addAxis(0, (IDataset)output.get(XAXIS));
			data.addMetadata(ax);
		}
		
		if (output.containsKey(ERROR)) {
			
			data.setError((IDataset)output.get(ERROR));
		}
		
		IDataset[] aux = unpackAuxiliary(output);
		
		return aux == null ? new OperationData(data) : new OperationData(data, (Serializable[])aux);
	}

	public static Map<String, Object> packXY(IDataset input) {
		
		
		Map<String,Object> inputs = new HashMap<String,Object>();
		
		inputs.put(DATA, input);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		if (axes != null && axes[0] != null) {
			try {
				IDataset ax = axes[0].getSlice();
				inputs.put(XAXIS, ax);
			} catch (DatasetException e1) {
			}
		} else {
			inputs.put(XAXIS, null);
		}
		
		ILazyDataset e = input.getError();
		if (e != null) {
			try {
				inputs.put(ERROR, e.getSlice());
			} catch (DatasetException e1) {
			}
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
	
	private static IDataset[] unpackAuxiliary(Map<String, Object> output) {
		Object aux = output.get(AUXILIARY);
		if (aux == null) return null;
		List<IDataset> auxData = new ArrayList<>();
		if (aux instanceof Map) {
			Map m = (Map)aux;
			Set keySet = m.keySet();
			for (Object key : keySet) {
				if (!(key instanceof String)) continue;
				Object object = m.get(key);
				if (object instanceof IDataset) {
					IDataset d = (IDataset)object;
					d.setName((String)key);
					auxData.add(d);
				} else if (object instanceof Number) {
					IDataset d = DatasetFactory.createFromObject(object);
					d.setName((String)key);
					auxData.add(d);
				}
			}
		}

		return auxData.isEmpty() ? null : auxData.toArray(new IDataset[auxData.size()]);
	}
}
