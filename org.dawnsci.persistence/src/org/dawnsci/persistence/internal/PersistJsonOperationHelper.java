/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PersistJsonOperationHelper {

	ObjectMapper mapper;
	IOperationService service;
	
	private final static String DATA = "data";
	private final static String ID = "id";
	private final static String SLASH = "/";
	private final static String NAME = "name";
	private final static String PASS = "passed";
	private final static String SAVE = "saved";
	
	private final static Logger logger = LoggerFactory.getLogger(PersistJsonOperationHelper.class);
	
	public IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(IHierarchicalDataFile file) throws Exception{
		
		List<IOperation> opList = new ArrayList<IOperation>();

		String group = file.group(PersistenceConstants.PROCESS_ENTRY);
		List<String> memberList = file.memberList(group);

		int i = 0;
		String[] name = new String[]{PersistenceConstants.PROCESS_ENTRY+SLASH+ Integer.toString(i)};

		while (memberList.contains(name[0])){
			IDataset data = LoaderFactory.getDataSet(file.getPath(), name[0] +SLASH + DATA, null);
			IDataset id = LoaderFactory.getDataSet(file.getPath(), name[0] +SLASH + ID, null);
			String json = data.getObject(0).toString();
			String sid = id.getObject(0).toString();
			
			boolean p = false;
			boolean s = false;
			
			try {
				IDataset pass = LoaderFactory.getDataSet(file.getPath(), name[0] +SLASH + PASS, null);
				IDataset save = LoaderFactory.getDataSet(file.getPath(), name[0] +SLASH + SAVE, null);
				p = pass.getInt(0) == 0 ? false : true;
				s = save.getInt(0) == 0 ? false : true;
			} catch (Exception e) {
				logger.error("Could not read pass/save nodes", e);
			}
			
			if (service == null) service = (IOperationService)ServiceManager.getService(IOperationService.class);

			IOperation op = service.create(sid);
			Class<? extends IOperationModel> modelClass = service.getModelClass(sid);
			if (mapper == null) mapper = new ObjectMapper();
			
			try {
			IOperationModel readValue = mapper.readValue(json, modelClass);
			op.setModel(readValue);
			} catch (Exception e) {
				logger.error("Could not read model values", e);
				Class modelType = (Class)((ParameterizedType)op.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
				IOperationModel model  = (IOperationModel) modelType.newInstance();
				op.setModel(model);
			}
			
			op.setPassUnmodifiedData(p);
			op.setStoreOutput(s);
			
			opList.add(op);

			name[0] = PersistenceConstants.PROCESS_ENTRY +"/"+ Integer.toString(++i);

		}

		return opList.isEmpty() ? null : opList.toArray(new IOperation[opList.size()]);
	}
	
	public void writeOperations(IHierarchicalDataFile file, IOperation<? extends IOperationModel, ? extends OperationData>[] operations) throws Exception {
			
		String process = file.group(PersistenceConstants.PROCESS_ENTRY);
		//file.setNexusAttribute(process, Nexus.PROCESS);

		for (int i = 0; i < operations.length; i++) {
			writeOperationToProcess(file, process, i, operations[i]);
		}
		
	}
	
	private void writeOperationToProcess(IHierarchicalDataFile file,String group, int i, IOperation<? extends IOperationModel, ? extends OperationData> op) throws Exception {
		
		String opId = op.getId();
		String name = op.getName();
		String modelJson = getModelJson(op.getModel());
		IDataset boolTrue = DatasetFactory.ones(new int[] {1}, Dataset.INT);
		IDataset boolFalse =  DatasetFactory.zeros(new int[] {1}, Dataset.INT);
		
		IDataset pass = op.isPassUnmodifiedData() ? boolTrue : boolFalse;
		IDataset save = op.isStoreOutput() ? boolTrue : boolFalse;
		
		String note = file.group(Integer.toString(i), group);
		//file.setNexusAttribute(note, Nexus.NOTE);
		file.createStringDataset(NAME, name, note);
		file.createStringDataset(ID, opId, note);
		file.createStringDataset(DATA, modelJson, note);
		file.createDataset(SAVE, save, note);
		file.createDataset(PASS, pass, note);
		
	}
	
	public String getModelJson(IOperationModel model) throws Exception {
		if (mapper == null ) mapper = new ObjectMapper();
		return mapper.writeValueAsString(model);
	}
}
