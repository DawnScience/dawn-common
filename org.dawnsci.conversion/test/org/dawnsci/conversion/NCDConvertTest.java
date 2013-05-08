package org.dawnsci.conversion;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class NCDConvertTest {
	
	@Test
	public void testNCDSimple() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("results_i22-102527_Pilatus2M_280313_112434.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("NCDtestSimple", ".dat");
		tmp.deleteOnExit();
		final File dir = new File(tmp.getParent(), "NCD_export"+System.currentTimeMillis());
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setOutputPath(dir.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.CUSTOM_NCD);
        context.setDatasetName("/entry1/Pilatus2M_result/data");
        context.setAxisDatasetName("/entry1/Pilatus2M_result/q");
        context.addSliceDimension(0, "all");
        service.process(context);
        
        service.process(context);
        
        final File[] fa = dir.listFiles();
        for (File file : fa) {
        	file.deleteOnExit();
        	final DataHolder   dh    = LoaderFactory.getData(file.getAbsolutePath());
            String[] names = dh.getNames();
            assertEquals(61, names.length);
            assertEquals("x",names[0]);
            assertEquals("Data0",names[1]);
        }
   	}
	
	
	@Test
	public void testNCDMultiDims() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("results_i22-114346_Pilatus2M_220313_090939.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("NCDtestSimple", ".dat");
		tmp.deleteOnExit();
		final File dir = new File(tmp.getParent(), "NCD_export"+System.currentTimeMillis());
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setOutputPath(dir.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.CUSTOM_NCD);
        context.setDatasetName("/entry1/Pilatus2M_result/data");
        context.setAxisDatasetName("/entry1/Pilatus2M_result/q");
        context.addSliceDimension(0, "all");
        service.process(context);
        
        final File[] fa = dir.listFiles();
        assertEquals(12, fa.length);
        for (File file : fa) {
        	file.deleteOnExit();
        	final DataHolder   dh    = LoaderFactory.getData(file.getAbsolutePath());
            String[] names = dh.getNames();
            assertEquals(3, names.length);
            assertEquals("x",names[0]);
            assertEquals("Data0",names[1]);
        }
   	}
	
	@Test
	public void testNCDSingleAndNormalised() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("results_i22-118040_Pilatus2M_010513_125108.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("NCDtestSimple", ".dat");
		tmp.deleteOnExit();
		final File dir = new File(tmp.getParent(), "NCD_export"+System.currentTimeMillis());
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setOutputPath(dir.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.CUSTOM_NCD);
        context.setDatasetNames(Arrays.asList(new String[] {"/entry1/Pilatus2M_result/data","/entry1/Pilatus2M_processing/Normalisation/data"}));
        context.setAxisDatasetName("/entry1/Pilatus2M_result/q");
        context.addSliceDimension(0, "all");
        service.process(context);
        
        final File[] fa = dir.listFiles();
        assertEquals(2, fa.length);
        for (File file : fa) {
        	file.deleteOnExit();
        	final DataHolder   dh    = LoaderFactory.getData(file.getAbsolutePath());
            String[] names = dh.getNames();
            assertEquals(2, names.length);
            assertEquals("x",names[0]);
            assertEquals("Data0",names[1]);
        }
   	}
	
	@Test
	public void testNCDMultiFile() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("results_i22-118040_Pilatus2M_010513_125108.nxs");
		final String path1 = getTestFilePath("results_i22-102527_Pilatus2M_280313_112434.nxs");
		final String path2 = getTestFilePath("results_i22-114346_Pilatus2M_220313_090939.nxs");
		
		final IConversionContext context = service.open(path);
		///TODO fix in interface
		if (context instanceof ConversionContext) {
			((ConversionContext)context).setFilePaths(path,path1,path2);
		}
		
		final File tmp = File.createTempFile("NCDtestSimple", ".dat");
		tmp.deleteOnExit();
		final File dir = new File(tmp.getParent(), "NCD_export"+System.currentTimeMillis());
		dir.mkdirs();
		dir.deleteOnExit();
		
        context.setOutputPath(dir.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.CUSTOM_NCD);
        context.setDatasetNames(Arrays.asList(new String[] {"/entry1/Pilatus2M_result/data","/entry1/Pilatus2M_processing/Normalisation/data"}));
        context.setAxisDatasetName("/entry1/Pilatus2M_result/q");
        context.addSliceDimension(0, "all");
        service.process(context);
        
        final File[] fa = dir.listFiles();
        assertEquals(27, fa.length);
        for (File file : fa) {
        	file.deleteOnExit();
        	final DataHolder   dh    = LoaderFactory.getData(file.getAbsolutePath());
            String[] names = dh.getNames();
            assertEquals("x",names[0]);
            assertEquals("Data0",names[1]);
        }
   	}
	
	private String getTestFilePath(String fileName) {
		final File test = new File("test/org/dawnsci/conversion/"+fileName);
		return test.getAbsolutePath();
	}
}
