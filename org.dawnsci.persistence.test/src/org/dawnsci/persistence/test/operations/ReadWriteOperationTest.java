package org.dawnsci.persistence.test.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.ParameterizedType;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.persistence.internal.PersistJsonOperationHelper;
import org.dawnsci.persistence.internal.PersistJsonOperationsNode;
import org.dawnsci.persistence.internal.PersistenceServiceImpl;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf.object.IHierarchicalDataFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.OriginMetadata;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.processing.OperationServiceImpl;
import uk.ac.diamond.scisoft.analysis.processing.operations.FunctionModel;

public class ReadWriteOperationTest {
	
	
	private static IOperationService   service;
	private static IPersistenceService pservice;
	
	@BeforeClass
	public static void before() throws Exception {

		ServiceManager.setService(IOperationService.class, new OperationServiceImpl());
		service = (IOperationService)ServiceManager.getService(IOperationService.class);
		service.createOperations(service.getClass().getClassLoader(), "org.dawnsci.persistence.test.operations");
		service.createOperations(service.getClass().getClassLoader(), "uk.ac.diamond.scisoft.analysis.processing.operations");
		PersistJsonOperationsNode.setOperationService(service);
		
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
		IHierarchicalDataFile filewriter = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());
		IPersistentFile persist = pservice.createPersistentFile(filewriter);
		try {
			persist.setOperations(functionOp);
			
		} finally {
			persist.close();
		}
		
		IHierarchicalDataFile filereader = HierarchicalDataFactory.getReader(tmp.getAbsolutePath());
		persist = pservice.createPersistentFile(filereader);
		try {

			IOperation[] readOperations = persist.getOperations();

			final FunctionModel model = (FunctionModel)readOperations[0].getModel();
            if (!poly.equals(model.getFunction())) {
            	throw new Exception("Cannot get function from serialized file!");
            }
            
		} finally {
			persist.close();
		}
	}
	
	@Test
	public void testWriteReadOperations() throws Exception {
		IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperation");
		Class modelType = (Class)((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		IOperationModel model2  = (IOperationModel) modelType.newInstance();
		op2.setModel(model2);
		((JunkTestOperationModel)model2).setxDim(50);

		PersistJsonOperationHelper util = new PersistJsonOperationHelper();
		String modelJson = util.getModelJson(model2);

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();
		
		// TODO Must be closed in a try{} finally{} ?
		IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());

		util.writeOperations(file, new IOperation[]{op2});

		IOperation[] readOperations = PersistJsonOperationsNode.readOperations(LoaderFactory.getData(tmp.getAbsolutePath()).getTree());

		assertEquals(((JunkTestOperationModel)(readOperations[0].getModel())).getxDim(), 50);

	}
	
	@Test
	public void testWriteReadOperationRoiFuncData() throws Exception {
		IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperationROI");
		Class modelType = (Class)((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		JunkTestModelROI model2  = (JunkTestModelROI) modelType.newInstance();

		model2.setRoi(new SectorROI());
		model2.setxDim(50);
		model2.setBar(new Gaussian());
		model2.setFoo(DatasetFactory.createRange(10, Dataset.INT32));
		model2.setData(DatasetFactory.createRange(5, Dataset.INT32));
		model2.setFunc(new Lorentzian());
		model2.setRoi2(new RectangularROI());
		op2.setModel(model2);

		PersistJsonOperationHelper util = new PersistJsonOperationHelper();
//		String modelJson = util.getModelJson(model2);

		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();
		IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());

		util.writeOperations(file, new IOperation[]{op2});

		IOperation[] readOperations = PersistJsonOperationsNode.readOperations(LoaderFactory.getData(tmp.getAbsolutePath()).getTree());
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
	public void testWriteReadOperationRoiFuncDataNode() throws Exception {
		IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperationROI");
		Class modelType = (Class)((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		JunkTestModelROI model2  = (JunkTestModelROI) modelType.newInstance();

		model2.setRoi(new SectorROI());
		model2.setxDim(50);
		model2.setBar(new Gaussian());
		model2.setFoo(DatasetFactory.createRange(10, Dataset.INT32));
		model2.setData(DatasetFactory.createRange(5, Dataset.INT32));
		model2.setFunc(new Lorentzian());
		model2.setRoi2(new RectangularROI());
		op2.setModel(model2);

//		String modelJson = util.getModelJson(model2);

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

		PersistJsonOperationHelper util = new PersistJsonOperationHelper();


		final File tmp = File.createTempFile("Test", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();
		IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());

		util.writeOriginalDataInformation(file, ssm);

		OriginMetadata outOm = PersistJsonOperationsNode.readOriginalDataInformation(file.getPath());
		outOm.toString();	
		
	}

}
