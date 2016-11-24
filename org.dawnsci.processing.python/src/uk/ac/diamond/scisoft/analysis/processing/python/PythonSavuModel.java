package uk.ac.diamond.scisoft.analysis.processing.python;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;


public class PythonSavuModel extends AbstractOperationModel {
	
	String wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
	@OperationModelField(label="Plugin Name")
	private String pluginName = null;
	
	@OperationModelField(label="Meta data only", hint="Do we want the data in the meta data or to be passed down the chain")
	private boolean MetaDataOnly = false;

	public boolean isMetaDataOnly() {
		return this.MetaDataOnly;
	}

	public void setMetaDataOnly(boolean metaDataOnly) {
		MetaDataOnly = metaDataOnly;
	}
	@OperationModelField(label="Plugin Path")
	private String pluginPath = null;
	
	@OperationModelField(label="Parameters", hint="parameters for the filter")
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
		Map <String, Object> pluginMap = (Map<String, Object>) getPluginMap().get(pluginName);
		setPluginPath((String) pluginMap.get("path2plugin"));
		Map <String, Object> parameters = (Map <String, Object>) getPluginParams(pluginName);
		System.out.println(parameters);
		setParameters(parameters);

		}	
		

	public Object getPluginPath() {
		return pluginPath;
	}

	public void setPluginPath(String pluginPath) {
		this.pluginPath = pluginPath;
	}

	public Map <String, Object> getPluginMap(){

		final String savuPluginPath = wspacePath+"savu_plugin_info.ser";
		
		Map <String, Object> pluginDict = null;
		FileInputStream fileIn = null;

		try {
			fileIn = new FileInputStream(savuPluginPath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			pluginDict = (Map<String, Object>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pluginDict;
	}
	public Map <String, Object> getPluginParams(String pluginName){

		final String savuPluginPath = wspacePath+pluginName+".ser";
		
		Map <String, Object> pluginDict = null;
		FileInputStream fileIn = null;

		try {
			fileIn = new FileInputStream(savuPluginPath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			pluginDict = (Map<String, Object>) in.readObject();
			in.close();
			fileIn.close();
			System.out.println(pluginDict.keySet().toString());
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pluginDict;
	}
	
	
}
