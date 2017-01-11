package org.dawnsci.persistence.internal;

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
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.DataNodeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.GroupNodeImpl;
import org.eclipse.dawnsci.hdf.object.Nexus;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.metadata.OriginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class PersistJsonOperationsNode {

	private final static String DATA = "data";
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String PASS = "passed";
	private final static String SAVE = "saved";
	private final static String REGIONS = "regions";
	private final static String FUNCTIONS = "functions";
	private final static String DATASETS = "datasets";
	private final static String ORIGIN = "origin";
	
	private static IOperationService  service;
	
	private final static Logger logger = LoggerFactory.getLogger(PersistJsonOperationsNode.class);
	
	public static void setOperationService(IOperationService s) {
		service = s;
	}
	
	public static IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(Tree file) throws Exception {
		NodeLink nl = file.findNodeLink(PersistenceConstants.PROCESS_ENTRY);
		GroupNode n = (GroupNode)nl.getDestination();
		return readOperations(n);
	}
	
	public static IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(GroupNode process) throws Exception{
		
		List<IOperation> opList = new ArrayList<IOperation>();

		
		Collection<String> memberList = process.getNames();

		int i = 0;
		String[] name = new String[]{PersistenceConstants.PROCESS_ENTRY+Node.SEPARATOR+ Integer.toString(i)};

		while (memberList.contains(Integer.toString(i))){
			GroupNode gn = (GroupNode)process.getNodeLink(Integer.toString(i)).getDestination();
			DataNode dataNode = gn.getDataNode(DATA);
			Dataset data = DatasetUtils.convertToDataset(dataNode.getDataset().getSlice());
			dataNode = gn.getDataNode(ID);
			Dataset id = DatasetUtils.convertToDataset(dataNode.getDataset().getSlice());
			String json = data.getObject().toString();
			String sid = id.getObject().toString();
			
			boolean p = false;
			boolean s = false;
			
			try {
				dataNode = gn.getDataNode(PASS);
				IDataset pass = dataNode.getDataset().getSlice();
				dataNode = gn.getDataNode(SAVE);
				IDataset save = dataNode.getDataset().getSlice();
				p = pass.getInt(0) == 0 ? false : true;
				s = save.getInt(0) == 0 ? false : true;
			} catch (Exception e) {
				logger.error("Could not read pass/save nodes", e);
			}
			
			IOperation op = service.create(sid);
			Class modelType = ((AbstractOperationBase)op).getModelClass();
			ObjectMapper mapper = getMapper();

			try {
			IOperationModel	 unmarshal = mapper.readValue(json, modelType);
			op.setModel(unmarshal);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Could not read model values", e);
				IOperationModel model  = (IOperationModel) modelType.newInstance();
				op.setModel(model);
			}
			
			op.setPassUnmodifiedData(p);
			op.setStoreOutput(s);
			
			readSpecial(op.getModel(),gn,name[0],REGIONS);
			readSpecial(op.getModel(),gn,name[0],FUNCTIONS);
			readSpecial(op.getModel(),gn,name[0],DATASETS);
			
			opList.add(op);

			name[0] = PersistenceConstants.PROCESS_ENTRY +"/"+ Integer.toString(++i);

		}

		return opList.isEmpty() ? null : opList.toArray(new IOperation[opList.size()]);
	}
	
	public static GroupNode writeOperationsToNode(IOperation<? extends IOperationModel, ? extends OperationData>... operations) {
		GroupNodeImpl process = new GroupNodeImpl(1);
		process.addAttribute(new AttributeImpl("NX_class", Nexus.PROCESS));
		
		for (int i = 0; i < operations.length; i++) {
			writeOperationToProcessGroup(process, i, operations[i]);
		}
		
		
		return process;
	}
	
	private static void writeOperationToProcessGroup(GroupNodeImpl n, int i, IOperation op) {
		String opId = op.getId();
		String name = op.getName();
		IDataset boolTrue = DatasetFactory.ones(new int[] {1}, Dataset.INT);
		IDataset boolFalse =  DatasetFactory.zeros(new int[] {1}, Dataset.INT);
		
		Map<Class, Map<String, Object>> specialObjects = getSpecialObjects(op.getModel());
		
		IDataset pass = op.isPassUnmodifiedData() ? boolTrue : boolFalse;
		IDataset save = op.isStoreOutput() ? boolTrue : boolFalse;
		GroupNodeImpl gn = new GroupNodeImpl(1);
		gn.addAttribute(new AttributeImpl("NX_class",Nexus.NOTE));
		n.addGroupNode(Integer.toString(i),gn);
		DataNodeImpl nameNode = new DataNodeImpl(1);
		nameNode.setDataset(DatasetFactory.createFromObject(name));
		gn.addDataNode(NAME, nameNode);
		
		DataNodeImpl opNode = new DataNodeImpl(1);
		opNode.setDataset(DatasetFactory.createFromObject(opId));
		gn.addDataNode(ID, opNode);
		
		DataNodeImpl saveNode = new DataNodeImpl(1);
		saveNode.setDataset(save);
		gn.addDataNode(SAVE, saveNode);
		
		DataNodeImpl passNode = new DataNodeImpl(1);
		passNode.setDataset(pass);
		gn.addDataNode(PASS, passNode);
		
		ObjectMapper mapper = getMapper();
		IJSonMarshaller converter = new JacksonMarshaller();
		
		try {
			Map<String, Object> m = specialObjects.get(IROI.class);
			writeSpecialObjects(m,REGIONS,gn,converter);
			m = specialObjects.get(IFunction.class);
			writeSpecialObjects(m,FUNCTIONS,gn,converter);
			m = specialObjects.get(IDataset.class);
			writeSpecialObjects(m,DATASETS,gn,converter);

			String modelJson = getModelJson(op.getModel(), mapper);
			DataNodeImpl json = new DataNodeImpl(1);
			json.setDataset(DatasetFactory.createFromObject(modelJson));
			gn.addDataNode(DATA, json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static void writeSpecialObjects(Map<String, Object> special, String type, GroupNodeImpl node,IJSonMarshaller converter) throws Exception  {
		 if (!special.isEmpty()) {
			 GroupNodeImpl gn = new GroupNodeImpl(1);
			 gn.addAttribute(new AttributeImpl("NX_class","NXcollection"));
			 node.addGroupNode(type, gn);

			 
			 for (Map.Entry<String, Object> entry : special.entrySet()) {
				    String key = entry.getKey();
				    Object value = entry.getValue();
				    if (value instanceof IDataset) {
				    	DataNodeImpl dn = new DataNodeImpl(1);
				    	dn.setDataset((IDataset)value);
				    	gn.addDataNode(key, dn);
	
				    } else {
				    	String json = converter.marshal(value);
				    	DataNodeImpl dn = new DataNodeImpl(1);
				    	dn.setDataset(DatasetFactory.createFromObject(json));
				    	gn.addDataNode(key, dn);
				    	
				    }
				}
		 }
	}
	
	public static OriginMetadata readOriginalDataInformation(String path) throws Exception {
		String fp = LoaderFactory.getDataSet(path,  PersistenceConstants.PROCESS_ENTRY + Node.SEPARATOR + ORIGIN+ Node.SEPARATOR + "path", null).getString(0);
		String dsn = LoaderFactory.getDataSet(path,  PersistenceConstants.PROCESS_ENTRY + Node.SEPARATOR + ORIGIN+ Node.SEPARATOR + "dataset", null).getString(0);
		String ss = LoaderFactory.getDataSet(path,  PersistenceConstants.PROCESS_ENTRY + Node.SEPARATOR + ORIGIN+ Node.SEPARATOR + "sampling", null).getString(0);
		IDataset dd = LoaderFactory.getDataSet(path,  PersistenceConstants.PROCESS_ENTRY + Node.SEPARATOR + ORIGIN + Node.SEPARATOR+ "data dimensions", null);
		int[] dataDims = (int[])DatasetUtils.cast(dd, Dataset.INT32).getBuffer();
		
		return MetadataFactory.createMetadata(OriginMetadata.class, null, Slice.convertFromString(ss), dataDims, fp, dsn);
	}
	
	public static GroupNode writeOriginalDataInformation(OriginMetadata origin) {
		GroupNode node = new GroupNodeImpl(1);
		node.addAttribute(new AttributeImpl("NX_class", Nexus.NOTE));
		DataNode dn = new DataNodeImpl(1);
		dn.setDataset(DatasetFactory.createFromObject(origin.getFilePath()));
		node.addDataNode("path", dn);
		
		dn = new DataNodeImpl(1);
		dn.setDataset(DatasetFactory.createFromObject(origin.getDatasetName()));
		node.addDataNode("dataset", dn);
		
		if (origin instanceof SliceFromSeriesMetadata) {
			dn = new DataNodeImpl(1);
			dn.setDataset(DatasetFactory.createFromObject(Slice.createString(((SliceFromSeriesMetadata)origin).getSliceInfo().getSubSampling())));
			node.addDataNode("sampling", dn);
		}
		
		dn = new DataNodeImpl(1);
		dn.setDataset(DatasetFactory.createFromObject(origin.getDataDimensions()));
		node.addDataNode("data_dimensions", dn);

		return node;
	}
	
	private static void readSpecial(IOperationModel model, GroupNode node, String name, String type) throws Exception {

		Collection<String> memberList = node.getNames();

		if (memberList.contains(type)) {
			GroupNode gn = (GroupNode)node.getNodeLink(type).getDestination();
			Collection<String> names = gn.getNames();
//			IDataset data = dataNode.getDataset().getSlice();
			for (String s : names) {
//				String rName = s.substring(s.lastIndexOf(SLASH)+1);
				if (model.isModelField(s)) {
					Dataset ob = DatasetUtils.convertToDataset(gn.getDataNode(s).getDataset().getSlice());
					if (type.equals(DATASETS)) {
						model.set(s, ob);
					} else {
						IJSonMarshaller converter = new JacksonMarshaller();
						model.set(s, converter.unmarshal(ob.getString()));
					}
				}
			}
		};

	}
	
	private static Map<Class,Map<String, Object>> getSpecialObjects(IOperationModel model) {

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
	
	private static ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.addMixInAnnotations(IDataset.class, MixIn.class);
		mapper.addMixInAnnotations(IROI.class, MixIn.class);
		mapper.addMixInAnnotations(IFunction.class, MixIn.class);
		return mapper;
	}
	
	public static String getModelJson(IOperationModel model) throws Exception {

		ObjectMapper mapper = getMapper();

		return mapper.writeValueAsString(model);
	}
	
	private static String getModelJson(IOperationModel model, ObjectMapper mapper) throws Exception {

		return mapper.writeValueAsString(model);
	}
	
	@JsonIgnoreType abstract class MixIn{};
	
}
