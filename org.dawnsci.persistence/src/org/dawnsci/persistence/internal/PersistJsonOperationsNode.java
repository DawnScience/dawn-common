package org.dawnsci.persistence.internal;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.util.eclipse.BundleUtils;
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
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.ac.diamond.osgi.services.ServiceProvider;
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
	private final static String AUTOCONFIG = "auto_configured";

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

	@SuppressWarnings("unchecked")
	public static IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(GroupNode process) throws Exception{
		List<IOperation<?, ?>> opList = new ArrayList<IOperation<?, ?>>();

		
		Collection<String> memberList = process.getNames();

		int i = 0;
		for (String number = Integer.toString(i); memberList.contains(number); number = Integer.toString(++i)) {
			GroupNode gn = process.getGroupNode(number);
			DataNode dataNode = gn.getDataNode(DATA);
			Dataset data = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			dataNode = gn.getDataNode(ID);
			Dataset id = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			String json = data.getString();
			String sid = id.getString();
			
			boolean p = false;
			boolean s = false;
			
			try {
				dataNode = gn.getDataNode(PASS);
				Dataset pass = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
				p = pass.getBoolean(0);
				dataNode = gn.getDataNode(SAVE);
				Dataset save = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
				s = save.getBoolean(0);
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

			IJSonMarshaller converter = new JacksonMarshaller();
			readSpecial(converter, op.getModel(), gn, REGIONS);
			readSpecial(converter, op.getModel(), gn, FUNCTIONS);
			readSpecial(converter, op.getModel(), gn, DATASETS);
			
			opList.add(op);
		}

		return opList.isEmpty() ? null : opList.toArray(new IOperation[opList.size()]);
	}

	public static boolean hasConfiguredFields(GroupNode process) throws Exception {
		Collection<String> memberList = process.getNames();

		int i = 0;
		for (String number = Integer.toString(i); memberList.contains(number); number = Integer.toString(++i)) {
			GroupNode opNote = process.getGroupNode(number);
			if (opNote.containsGroupNode(AUTOCONFIG)) {
				return true;
			}
			
		}
		return false;
	}

	public static void applyConfiguredFields(GroupNode process, IOperation<? extends IOperationModel, ? extends OperationData>[] ops) throws Exception {
		Collection<String> memberList = process.getNames();

		int i = 0;
		for (String number = Integer.toString(i); memberList.contains(number); number = Integer.toString(++i)) {
			GroupNode opNote = process.getGroupNode(number);
			Map<String, Serializable> config = readAutoConfiguredFields(opNote);
			if (config != null) {
				applyConfiguredFields(ops[i].getModel(), config);
			}
		}
	}

	private static void applyConfiguredFields(IOperationModel model, Map<String, Serializable> config) {
		for (Entry<String, Serializable> e : config.entrySet()) {
			try {
				model.set(e.getKey(), e.getValue());
			} catch (Exception ex) {
				logger.error("Could not apply configured field {}={}", e.getKey(), e.getValue(), ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static GroupNode writeOperationsToNode(IOperation<? extends IOperationModel, ? extends OperationData>... operations) {
		GroupNode process = NexusUtils.createNXclass(NexusConstants.PROCESS);

		for (int i = 0; i < operations.length; i++) {
			GroupNode gn = createOperationInGroup(operations[i]);
			process.addGroupNode(Integer.toString(i), gn);
		}

		try {
			DataNode n = TreeFactory.createDataNode(1);
			n.setDataset(DatasetFactory.createFromObject(DAWN));
			process.addDataNode(PROGRAM, n);

			// date-time to minutes with time-zone offset
			String date = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
			DataNode ndate = TreeFactory.createDataNode(1);
			ndate.setDataset(DatasetFactory.createFromObject(date));
			process.addDataNode(DATE, ndate);
			
			String v = BundleUtils.getDawnVersion();
			if (v != null) {
				DataNode node = TreeFactory.createDataNode(1);
				node.setDataset(DatasetFactory.createFromObject(v));
				process.addDataNode(VERSION, node);
			}
			
		} catch (Exception e) {
			logger.debug("Could not read version number",e);
		}
		
		return process;
	}
	
	private static GroupNode createOperationInGroup(IOperation<?, ?> op) {
		String opId = op.getId();
		String name = op.getName();
		IDataset boolTrue = DatasetFactory.ones(IntegerDataset.class, 1);
		IDataset boolFalse =  DatasetFactory.zeros(IntegerDataset.class, 1);
		
		IDataset pass = op.isPassUnmodifiedData() ? boolTrue : boolFalse;
		IDataset save = op.isStoreOutput() ? boolTrue : boolFalse;
		GroupNode gn = NexusUtils.createNXclass(NexusConstants.NOTE);
		DataNode nameNode = TreeFactory.createDataNode(1);
		nameNode.setDataset(name == null ? NULL : DatasetFactory.createFromObject(name));
		gn.addDataNode(NAME, nameNode);
		
		DataNode opNode = TreeFactory.createDataNode(1);
		opNode.setDataset(DatasetFactory.createFromObject(opId));
		gn.addDataNode(ID, opNode);
		
		DataNode saveNode = TreeFactory.createDataNode(1);
		saveNode.setDataset(save);
		gn.addDataNode(SAVE, saveNode);
		
		DataNode passNode = TreeFactory.createDataNode(1);
		passNode.setDataset(pass);
		gn.addDataNode(PASS, passNode);
		
		ObjectMapper mapper = getMapper();
		IJSonMarshaller converter = new JacksonMarshaller();
		Map<Class<?>, Map<String, Object>> specialObjects = getSpecialObjects(op.getModel());
		
		addModelFields(op, gn, mapper, converter, specialObjects);

		return gn;
	}

	/**
	 * Write auto-configured fields in group to operation note
	 * @param note
	 * @param fields
	 */
	public static GroupNode writeAutoConfiguredFieldsToNode(Map<String, Serializable> fields) {
		GroupNode config = NexusUtils.createNXclass(NexusConstants.NOTE);
		DataNode typeNode = TreeFactory.createDataNode(1);
		typeNode.setDataset(JSON_MIME_TYPE);
		config.addDataNode(TYPE, typeNode);
		ObjectMapper mapper = getMapper();
		IJSonMarshaller converter = new JacksonMarshaller();
		addModelFields(config, mapper, converter, fields);

		GroupNode note = TreeFactory.createGroupNode(1);
		note.addGroupNode(AUTOCONFIG, config);
		return note;
	}

	private static void addModelFields(IOperation<?, ?> op, GroupNode gn, ObjectMapper mapper,
			IJSonMarshaller converter, Map<Class<?>, Map<String, Object>> specialObjects) {
		try {
			Map<String, Object> m = specialObjects.get(IROI.class);
			addSpecialObjects(m, REGIONS, gn, converter);
			m = specialObjects.get(IFunction.class);
			addSpecialObjects(m, FUNCTIONS, gn, converter);
			m = specialObjects.get(IDataset.class);
			addSpecialObjects(m, DATASETS, gn, converter);

			DataNode typeNode = TreeFactory.createDataNode(1);
			typeNode.setDataset(JSON_MIME_TYPE);
			gn.addDataNode(TYPE, typeNode);

			String modelJson = mapper.writeValueAsString(op.getModel());
			DataNode json = TreeFactory.createDataNode(1);
			json.setDataset(DatasetFactory.createFromObject(modelJson));
			gn.addDataNode(DATA, json);
		} catch (Exception e) {
			logger.warn("Could not add model fields", e);
		}
	}

	private static void addModelFields(GroupNode gn, ObjectMapper mapper,
			IJSonMarshaller converter, Map<String, Serializable> fields) {

		Map<String, Object> nonSpecials = new HashMap<>();
		for (Entry<String, Serializable> e : fields.entrySet()) {
			String k = e.getKey();
			Object v = e.getValue();
			GroupNode sg = null;
			Class<? extends Object> c = v.getClass();
			if (IROI.class.isAssignableFrom(c)) {
				sg = requireNXcollection(gn, REGIONS);
			} else if (IDataset.class.isAssignableFrom(c)) {
				sg = requireNXcollection(gn, DATASETS);
			} else if (IFunction.class.isAssignableFrom(c)) {
				sg = requireNXcollection(gn, FUNCTIONS);
			}

			if (sg == null) {
				nonSpecials.put(k,  v);
			} else {
				try {
					addFieldNode(converter, sg, k, v);
				} catch (Exception ex) {
					logger.error("Could not add field {}", k, ex);
				}
			}
		}

		if (!nonSpecials.isEmpty()) {
			try {
				String modelJson = mapper.writeValueAsString(nonSpecials);
				DataNode json = TreeFactory.createDataNode(1);
				json.setDataset(DatasetFactory.createFromObject(modelJson));
				gn.addDataNode(DATA, json);
			} catch (JsonProcessingException ex) {
				logger.error("Could not add configured fields in {}", DATA, ex);
			}
		}
	}

	private static GroupNode requireNXcollection(GroupNode p, String name) {
		GroupNode g = p.getGroupNode(name);
		if (g == null) {
			g = NexusUtils.createNXclass(NexusConstants.COLLECTION);
			p.addGroupNode(name, g);
		}
		return g;
	}

	private static void addSpecialObjects(Map<String, Object> special, String type, GroupNode node, IJSonMarshaller converter) throws Exception  {
		if (!special.isEmpty()) {
			GroupNode gn = requireNXcollection(node, type);

			for (Map.Entry<String, Object> entry : special.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof IDataset) {
					DataNode dn = TreeFactory.createDataNode(1);
					dn.setDataset((IDataset) value);
					gn.addDataNode(key, dn);
				} else {
					String json = converter.marshal(value);
					DataNode dn = TreeFactory.createDataNode(1);
					dn.setDataset(DatasetFactory.createFromObject(json));
					gn.addDataNode(key, dn);
				}
				addFieldNode(converter, gn, key, value);
			}
		}
	}

	private static void addFieldNode(IJSonMarshaller converter, GroupNode gn, String key, Object value)
			throws Exception {
		IDataset d;
		if (value instanceof IDataset) {
			d = (IDataset) value;
		} else {
			String json = converter.marshal(value);
			d = DatasetFactory.createFromObject(json);
		}
		DataNode dn = TreeFactory.createDataNode(1);
		dn.setDataset(d);
		gn.addDataNode(key, dn);
	}

	/**
	 * Read auto-configured operation fields
	 * @param opNote
	 * @return map of fields
	 * @throws Exception
	 */
	public static Map<String, Serializable> readAutoConfiguredFields(GroupNode opNote) {
		if (!opNote.containsGroupNode(AUTOCONFIG)) {
			return null;
		}

		GroupNode configured = opNote.getGroupNode(AUTOCONFIG);
		ObjectMapper mapper = getMapper();

		Map<String, Serializable> fields;
		String json = null;
		try {
			DataNode dataNode = configured.getDataNode(DATA);
			Dataset data = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			json = data.getString();
			fields = (Map) mapper.readValue(json, Map.class);
		} catch (Exception e) {
			if (json == null) {
				logger.error("Could not read JSON from {}", DATA, e);
			} else {
				logger.error("Could not read values from {}", json, e);
			}
			fields = new HashMap<>();
		}

		IJSonMarshaller converter = new JacksonMarshaller();
		readSpecial(fields, converter, configured, REGIONS);
		readSpecial(fields, converter, configured, FUNCTIONS);
		readSpecial(fields, converter, configured, DATASETS);

		return fields;
	}

	/**
	 * Read information about original data
	 * @param path
	 * @param group NXprocess group
	 * @return origin metadata
	 * @throws Exception
	 */
	public static OriginMetadata readOriginalDataInformation(String path) throws Exception {
		try (NexusFile nexusFile = ServiceProvider.getService(INexusFileFactory.class).newNexusFile(path)) {
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
		GroupNode node = NexusUtils.createNXclass(NexusConstants.NOTE);
		DataNode dn = TreeFactory.createDataNode(1);
		String text = origin.getFilePath();
		dn.setDataset(text == null ? NULL : DatasetFactory.createFromObject(text));
		node.addDataNode("path", dn);
		
		dn = TreeFactory.createDataNode(1);
		text = origin.getDatasetName();
		dn.setDataset(text == null ? NULL : DatasetFactory.createFromObject(text));
		node.addDataNode("dataset", dn);
		
		if (origin instanceof SliceFromSeriesMetadata) {
			dn = TreeFactory.createDataNode(1);
			dn.setDataset(DatasetFactory.createFromObject(Slice.createString(((SliceFromSeriesMetadata)origin).getSliceInfo().getSubSampling())));
			node.addDataNode("sampling", dn);
		}
		
		dn = TreeFactory.createDataNode(1);
		dn.setDataset(DatasetFactory.createFromObject(origin.getDataDimensions()));
		node.addDataNode("data_dimensions", dn);

		return node;
	}

	private static void readSpecial(IJSonMarshaller converter, IOperationModel model, GroupNode node, String type) throws Exception {
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
						model.set(s, converter.unmarshal(ob.getString()));
					}
				}
			}
		}
	}

	private static void readSpecial(Map<String, Serializable> map, IJSonMarshaller converter, GroupNode node, String type) {
		Collection<String> memberList = node.getNames();

		if (memberList.contains(type)) {
			GroupNode gn = (GroupNode) node.getNodeLink(type).getDestination();
			Collection<String> names = gn.getNames();
			for (String s : names) {
				try {
					Dataset ob = DatasetUtils.sliceAndConvertLazyDataset(gn.getDataNode(s).getDataset());
					if (type.equals(DATASETS)) {
						map.put(s, ob);
					} else {
						map.put(s, (Serializable) converter.unmarshal(ob.getString()));
					}
				} catch (Exception e) {
					logger.error("Could not read field {}", s, e);
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
				} catch (Exception e) {
					logger.warn("Could not get region from model", e);
				}
			} else if (IDataset.class.isAssignableFrom(class1)) {
				try {
					out.get(IDataset.class).put(field.getName(), model.get(field.getName()));
				} catch (Exception e) {
					logger.warn("Could not get dataset from model", e);
				}
			} else if (IFunction.class.isAssignableFrom(class1)) {
				try {
					out.get(IFunction.class).put(field.getName(), model.get(field.getName()));
				} catch (Exception e) {
					logger.warn("Could not get function from model", e);
				}
			}
		}
		
		return out;
	}

	private static ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.addMixIn(IDataset.class, MixIn.class);
		mapper.addMixIn(IROI.class, MixIn.class);
		mapper.addMixIn(IFunction.class, MixIn.class);
		return mapper;
	}

	public static String getModelJson(IOperationModel model) throws Exception {
		ObjectMapper mapper = getMapper();
		return mapper.writeValueAsString(model);
	}

	@JsonIgnoreType abstract class MixIn{};
}
