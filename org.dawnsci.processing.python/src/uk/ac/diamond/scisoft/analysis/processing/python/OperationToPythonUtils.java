package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;

public class OperationToPythonUtils {

	public static final String INPUT_DATA = "data";
	public static final String OUTPUT_DATA = "data_out";
	public static final String INPUT_XAXIS = "xaxis";
	public static final String OUTPUT_XAXIS = "xaxis_out";
	public static final String INPUT_YAXIS = "yaxis";
	public static final String OUTPUT_YAXIS = "yaxis_out";
	public static final String INPUT_MASK = "mask";
	public static final String OUTPUT_MASK = "mask_out";
	public static final String INPUT_ERROR = "error";
	public static final String OUTPUT_ERROR = "error_out";

	

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

		inputs.put(INPUT_XAXIS, xa);
		inputs.put(INPUT_YAXIS, ya);
		return inputs;
	}
	
	public static OperationData unpackImage(Map<String, Object> output) {
		
		IDataset d = (IDataset)output.get(OUTPUT_DATA);
		
		if (output.containsKey(OUTPUT_XAXIS) || output.containsKey(OUTPUT_YAXIS)) {
			AxesMetadataImpl ax = new AxesMetadataImpl(2);
			if (output.containsKey(OUTPUT_XAXIS)) ax.addAxis(1, (IDataset)output.get(OUTPUT_XAXIS));
			if (output.containsKey(OUTPUT_YAXIS)) ax.addAxis(0, (IDataset)output.get(OUTPUT_YAXIS));
			
			d.addMetadata(ax);
		}
		
		return new OperationData(d);
	}
	
	public static OperationData unpackXY(Map<String, Object> output) {
		
		IDataset data = (IDataset)output.get(OUTPUT_DATA);
		
		if (output.containsKey(OUTPUT_XAXIS)) {
			AxesMetadataImpl ax = new AxesMetadataImpl(1);
			ax.addAxis(0, (IDataset)output.get(OUTPUT_XAXIS));
			data.addMetadata(ax);
		}
		
		return new OperationData(data);
	}

	public static Map<String, Object> packXY(IDataset input) {
		
		
		Map<String,Object> inputs = new HashMap<String,Object>();
		
		inputs.put(INPUT_DATA, input);
		
		ILazyDataset[] axes = AbstractOperation.getFirstAxes(input);
		if (axes != null && axes[0] != null) {
			IDataset ax = axes[0].getSlice();
			inputs.put(INPUT_XAXIS, ax);
		} else {
			inputs.put(INPUT_XAXIS, null);
		}
		
		return inputs;
	}
}
