package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;

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
		return inputs;
	}
	
	public static OperationData unpackImage(Map<String, Object> output) {
		return new OperationData((IDataset)output.get(OUTPUT_DATA));
	}
	
	public static OperationData unpackXY(Map<String, Object> output) {
		return new OperationData((IDataset)output.get(OUTPUT_DATA));
	}

	public static Map<String, Object> packXY(IDataset input) {
		Map<String,Object> inputs = new HashMap<String,Object>();
		inputs.put(INPUT_DATA, input);
		return inputs;
	}
}
