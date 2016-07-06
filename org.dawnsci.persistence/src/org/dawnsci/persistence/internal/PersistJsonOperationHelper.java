/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.persistence.json.JacksonMarshaller;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperationBase;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIBase;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.DataNodeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.GroupNodeImpl;
import org.eclipse.dawnsci.hdf.object.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf.object.Nexus;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.OriginMetadata;
import org.eclipse.january.metadata.internal.OriginMetadataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * 
 * @author Matthew Gerring
 *
 */
public class PersistJsonOperationHelper {

	
	private final static Logger logger = LoggerFactory.getLogger(PersistJsonOperationHelper.class);

	// Static stuff
	private final static String DATA = "data";
	private final static String ID = "id";
	private final static String SLASH = "/";
	private final static String NAME = "name";
	private final static String PASS = "passed";
	private final static String SAVE = "saved";
	private final static String REGIONS = "regions";
	private final static String FUNCTIONS = "functions";
	private final static String DATASETS = "datasets";
	private final static String ORIGIN = "origin";
	
	private ObjectMapper mapper;
	
	
	public void writeOperations(IHierarchicalDataFile file, IOperation<? extends IOperationModel, ? extends OperationData>... operations) throws Exception {
		String entry = file.group(PersistenceConstants.ENTRY);
		String process = file.group(PersistenceConstants.PROCESS_ENTRY);
		file.setNexusAttribute(process, Nexus.PROCESS);

		for (int i = 0; i < operations.length; i++) {
			writeOperationToProcess(file, process, i, operations[i]);
		}
		
	}
	
	private void writeOperationToProcess(IHierarchicalDataFile file,String group, int i, IOperation<? extends IOperationModel, ? extends OperationData> op) throws Exception {
		
		String opId = op.getId();
		String name = op.getName();
		IDataset boolTrue = DatasetFactory.ones(new int[] {1}, Dataset.INT);
		IDataset boolFalse =  DatasetFactory.zeros(new int[] {1}, Dataset.INT);
		
		Map<Class, Map<String, Object>> specialObjects = getSpecialObjects(op.getModel());
		
		IDataset pass = op.isPassUnmodifiedData() ? boolTrue : boolFalse;
		IDataset save = op.isStoreOutput() ? boolTrue : boolFalse;
		
		String note = file.group(Integer.toString(i), group);
		file.setNexusAttribute(note, Nexus.NOTE);
		file.createStringDataset(NAME, name, note);
		file.createStringDataset(ID, opId, note);
		file.createDataset(SAVE, save, note);
		file.createDataset(PASS, pass, note);
		
		Map<String, Object> m = specialObjects.get(IROI.class);
		writeSpecialObjects(m,REGIONS,file,note);
		m = specialObjects.get(IFunction.class);
		writeSpecialObjects(m,FUNCTIONS,file,note);
		m = specialObjects.get(IDataset.class);
		writeSpecialObjects(m,DATASETS,file,note);
		
		String modelJson = getModelJson(op.getModel());
		file.createStringDataset(DATA, modelJson, note);

	}
	
	public String getModelJson(IOperationModel model) throws Exception {
		
		if (mapper == null ) mapper = getMapper();
//		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//		mapper.setSerializationInclusion(Include.NON_NULL);
		
		return mapper.writeValueAsString(model);
	}


	/**
	 * If this is changed, please update OperationModelMarshaller as well.
	 * 
	 * @param model
	 * @return
	 */
	private Map<Class,Map<String, Object>> getSpecialObjects(IOperationModel model) {

		Map<Class,Map<String, Object>> out = new HashMap<Class, Map<String,Object>>();
		out.put(IROI.class, new HashMap<String, Object>());
		out.put(IDataset.class, new HashMap<String, Object>());
		out.put(IFunction.class, new HashMap<String, Object>());
		
		final List<Field> allFields = new ArrayList<Field>(31);
		allFields.addAll(Arrays.asList(model.getClass().getDeclaredFields()));
		allFields.addAll(Arrays.asList(model.getClass().getSuperclass().getDeclaredFields()));
		
		for (Field field : allFields) {
			Class<?> class1 = field.getType();
			if (IROI.class.isAssignableFrom(class1)) {
				try {
					out.get(IROI.class).put(field.getName(), model.get(field.getName()));
//					model.set(field.getName(), null);
				} catch (Exception e) {
					//Do nothing
				}
			} else if (IDataset.class.isAssignableFrom(class1)) {
				try {
					out.get(IDataset.class).put(field.getName(), model.get(field.getName()));
//					model.set(field.getName(), null);
				} catch (Exception e) {
					//Do nothing
				}
			} else if (IFunction.class.isAssignableFrom(class1)) {
				try {
					out.get(IFunction.class).put(field.getName(), model.get(field.getName()));
//					model.set(field.getName(), null);
				} catch (Exception e) {
					//Do nothing
				}
			}
		}
		
		return out;
	}
	
	public void writeSpecialObjects(Map<String, Object> special, String type, IHierarchicalDataFile file, String group) throws Exception {
		 if (!special.isEmpty()) {
			 String g = file.group(type, group);
			 file.setNexusAttribute(g, "NXcollection");
			 
			 for (Map.Entry<String, Object> entry : special.entrySet()) {
				    String key = entry.getKey();
				    Object value = entry.getValue();
				    if (value instanceof IDataset) {
				    	file.createDataset(key, (IDataset)value, g);
				    } else {
				    	IJSonMarshaller converter = new JacksonMarshaller();
						String json = converter.marshal(value);
						file.createStringDataset(key, json, g);
				    }
				}
		 }
	}
	
	public void writeOriginalDataInformation(IHierarchicalDataFile file, OriginMetadata origin) throws Exception {
		String entry = file.group(PersistenceConstants.ENTRY);
		String process = file.group(PersistenceConstants.PROCESS_ENTRY);
		file.setNexusAttribute(process, Nexus.PROCESS);
		String note = file.group(ORIGIN, process);
		file.createStringDataset("path", origin.getFilePath(), note);
		file.createStringDataset("dataset", origin.getDatasetName(), note);
		if (origin instanceof SliceFromSeriesMetadata) file.createStringDataset("sampling",
				Slice.createString(((SliceFromSeriesMetadata)origin).getSliceInfo().getSubSampling()), note);
		Dataset dd = DatasetFactory.createFromObject(origin.getDataDimensions());
		file.createDataset("data dimensions", dd, note);
		
	}
	
	private ObjectMapper getMapper() {
		mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.addMixInAnnotations(IDataset.class, MixIn.class);
		mapper.addMixInAnnotations(IROI.class, MixIn.class);
		mapper.addMixInAnnotations(IFunction.class, MixIn.class);
		return mapper;
	}
	
	
	@JsonIgnoreType abstract class MixIn{};
	
	

}
