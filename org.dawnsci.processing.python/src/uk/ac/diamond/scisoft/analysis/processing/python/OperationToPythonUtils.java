package uk.ac.diamond.scisoft.analysis.processing.python;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MaskMetadata;
import org.eclipse.january.metadata.MetadataFactory;

public class OperationToPythonUtils {

	public static final String DATA = "data";
	public static final String DATA_TITLE = "data_title";
	public static final String XAXIS = "xaxis";
	public static final String XAXIS_TITLE = "xaxis_title";
	public static final String YAXIS = "yaxis";
	public static final String YAXIS_TITLE = "yaxis_title";
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

		String dataTitle = " ";
		String xAxisTitle = " ";
		String yAxisTitle = " ";

		inputs.put(DATA, input);
		dataTitle = input.getName();
		inputs.put(DATA_TITLE, dataTitle);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		IDataset xa = null;
		IDataset ya = null;
		
		if (axes != null) {

			if (axes[0] != null) {
				try {
					ya = axes[0].getSlice().squeeze();
					yAxisTitle = ya.getName();
				} catch (DatasetException e1) {
				}
			}
			if (axes[1] != null) {
				try {
					xa = axes[1].getSlice().squeeze();
					xAxisTitle = xa.getName();
				} catch (DatasetException e1) {
				}
			}
			
		}

		inputs.put(XAXIS, xa);
		inputs.put(XAXIS_TITLE, xAxisTitle);
		inputs.put(YAXIS, ya);
		inputs.put(YAXIS_TITLE, yAxisTitle);
		
		populateSliceFromSeriesMetadata(AbstractOperation.getSliceSeriesMetadata(input),inputs);
		
		ILazyDataset mask = AbstractOperation.getFirstMask(input);
		if (mask != null) {
			try {
				inputs.put(MASK, mask.getSlice());
			} catch (DatasetException e1) {
			}
		}
		ILazyDataset e = input.getErrors();
		if (e != null) {
			try {
				inputs.put(ERROR, e.getSlice());
			} catch (DatasetException e1) {
			}
		}
		
		return inputs;
	}
	
	public static OperationData unpackImage(Map<String, Object> output) throws MetadataException {
		
		IDataset d = (IDataset)output.get(DATA);
		String dataTitle = (String) output.get(DATA_TITLE);
		d.setName(dataTitle);
		
		if (output.containsKey(XAXIS) || output.containsKey(YAXIS)) {
			AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			if (output.containsKey(XAXIS) && output.get(XAXIS) != null) {
				IDataset xaxis = (IDataset)output.get(XAXIS);
				String xAxisTitle = (String) output.get(XAXIS_TITLE);
				xaxis.setName(xAxisTitle);
				ax.addAxis(1, xaxis);
			}
			if (output.containsKey(YAXIS) && output.get(YAXIS) != null){
				IDataset yaxis = (IDataset)output.get(YAXIS);
				String yAxisTitle = (String) output.get(YAXIS_TITLE);
				yaxis.setName(yAxisTitle);
				ax.addAxis(0, yaxis);
			}
			
			d.addMetadata(ax);
		}
		
		if (output.containsKey(MASK)) {
			MaskMetadata mask = MetadataFactory.createMetadata(MaskMetadata.class, (IDataset)output.get(MASK));
			d.setMetadata(mask);
		}
		
		if (output.containsKey(ERROR)) {
			
			d.setErrors((IDataset)output.get(ERROR));
		}
		
		IDataset[] aux = unpackAuxiliary(output);
		
		return aux == null ? new OperationData(d) : new OperationData(d, (Serializable[])aux);
	}
	
	public static OperationData unpackXY(Map<String, Object> output) throws MetadataException {
		
		IDataset data = (IDataset)output.get(DATA);
		String dataTitle = (String) output.get(DATA_TITLE);
		data.setName(dataTitle);
		
		if (output.containsKey(XAXIS) && output.get(XAXIS) != null) {
			AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			
			IDataset x = (IDataset)output.get(XAXIS);
			String xAxisTitle = (String) output.get(XAXIS_TITLE);
			x.setName(xAxisTitle);
			ax.addAxis(0, x);
			data.addMetadata(ax);
		}
		
		if (output.containsKey(ERROR)) {
			IDataset error = (IDataset)output.get(ERROR);
			error.setName(ERROR);
			data.setErrors(error);
		}
		
		IDataset[] aux = unpackAuxiliary(output);
		
		return aux == null ? new OperationData(data) : new OperationData(data, (Serializable[])aux);
	}

	public static Map<String, Object> packXY(IDataset input) {
				
		Map<String,Object> inputs = new HashMap<String,Object>();
		
		String dataTitle = " ";
		String xAxisTitle = " ";

		inputs.put(DATA, input);
		dataTitle = input.getName();
		inputs.put(DATA_TITLE, dataTitle);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		if (axes != null && axes[0] != null) {
			try {
				IDataset ax = axes[0].getSlice();
				xAxisTitle = ax.getName();
				inputs.put(XAXIS, ax);
				inputs.put(XAXIS_TITLE, xAxisTitle);
			} catch (DatasetException e1) {
			}
		} else {
			inputs.put(XAXIS, null);
			inputs.put(XAXIS_TITLE, null);
		}
		
		ILazyDataset e = input.getErrors();
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
