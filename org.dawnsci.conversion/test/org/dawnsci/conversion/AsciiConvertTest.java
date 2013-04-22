package org.dawnsci.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawnsci.conversion.internal.AbstractConversion;
import org.dawnsci.conversion.internal.AsciiConvert1D;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


public class AsciiConvertTest {

	
	@Test
	public void testAsciiSimple() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("MoKedge_1_15.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("testSimple", ".dat");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.ASCII_FROM_1D);
        context.setDatasetName("/entry1/counterTimer01/(Energy|I0|lnI0It|It)");
        
        service.process(context);
        
        final DataHolder   dh    = LoaderFactory.getData(tmp.getAbsolutePath());
        final List<String> names = Arrays.asList("/entry1/counterTimer01/Energy","/entry1/counterTimer01/I0","/entry1/counterTimer01/lnI0It","/entry1/counterTimer01/It");
        for (String name : names) {
            if (dh.getDataset(name)==null) throw new Exception("Missing dataset "+name);
		}
   	}
	
	@Test
	public void testAsciiCustomConfig() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("MoKedge_1_15.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("testConfig", ".dat");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.ASCII_FROM_1D);
        context.setDatasetName("/entry1/counterTimer01/(Energy|I0|lnI0It|It)");
        
        // Set some custom data
        final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
        bean.setNumberFormat("#0.00");
        final Map<String,String> alternates = new HashMap<String,String>(4);
        alternates.put("/entry1/counterTimer01/Energy", "Energy");
        alternates.put("/entry1/counterTimer01/I0",     "I0");
        alternates.put("/entry1/counterTimer01/lnI0It", "lnI0It");
        alternates.put("/entry1/counterTimer01/It",     "It");
        bean.setAlernativeNames(alternates);
        context.setUserObject(bean);

        service.process(context);
        
        // Check rename worked
        final DataHolder   dh    = LoaderFactory.getData(tmp.getAbsolutePath());
        final List<String> names = Arrays.asList("Energy","I0","lnI0It","It");
        for (String name : names) {
            if (dh.getDataset(name)==null) throw new Exception("Missing dataset "+name);
		}
        
        // Check format worked
        final StringBuffer content = readFile(tmp);
        final String[] sa = content.toString().split("\n");
        final String firstDataLine = sa[1];
        if (!"6912.00\t134878.00\t2040284.00\t-2.72".equals(firstDataLine.trim())) {
        	throw new Exception("Unexpected format!");
        }
 	}

	@Test
	public void testAsciiCustomConfig2() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("MoKedge_1_15.nxs");
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("testConfig", ".csv");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.ASCII_FROM_1D);
        context.setDatasetName("/entry1/counterTimer01/(Energy|I0|lnI0It|It)");
        
        // Set some custom data
        final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
        bean.setNumberFormat("#0.00");
        bean.setConversionType("csv");
        final Map<String,String> alternates = new HashMap<String,String>(4);
        alternates.put("/entry1/counterTimer01/Energy", "Energy");
        alternates.put("/entry1/counterTimer01/I0",     "I0");
        alternates.put("/entry1/counterTimer01/lnI0It", "lnI0It");
        alternates.put("/entry1/counterTimer01/It",     "It");
        bean.setAlernativeNames(alternates);
        context.setUserObject(bean);

        service.process(context);
                
        // Check format worked
        final StringBuffer content = readFile(tmp);
        final String[] sa = content.toString().split("\n");
        final String firstDataLine = sa[1];
        if (!"6912.00,\t134878.00,\t2040284.00,\t-2.72".equals(firstDataLine.trim())) {
        	throw new Exception("Unexpected format!");
        }
 	}


	private String getTestFilePath(String fileName) {
		
		final File test = new File("test/org/dawnsci/conversion/"+fileName);
		return test.getAbsolutePath();
	
	}
	
	
	/**
	 * TODO FIXME - move this to proper junit test!
	 * @param args
	 */
	//@Test
	public void testInternals()  throws Exception {
		
		final AbstractConversion conv = new AbstractConversion(null) {
			@Override
			protected void convert(AbstractDataset slice ) {
				System.out.println(slice);
			}
		};
		final StringBuffer buf = new StringBuffer();
		buf.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ EXPAND");
		buf.append(Arrays.toString(conv.expand("C:/Work/results/i03_data/35873/35873_M3S15_1_0001.cbf").toArray()));
		buf.append(Arrays.toString(conv.expand("C:/Work/results/i03_data/35873/35873_M3S15_1_000(\\d+).cbf").toArray()));
		buf.append(Arrays.toString(conv.expand("C:/Work/results/i03_data/35873/35873_M3S15_1_00(\\d+).cbf").toArray()));
		buf.append(conv.expand("C:/Work/results/i03_data/35873/fred.cbf"));
		buf.append(conv.expand("C:/Work/results/i03_data/35873/(.*).img"));
		buf.append(conv.expand("C:/Work/results/i03_data/35873/(.*).cbf"));
		buf.append(conv.expand("C:/tmp/"));
		
		buf.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ DATA");
		buf.append(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "naff"));
		buf.append(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/data"));
		buf.append(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/data_(.*)"));
		buf.append(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/(.*)"));
		
		
		buf.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ SLICE");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/white_z", null, null);
		
		final Map<Integer,String> slice = new HashMap<Integer,String>(1);
		slice.put(0, "36");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/dark_data", slice, null);

		slice.put(0, "12:24");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/dark_data", slice, null);

		slice.put(0, "all");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/dark_data", slice, null);

		final StringBuffer result = readFile(new File(getTestFilePath("Conversion.txt")));
		
		if (result.toString().trim().equals(buf.toString().trim())) {
			throw new Exception("Unexpected output from service internals!");
		}
	}
	
	
	/**
	 * @param file
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final File file) throws Exception {
		return readFile(new FileInputStream(file));
	}

	/**
	 * @param in
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final InputStream in) throws Exception {

		return readFile(in, null);
	}

	/**
	 * @param in
	 * @param charsetName
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final InputStream in, final String charsetName) throws Exception {

		BufferedReader ir = null;
		try {
			if (charsetName != null) {
				ir = new BufferedReader(new InputStreamReader(in, charsetName));
			} else {
				ir = new BufferedReader(new InputStreamReader(in));
			}

			// deliberately do not remove BOM here
			int c;
			StringBuffer currentStrBuffer = new StringBuffer();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

}
