package org.dawnsci.conversion;

import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawnsci.conversion.converters.CustomTomoConverter;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class TomoConvertTest {
	
	@Test
	public void testTomoSimple() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		final String path = getTestFilePath("extraction_test_small.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("whatever", ".unknown");
		final File dir = new File(tmp.getParent(), "tomo_export_testTomoSimple_"+System.currentTimeMillis());
		tmp.delete();
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setConversionScheme(ConversionScheme.CUSTOM_TOMO);
        
        //Guide using the tomo converter bean
        CustomTomoConverter.TomoInfoBean bean = new CustomTomoConverter.TomoInfoBean();
        
        if (!bean.setTomographyDefinition(path)) throw new Exception("Failed determining if a valid file");
        
        //set path to save to (leave null if individual images have there own path)
        context.setOutputPath(dir.getAbsolutePath());
        //Tomo bean knows the dataset name
        context.setDatasetName(bean.getTomoDataName());
        //Should be the same for all tomo blocks?
        context.addSliceDimension(0, "all");
        
        //TODO test for different %xds
        bean.setBits(8);
        bean.setDarkFieldPath("%s/d_%05d");
        bean.setFlatFieldPath("%s/f_%05d");
        bean.setProjectionPath("%s/p_%05d");
        context.setUserObject(bean);
        
        service.process(context);
        
        final File[] fa = dir.listFiles();
        fa.toString();
        for (File file : fa) {
        	file.deleteOnExit();
        	final IDataHolder holder = LoaderFactory.getData(file.getAbsolutePath());
        	final IDataset   set    = holder.getDataset(0);
        	if (set.getShape()[0]!=100 || set.getShape()[1]!=100) {
        		throw new Exception("Incorrect shape of exported dataset!");
        	}
        }
        
        // Check that 7 datasets were exported.
        if (fa.length!=7) {
        	String msg = "The directory: "+dir.getAbsolutePath()+" does not contain 7 images!";
        	System.out.println(msg);
        	throw new Exception(msg);
        }
   	}
	
	@Test
	public void testTomoDifferentPaths() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		final String path = getTestFilePath("extraction_test_small.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("whatever", ".unknown");
		final File dir = new File(tmp.getParent(), "tomo_export_testTomoDifferentPaths_"+System.currentTimeMillis());
		tmp.delete();
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setConversionScheme(ConversionScheme.CUSTOM_TOMO);
        
        //Guide using the tomo converter bean
        CustomTomoConverter.TomoInfoBean bean = new CustomTomoConverter.TomoInfoBean();
        
        if (!bean.setTomographyDefinition(path)) throw new Exception("Failed determining if a valid file");
        
        //set path to save to (leave null if individual images have there own path)
        //context.setOutputPath(dir.getAbsolutePath());
        //Tomo bean knows the dataset name
        context.setDatasetName(bean.getTomoDataName());
        //Should be the same for all tomo blocks?
        context.addSliceDimension(0, "all");
        
        //TODO test for different %xds
        bean.setBits(16);
        bean.setDarkFieldPath(dir.getAbsolutePath() + "/dark/d_%05d");
        bean.setFlatFieldPath(dir.getAbsolutePath() +"/flat/f_%05d");
        bean.setProjectionPath(dir.getAbsolutePath() + "/proj/p_%05d");
        context.setUserObject(bean);
        
        service.process(context);
        
        final File[] fa = dir.listFiles();
        
        if (fa.length!=3) {
        	String msg = "The directory: "+dir.getAbsolutePath()+" does not contain 3 folders!";
        	System.out.println(msg);
        	throw new Exception(msg);
        }
        
        for (File folder : fa) {
        	final File[] fa1 = folder.listFiles();
        	folder.deleteOnExit();
        	for (File file : fa1) {
        		file.deleteOnExit();
        		final IDataHolder holder = LoaderFactory.getData(file.getAbsolutePath());
        		final IDataset   set    = holder.getDataset(0);
        		if (set.getShape()[0]!=100 || set.getShape()[1]!=100) {
        			throw new Exception("Incorrect shape of exported dataset!");
        		}
        	}
        }
   	}
	
	@Test
	public void testTomoDifferentNumberWidth() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		final String path = getTestFilePath("extraction_test_small.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("whatever", ".unknown");
		final File dir = new File(tmp.getParent(), "tomo_export_testTomoDifferentNumberWidth_"+System.currentTimeMillis());
		tmp.delete();
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setConversionScheme(ConversionScheme.CUSTOM_TOMO);
        
        //Guide using the tomo converter bean
        CustomTomoConverter.TomoInfoBean bean = new CustomTomoConverter.TomoInfoBean();
        
        if (!bean.setTomographyDefinition(path)) throw new Exception("Failed determining if a valid file");
        
        //set path to save to (leave null if individual images have there own path)
        context.setOutputPath(dir.getAbsolutePath());
        //Tomo bean knows the dataset name
        context.setDatasetName(bean.getTomoDataName());
        //Should be the same for all tomo blocks?
        context.addSliceDimension(0, "all");
        
        //TODO test for different %xds
        bean.setBits(33);
        bean.setDarkFieldPath("%s/d_%01d_%06d");
        bean.setFlatFieldPath("%s/f_%02d");
        bean.setProjectionPath("%s/p_%03d");
        context.setUserObject(bean);
        
        service.process(context);
        
        final File[] fa = dir.listFiles();
        
        double lastVal = 0;
        
        for (File file : fa) {
        	file.deleteOnExit();
        	final IDataHolder holder = LoaderFactory.getData(file.getAbsolutePath());
        	final IDataset   set    = holder.getDataset(0);
        	if (set.getShape()[0]!=100 || set.getShape()[1]!=100) {
        		throw new Exception("Incorrect shape of exported dataset!");
        	}
        	
        	double val = set.getDouble(10,10);
        	if (val == lastVal) throw new Exception("Same data loaded each time");
        	lastVal = val;
        }
        
        // Check that 7 datasets were exported.
        if (fa.length!=7) {
        	String msg = "The directory: "+dir.getAbsolutePath()+" does not contain 7 images!";
        	System.out.println(msg);
        	throw new Exception(msg);
        }
   	}
	
	
	private String getTestFilePath(String fileName) {

		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();

	}

}
