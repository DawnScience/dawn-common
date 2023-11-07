package org.dawnsci.persistence.test.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.dawnsci.persistence.internal.PersistJsonOperationsNode;
import org.dawnsci.persistence.internal.PersistenceConstants;
import org.dawnsci.persistence.internal.PersistenceServiceImpl;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperationBase;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.OriginMetadata;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
import uk.ac.diamond.scisoft.analysis.processing.OperationServiceImpl;
import uk.ac.diamond.scisoft.analysis.processing.operations.FunctionModel;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReadWriteOperationTest {
	
	private static IOperationService   service;
	private static IPersistenceService pservice;
	
	@BeforeClass
	public static void before() throws Exception {
		service = new OperationServiceImpl();
		service.createOperations(ReadWriteOperationTest.class, "org.dawnsci.persistence.test.operations");
		service.createOperations(service.getClass(), "uk.ac.diamond.scisoft.analysis.processing.operations");
		new PersistJsonOperationsNode().setOperationService(service);
		pservice = new PersistenceServiceImpl();
		
		/*FunctionFactory has been set up as an OSGI service so need to register
		 *function before it is called (or make this a JUnit PluginTest.
		 */
		FunctionFactory.registerFunction(Polynomial.class, true);
	}

	@Test
	public void testFunctionSimple() throws Exception {
		
		final IOperation functionOp = service.findFirst("function");
		
		// y(x) = a_0 x^n + a_1 x^(n-1) + a_2 x^(n-2) + ... + a_(n-1) x + a_n
		final IFunction poly = FunctionFactory.getFunction("Polynomial", 3/*x^2*/, 5.3/*x*/, 9.4/*m*/);
		functionOp.setModel(new FunctionModel(poly));

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();
		try (IPersistentFile persist = pservice.createPersistentFile(NexusFileHDF5.createNexusFile(tmp.getAbsolutePath()))) {
			persist.setOperations(functionOp);
		}

		try (IPersistentFile persist = pservice.createPersistentFile(NexusFileHDF5.openNexusFile(tmp.getAbsolutePath()))) {
			IOperation[] readOperations = persist.getOperations();

			final FunctionModel model = (FunctionModel) readOperations[0].getModel();
			if (!poly.equals(model.getFunction())) {
				throw new Exception("Cannot get function from serialized file!");
			}
		}
	}

	@Test
	public void testWriteReadOperations() throws Exception {
		IOperation op = service.create("org.dawnsci.persistence.test.operations.JunkTestOperation");

		AbstractOperationBase aop = null;
		if (op instanceof AbstractOperationBase) {
			aop = (AbstractOperationBase) op;
		} else {
			fail("Operation must extend AbstractOperationBase");
		}

		Class<?> modelType = aop.getModelClass();
		assertEquals(JunkTestOperationModel.class, modelType);

		IOperationModel model2  = (IOperationModel) modelType.getConstructor().newInstance();
		op.setModel(model2);
		((JunkTestOperationModel)model2).setxDim(50);

		String modelJson = PersistJsonOperationsNode.getModelJson(model2);

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();

		try (NexusFile file = NexusFileHDF5.createNexusFile(tmp.getAbsolutePath())) {
			writeOperations(file, op);
	
			GroupNode g = NexusUtils.loadGroupFully(file, PersistenceConstants.PROCESS_ENTRY, 3);
			assertEquals(modelJson, g.getGroupNode("0").getDataNode("data").getString());

			IOperation[] readOperations = PersistJsonOperationsNode.readOperations(g);
			assertEquals(((JunkTestOperationModel)(readOperations[0].getModel())).getxDim(), 50);
		}
	}

	@Test
	public void testWriteReadAutoConfigureOperations() throws Exception {
		IOperation op = service.create("org.dawnsci.persistence.test.operations.DefaultAutoOperation");
		Class<?> modelType = (Class<?>) ((ParameterizedType)op.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		DefaultAutoModel iModel  = (DefaultAutoModel) modelType.getConstructor().newInstance();
		op.setModel(iModel);
		iModel.setxDim(50);

		String modelJson = PersistJsonOperationsNode.getModelJson(iModel);

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();

		try (NexusFile file = NexusFileHDF5.createNexusFile(tmp.getAbsolutePath())) {
			int[] shape = new int[] {24, 1000};
			int[] oShape = new int[] {shape[0]};
			int current = 0;
			Slice slice = new Slice(current, current + 1);
	
			SliceInformation si = new SliceInformation(new SliceND(shape, slice),
					new SliceND(oShape, slice), new SliceND(shape), new int[] {1}, oShape[0], current);
			SliceFromSeriesMetadata ssm = new SliceFromSeriesMetadata(si);
			IntegerDataset data = DatasetFactory.createRange(IntegerDataset.class, shape[1]);
			data.addMetadata(ssm);
			OperationData od = op.execute(data, null);
	
			writeOperations(file, op);
			writeAutoConfiguredFields(file, od.getConfiguredFields());
	
			GroupNode g = NexusUtils.loadGroupFully(file, PersistenceConstants.PROCESS_ENTRY, 3);
			assertEquals(modelJson, g.getGroupNode("0").getDataNode("data").getString());

			IOperation[] readOperations = PersistJsonOperationsNode.readOperations(g);
			DefaultAutoModel oModel = (DefaultAutoModel) readOperations[0].getModel();
			assertEquals(iModel.getxDim(), oModel.getxDim());
			assertEquals(iModel.getFile(), oModel.getFile());
			assertEquals(iModel.getRoiA(), oModel.getRoiA());
			assertEquals(iModel.getValue(), oModel.getValue());
	
			assertTrue(PersistJsonOperationsNode.hasConfiguredFields(g));
			PersistJsonOperationsNode.applyConfiguredFields(g, readOperations);
			assertEquals(iModel.getxDim(), oModel.getxDim());
			assertEquals("test.dat", oModel.getFile());
			assertEquals(new RectangularROI(shape[1], 0), oModel.getRoiA());
			assertEquals(Double.valueOf(0), oModel.getValue());
		}
	}

	@Test
	public void testWriteReadOperationRoiFuncData() throws Exception {
		IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperationROI");
		Class<?> modelType = (Class<?>) ((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		JunkTestModelROI model2  = (JunkTestModelROI) modelType.getConstructor().newInstance();

		model2.setRoi(new SectorROI());
		model2.setxDim(50);
		model2.setBar(new Gaussian());
		model2.setFoo(DatasetFactory.createRange(IntegerDataset.class, 10));
		model2.setData(DatasetFactory.createRange(IntegerDataset.class, 5));
		model2.setFunc(new Lorentzian());
		model2.setRoi2(new RectangularROI());
		op2.setModel(model2);

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();

		try (NexusFile file = NexusFileHDF5.createNexusFile(tmp.getAbsolutePath())) {
			writeOperations(file, op2);
	
			GroupNode g = NexusUtils.loadGroupFully(file, PersistenceConstants.PROCESS_ENTRY, 3);
			IOperation[] readOperations = PersistJsonOperationsNode.readOperations(g);
			JunkTestModelROI mo = (JunkTestModelROI)readOperations[0].getModel();
			assertEquals(mo.getxDim(), 50);
			assertTrue(mo.getRoi() != null);
			assertTrue(mo.getBar() != null);
			assertTrue(mo.getFoo() != null);
			assertTrue(mo.getData() != null);
			assertTrue(mo.getFunc() != null);
			assertTrue(mo.getRoi2() != null);
		}
	}

	@Test
	public void testWriteReadOperationRoiFuncDataNode() throws Exception {
		IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperationROI");
		Class<?> modelType = (Class<?>) ((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		JunkTestModelROI model2  = (JunkTestModelROI) modelType.getConstructor().newInstance();

		model2.setRoi(new SectorROI());
		model2.setxDim(50);
		model2.setBar(new Gaussian());
		model2.setFoo(DatasetFactory.createRange(IntegerDataset.class, 10));
		model2.setData(DatasetFactory.createRange(IntegerDataset.class, 5));
		model2.setFunc(new Lorentzian());
		model2.setRoi2(new RectangularROI());
		op2.setModel(model2);

		GroupNode n = PersistJsonOperationsNode.writeOperationsToNode(op2);
		
		IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations = PersistJsonOperationsNode.readOperations(n);
		
		JunkTestModelROI mo = (JunkTestModelROI)readOperations[0].getModel();
		assertEquals(mo.getxDim(), 50);
		assertTrue(mo.getRoi() != null);
		assertTrue(mo.getBar() != null);
		assertTrue(mo.getFoo() != null);
		assertTrue(mo.getData() != null);
		assertTrue(mo.getFunc() != null);
		assertTrue(mo.getRoi2() != null);
	}

	@Test
	public void testWriteOrigin() throws Exception {
		Slice[] slices = Slice.convertFromString("0:10:2,2:20,:,:");
		int[] shape = new int[]{100,100,100,100};
		int[] dataDims = new int[]{2,3};
		String path = "pathvalue";
		String dsname = "dsname";

		SliceInformation si = new SliceInformation(null, null, new SliceND(shape,slices), dataDims, 100*100, 200);
		SourceInformation so = new SourceInformation(path, dsname, null);
		SliceFromSeriesMetadata ssm = new SliceFromSeriesMetadata(so,si);

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();

		try (NexusFile file = NexusFileHDF5.createNexusFile(tmp.getAbsolutePath())) {
			writeOriginalDataInformation(file, ssm);
		}

		OriginMetadata outOm = PersistJsonOperationsNode.readOriginalDataInformation(tmp.getAbsolutePath());
		outOm.toString();
	}

	private static final String PROCESSED_ENTRY = Tree.ROOT + "processed" + NexusFile.NXCLASS_SEPARATOR + NexusConstants.ENTRY;
	private static final String ORIGIN = "origin";
	private static final String PROCESS = "process";
	
	private static void writeOperations(NexusFile file, IOperation<? extends IOperationModel, ? extends OperationData>... operations) throws Exception {
		GroupNode entryGroup = file.getGroup(PROCESSED_ENTRY, true);
		GroupNode processGroup = PersistJsonOperationsNode.writeOperationsToNode(operations);
		file.addNode(entryGroup, PROCESS, processGroup);
	}

	private void writeAutoConfiguredFields(NexusFile file, Map<String, Serializable> configuredFields) throws Exception {
		GroupNode ac = PersistJsonOperationsNode.writeAutoConfiguredFieldsToNode(configuredFields);
		String n = ac.getNames().iterator().next();
		GroupNode note = file.getGroup(PROCESSED_ENTRY + Node.SEPARATOR + PROCESS + Node.SEPARATOR + "0", false);
		file.addNode(note, n, ac.getGroupNode(n));
	}

	private static void writeOriginalDataInformation(NexusFile file, OriginMetadata origin) throws Exception {
		GroupNode groupEntry = file.getGroup(PROCESSED_ENTRY, true);
		
		GroupNode processNode = file.getGroup(groupEntry, PROCESS, NexusConstants.PROCESS, true);
		GroupNode originNode = PersistJsonOperationsNode.writeOriginalDataInformation(origin);
		file.addNode(processNode, ORIGIN, originNode);
	}
}
