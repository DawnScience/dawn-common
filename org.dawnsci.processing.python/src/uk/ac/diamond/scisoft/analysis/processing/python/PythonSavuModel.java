package uk.ac.diamond.scisoft.analysis.processing.python;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;


public class PythonSavuModel extends AbstractOperationModel {
	
//	The plugin name
	@OperationModelField(editable = false, visible = false, label="Plugin Name")
	private String pluginName = null;

	public String getPluginName() {
		return this.pluginName;
	}

	public void setPluginName(String pluginName) {
		firePropertyChange("pluginName", this.pluginName, this.pluginName = pluginName);
	}
	
	// Whether this is to be treated as metadata or just passed down the chain
	@OperationModelField(editable = false, visible = false, label="Meta data only", hint="Do we want the data in the meta data or to be passed down the chain")
	private boolean metaDataOnly = false;

	public boolean isMetaDataOnly() {
		return this.metaDataOnly;
	}

	public void setMetaDataOnly(boolean metaDataOnly) {
		firePropertyChange("metaDataOnly", this.metaDataOnly, this.metaDataOnly = metaDataOnly);
	}
	
//	the pluginPath
	@OperationModelField(editable = false, visible = false, label="Plugin Path")
	private String pluginPath = null;

	public String getPluginPath() {
		return pluginPath;
	}

	public void setPluginPath(String pluginPath) {
		firePropertyChange("pluginPath", this.pluginPath, this.pluginPath = pluginPath);
	}

	// the parameters Map -> read only!
	@OperationModelField(editable = false, visible = false, label="Parameters", hint="parameters for the filter")
	private Map<String, Object> parameters = new HashMap<>();

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters.clear();
		this.parameters.putAll(parameters);
	}

//	the plugin input rank
	@OperationModelField(editable = false, visible = false, label="Plugin input rank")
	private int pluginRank = -1;

	public int getPluginRank() {
		return pluginRank;
	}

	public void setPluginRank(int pluginRank) {
		this.pluginRank = pluginRank; // notice the lack of firePropertyChange...
	}

}
