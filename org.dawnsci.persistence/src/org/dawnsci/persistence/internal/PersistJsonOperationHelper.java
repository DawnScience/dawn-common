/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.persistence.json.JacksonMarshaller;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.OriginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
	
	public void writeOperations(NexusFile file, IOperation<? extends IOperationModel, ? extends OperationData>... operations) throws Exception {
		GroupNode entryGroup = file.getGroup(PersistenceConstants.ENTRY, true);
		GroupNode processGroup = file.getGroup(entryGroup, "process", NexusConstants.PROCESS, true);

		for (int i = 0; i < operations.length; i++) {
			writeOperationToProcess(file, processGroup, i, operations[i]);
		}
	}
	
	private void writeOperationToProcess(NexusFile file, GroupNode group, int i, IOperation<? extends IOperationModel, ? extends OperationData> op) throws Exception {
		
		String opId = op.getId();
		String name = op.getName();
		IDataset boolTrue = DatasetFactory.ones(IntegerDataset.class, 1);
		IDataset boolFalse =  DatasetFactory.zeros(IntegerDataset.class, 1);
		
		Map<Class, Map<String, Object>> specialObjects = getSpecialObjects(op.getModel());
		
		IDataset pass = op.isPassUnmodifiedData() ? boolTrue : boolFalse;
		IDataset save = op.isStoreOutput() ? boolTrue : boolFalse;
		
		GroupNode groupNote =  file.getGroup(group, Integer.toString(i), "NXnote", true);
		
		Dataset dataName = DatasetFactory.createFromObject(name);
		dataName.setName(NAME);
		file.createData(groupNote, dataName);

		Dataset dataID = DatasetFactory.createFromObject(opId);
		dataID.setName(ID);
		file.createData(groupNote, dataID);

		Dataset dataSave = DatasetFactory.createFromObject(save);
		dataSave.setName(SAVE);
		file.createData(groupNote, dataSave);

		Dataset dataPass = DatasetFactory.createFromObject(pass);
		dataPass.setName(PASS);
		file.createData(groupNote, dataPass);

		Map<String, Object> m = specialObjects.get(IROI.class);
		writeSpecialObjects(m, REGIONS, file, groupNote);
		m = specialObjects.get(IFunction.class);
		writeSpecialObjects(m, FUNCTIONS, file, groupNote);
		m = specialObjects.get(IDataset.class);
		writeSpecialObjects(m, DATASETS, file, groupNote);

		String modelJson = getModelJson(op.getModel());
		Dataset dataJson = DatasetFactory.createFromObject(modelJson);
		dataJson.setName(DATA);
		file.createData(groupNote, dataJson);

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

		Map<Class,Map<String, Object>> out = new HashMap<>();
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
	
	public void writeSpecialObjects(Map<String, Object> special, String type, NexusFile file, GroupNode group) throws Exception {
		if (!special.isEmpty()) {
			GroupNode groupCollection = file.getGroup(group, type, NexusConstants.COLLECTION, true);

			for (Map.Entry<String, Object> entry : special.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof IDataset) {
					IDataset data = (IDataset) value;
					data.setName(key);
					file.createData(groupCollection, data);
				} else {
					IJSonMarshaller converter = new JacksonMarshaller();
					String json = converter.marshal(value);
					Dataset data = DatasetFactory.createFromObject(json);
					data.setName(key);
					file.createData(groupCollection, data);
				}
			}
		}
	}
	
	public void writeOriginalDataInformation(NexusFile file, OriginMetadata origin) throws Exception {
		GroupNode groupEntry = file.getGroup(PersistenceConstants.ENTRY, true);
		
		GroupNode processNode = file.getGroup(groupEntry, "process", NexusConstants.PROCESS, true);
		String originPath = PersistenceConstants.PROCESS_ENTRY + Node.SEPARATOR + ORIGIN;
		GroupNode originNode = file.getGroup(originPath, true);
		Dataset pathData = DatasetFactory.createFromObject(origin.getFilePath());
		pathData.setName("path");
		file.createData(originNode, pathData);
		Dataset dataset = DatasetFactory.createFromObject(origin.getDatasetName());
		dataset.setName("dataset");
		file.createData(originNode, dataset);
		if (origin instanceof SliceFromSeriesMetadata) {
			Dataset samplingData = DatasetFactory.createFromObject(Slice.createString(((SliceFromSeriesMetadata)origin).getSliceInfo().getSubSampling()));
			samplingData.setName("sampling");
			file.createData(originNode, samplingData);
		}
		Dataset dd = DatasetFactory.createFromObject(origin.getDataDimensions());
		dd.setName("data dimensions");
		file.createData(originNode, dd);
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
