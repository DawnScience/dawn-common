package org.dawnsci.persistence.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.dawb.common.util.eclipse.BundleUtils;
import org.dawnsci.persistence.ServiceLoader;
import org.dawnsci.persistence.json.JacksonMarshaller;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperationBase;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.DataNodeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.GroupNodeImpl;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.metadata.OriginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

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
	private final static String VERSION = "version";
	private final static String DATE = "date";
	private final static String PROGRAM = "program";
	private final static String TYPE = "type";
	private final static String DAWN = "DAWN";
	private final static Dataset JSON_MIME_TYPE = DatasetFactory.createFromObject("application/json");

	private final static Dataset NULL = DatasetFactory.createFromObject("null");

	private static IOperationService  service;
	
	private final static Logger logger = LoggerFactory.getLogger(PersistJsonOperationsNode.class);
	
	public void setOperationService(IOperationService s) {
		service = s;
	}

	public static IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(Tree file) throws Exception {
		GroupNode g = NexusTreeUtils.findFirstEntryWithProcess(file.getGroupNode());
		if (g == null) {
			throw new NexusException("No entries with process groups have been found");
		}
		NodeLink p = NexusTreeUtils.findFirstNode(g, NexusConstants.PROCESS);
		return readOperations((GroupNode) p.getDestination());
	}

	public static IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(GroupNode process) throws Exception{
		List<IOperation<?, ?>> opList = new ArrayList<IOperation<?, ?>>();

		
		Collection<String> memberList = process.getNames();

		int i = 0;
		for (String number = Integer.toString(i); memberList.contains(number); number = Integer.toString(++i)) {
			GroupNode gn = (GroupNode)process.getNodeLink(number).getDestination();
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
			
			@SuppressWarnings("rawtypes")
			IOperation op = service.create(sid);
			Class<?> modelType = ((AbstractOperationBase<?, ?>)op).getModelClass();
			ObjectMapper mapper = getMapper();

			try {
				IOperationModel unmarshal = (IOperationModel) mapper.readValue(json, modelType);
				op.setModel(unmarshal);
			} catch (Exception e) {
				logger.error("Could not read model values", e);
				IOperationModel model  = (IOperationModel) modelType.newInstance();
				op.setModel(model);
			}
			
			op.setPassUnmodifiedData(p);
			op.setStoreOutput(s);
			
			readSpecial(op.getModel(), gn, REGIONS);
			readSpecial(op.getModel(), gn, FUNCTIONS);
			readSpecial(op.getModel(), gn, DATASETS);
			
			opList.add(op);
		}

		return opList.isEmpty() ? null : opList.toArray(new IOperation[opList.size()]);
	}
	
	public static GroupNode writeOperationsToNode(IOperation<? extends IOperationModel, ? extends OperationData>... operations) {
		GroupNodeImpl process = new GroupNodeImpl(1);
		process.addAttribute(new AttributeImpl(NexusConstants.NXCLASS, NexusConstants.PROCESS));
		
		for (int i = 0; i < operations.length; i++) {
			addOperationToProcessGroup(process, i, operations[i]);
		}
		
		try {
			
			DataNodeImpl n = new DataNodeImpl(1);
			n.setDataset(DatasetFactory.createFromObject(DAWN));
			process.addDataNode(PROGRAM, n);
			String date = String.format("%tFT%<tR", Calendar.getInstance(TimeZone.getDefault()));
			
			DataNodeImpl ndate = new DataNodeImpl(1);
			ndate.setDataset(DatasetFactory.createFromObject(date));
			process.addDataNode(DATE, ndate);
			
			String v = BundleUtils.getDawnVersion();
			if (v != null) {
				DataNodeImpl node = new DataNodeImpl(1);
				node.setDataset(DatasetFactory.createFromObject(v));
				process.addDataNode(VERSION, node);
			}
			
		} catch (Exception e) {
			logger.debug("Could not read version number",e);
		}
		
		return process;
	}
	
	private static void addOperationToProcessGroup(GroupNodeImpl n, int i, IOperation<?, ?> op) {
		String opId = op.getId();
		String name = op.getName();
		IDataset boolTrue = DatasetFactory.ones(IntegerDataset.class, 1);
		IDataset boolFalse =  DatasetFactory.zeros(IntegerDataset.class, 1);
		
		Map<Class<?>, Map<String, Object>> specialObjects = getSpecialObjects(op.getModel());
		
		IDataset pass = op.isPassUnmodifiedData() ? boolTrue : boolFalse;
		IDataset save = op.isStoreOutput() ? boolTrue : boolFalse;
		GroupNodeImpl gn = new GroupNodeImpl(1);
		gn.addAttribute(new AttributeImpl(NexusConstants.NXCLASS, NexusConstants.NOTE));
		n.addGroupNode(Integer.toString(i),gn);
		DataNodeImpl nameNode = new DataNodeImpl(1);
		nameNode.setDataset(name == null ? NULL : DatasetFactory.createFromObject(name));
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
			addSpecialObjects(m,REGIONS,gn,converter);
			m = specialObjects.get(IFunction.class);
			addSpecialObjects(m,FUNCTIONS,gn,converter);
			m = specialObjects.get(IDataset.class);
			addSpecialObjects(m,DATASETS,gn,converter);

			DataNodeImpl typeNode = new DataNodeImpl(1);
			typeNode.setDataset(JSON_MIME_TYPE);
			gn.addDataNode(TYPE, typeNode);

			String modelJson = getModelJson(op.getModel(), mapper);
			DataNodeImpl json = new DataNodeImpl(1);
			json.setDataset(DatasetFactory.createFromObject(modelJson));
			gn.addDataNode(DATA, json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addSpecialObjects(Map<String, Object> special, String type, GroupNodeImpl node,IJSonMarshaller converter) throws Exception  {
		if (!special.isEmpty()) {
			GroupNodeImpl gn = new GroupNodeImpl(1);
			gn.addAttribute(new AttributeImpl(NexusConstants.NXCLASS, NexusConstants.COLLECTION));
			node.addGroupNode(type, gn);

			for (Map.Entry<String, Object> entry : special.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof IDataset) {
					DataNodeImpl dn = new DataNodeImpl(1);
					dn.setDataset((IDataset) value);
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

	/**
	 * Read information about original data
	 * @param path
	 * @param group NXprocess group
	 * @return origin metadata
	 * @throws Exception
	 */
	public static OriginMetadata readOriginalDataInformation(String path) throws Exception {
		try (NexusFile nexusFile = ServiceLoader.getNexusFactory().newNexusFile(path)) {
			nexusFile.openToRead();
			GroupNode group = NexusUtils.loadGroupFully(nexusFile, PersistenceConstants.PROCESS_ENTRY, 1);

			return readOriginalDataInformation(group);
		}
	}

	/**
	 * Read information about original data
	 * @param group NXprocess group
	 * @return origin metadata
	 * @throws DatasetException
	 * @throws MetadataException
	 */
	public static OriginMetadata readOriginalDataInformation(GroupNode group) throws DatasetException, MetadataException {
		GroupNode origin = group.getGroupNode(ORIGIN);
		if (origin == null) {
			return null;
		}
		
		String fp = origin.getDataNode("path").getString();
		String dsn = origin.getDataNode("dataset").getString();
		String ss = origin.getDataNode("sampling").getString();
		
		Dataset dd = DatasetUtils.sliceAndConvertLazyDataset(origin.getDataNode("data_dimensions").getDataset());
		int[] dataDims = dd.cast(IntegerDataset.class).getData();
		
		return MetadataFactory.createMetadata(OriginMetadata.class, null, Slice.convertFromString(ss), dataDims, fp, dsn);
	}

	public static GroupNode writeOriginalDataInformation(OriginMetadata origin) {
		GroupNode node = new GroupNodeImpl(1);
		node.addAttribute(new AttributeImpl(NexusConstants.NXCLASS, NexusConstants.NOTE));
		DataNode dn = new DataNodeImpl(1);
		String text = origin.getFilePath();
		dn.setDataset(text == null ? NULL : DatasetFactory.createFromObject(text));
		node.addDataNode("path", dn);
		
		dn = new DataNodeImpl(1);
		text = origin.getDatasetName();
		dn.setDataset(text == null ? NULL : DatasetFactory.createFromObject(text));
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

	private static void readSpecial(IOperationModel model, GroupNode node, String type) throws Exception {
		Collection<String> memberList = node.getNames();

		if (memberList.contains(type)) {
			GroupNode gn = (GroupNode) node.getNodeLink(type).getDestination();
			Collection<String> names = gn.getNames();
			for (String s : names) {
				if (model.isModelField(s)) {
					Dataset ob = DatasetUtils.sliceAndConvertLazyDataset(gn.getDataNode(s).getDataset());
					if (type.equals(DATASETS)) {
						model.set(s, ob);
					} else {
						IJSonMarshaller converter = new JacksonMarshaller();
						model.set(s, converter.unmarshal(ob.getString()));
					}
				}
			}
		}
	}

	private static Map<Class<?>,Map<String, Object>> getSpecialObjects(IOperationModel model) {

		Map<Class<?>,Map<String, Object>> out = new HashMap<>();
		out.put(IROI.class, new HashMap<String, Object>());
		out.put(IDataset.class, new HashMap<String, Object>());
		out.put(IFunction.class, new HashMap<String, Object>());
		
		final List<Field> allFields = new ArrayList<Field>(31);
		Class<?> klazz = model.getClass();
		do {
			for (Field f : klazz.getDeclaredFields()) {
				allFields.add(f);
			}
			klazz = klazz.getSuperclass();
		} while(klazz != AbstractOperationModel.class && klazz != null);

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
