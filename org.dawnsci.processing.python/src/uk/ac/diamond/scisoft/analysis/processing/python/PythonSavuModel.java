package uk.ac.diamond.scisoft.analysis.processing.python;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;


public class PythonSavuModel extends AbstractOperationModel {
	
//	The plugin name
	@OperationModelField(editable = false, visible = false, label="Plugin Name")
	private String pluginName = null;

	// Whether this is to be treated as metadata or just passed down the chain
	@OperationModelField(editable = false, visible = false, label="Meta data only", hint="Do we want the data in the meta data or to be passed down the chain")
	private boolean MetaDataOnly = false;

	public boolean isMetaDataOnly() {
		return this.MetaDataOnly;
	}

	public void setMetaDataOnly(boolean metaDataOnly) {
		MetaDataOnly = metaDataOnly;
	}
	
//	the pluginPath
	@OperationModelField(editable = false, visible = false, label="Plugin Path")
	private String pluginPath = null;

	public String getPluginPath() {
		return pluginPath;
	}

	public void setPluginPath(String pluginPath) {
		this.pluginPath = pluginPath;
	}

	// the parameters Map
	@OperationModelField(editable = false, visible = false, label="Parameters", hint="parameters for the filter")
	private Map <String, Object> parameters = null;

	public Map<String, Object> getParameters() {
		
		return this.parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public String getPluginName() {
		return this.pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}
	
//	the plugin input rank
	@OperationModelField(editable = false, visible = false, label="Plugin input rank")
	private Integer pluginRank = null;

	public Integer getPluginRank() {
		return pluginRank;
	}

	public void setPluginRank(Integer pluginRank) {
		this.pluginRank = pluginRank;
	}

	@OperationModelField(editable = false, visible = false, label="selected list item")
	private Integer selectedItem = null;

	public Integer getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(Integer selectedItem) {
		this.selectedItem = selectedItem;
	}
	
}
