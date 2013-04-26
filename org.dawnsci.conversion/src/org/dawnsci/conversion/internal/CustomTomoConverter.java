package org.dawnsci.conversion.internal;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.nexus.IFindInNexus;
import org.dawb.hdf5.nexus.NexusUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Custom converter for tomography data.
 * 
 * Manages the conversion of tomography nexus files containing NXtomo into image files
 *
 */
public class CustomTomoConverter extends AbstractConversion {

	private final static String DEF = "definition";
	private final static String NXTOMO = "nxtomo";
	private final static String DATA_LOCATION = "instrument/detector/data";
	private final static String KEY_LOCATION = "instrument/detector/image_key";
	private int counter;
	private int nImages;
	
	public CustomTomoConverter(IConversionContext context) {
		super(context);
	}

	
	@Override
	public void processSlice(final File                 path, 
							 final String               dsPath,
							 final Map<Integer, String> sliceDimensions,
							 final IConversionContext   context) throws Exception {
		
		//Overriding processSlice allows us to process the tomography bean before calling the super
		if (context.getUserObject() != null && context.getUserObject() instanceof TomoInfoBean) {
			processTomoInfoBeanContext(path, context);
		} else {
			throw new IllegalArgumentException("Not a recognised tomography file");
		}
		counter = 0;
		nImages = ((TomoInfoBean)context.getUserObject()).getNumberOfImages();
		super.processSlice(path, dsPath, sliceDimensions, context);	
	}
	
	@Override
	protected void convert(AbstractDataset slice) throws Exception {
		
		String filename = ((TomoInfoBean)context.getUserObject()).getNextFileName();
		int nBits = ((TomoInfoBean)context.getUserObject()).getBits();
		
		File file = new File(filename);
		file.getParentFile().mkdirs();
		
		final JavaImageSaver saver = new JavaImageSaver(filename, "tiff", nBits, true);
		final DataHolder     dh    = new DataHolder();
		dh.addDataset(slice.getName(), slice);
		saver.saveFile(dh);
		
		if (nImages < 101 || counter%(nImages/100) == 0) {
			if (context.getMonitor()!=null) context.getMonitor().worked((100)/(nImages));
		}
		
		counter++;
		
	}

	private void processTomoInfoBeanContext(File path, IConversionContext context) {
		if (findGroupContainingDefinition(path.getAbsolutePath()) == null) {
			throw new IllegalArgumentException("Not a recognised tomography file");
		}
		
		TomoInfoBean bean = (TomoInfoBean)context.getUserObject();

		if( context.getOutputPath() != null) bean.setOutputPath(context.getOutputPath());
		
		if (bean.getImageKey() == null) {
			IDataset key = getImageKey(bean, path);
			if (key != null) bean.setImageKey(key);
			else throw new IllegalArgumentException("Tomography file does not contain image key");
		}
	}
	
	private IDataset getImageKey(TomoInfoBean bean, File path) {
		
		IHierarchicalDataFile file = null;
		try {
			
			file = HierarchicalDataFactory.getReader(path.getAbsolutePath());
			Dataset dataset = (Dataset)file.getData(bean.tomoPath + KEY_LOCATION);
			return getSet(dataset.getData(),dataset.getDims(),dataset);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				file.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;

	}
	
	private static HObject findGroupContainingDefinition(String path) {
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(path);
			Group object = file.getRoot();
			
			IFindInNexus finder = new IFindInNexus() {
				
				@Override
				public boolean inNexus(HObject nexusObject) {
					if(nexusObject instanceof H5ScalarDS) {
						if (((H5ScalarDS)nexusObject).getName().toLowerCase().equals(DEF)) {
							if (((H5ScalarDS)nexusObject).getDatatype().getDatatypeClass() ==Datatype.CLASS_STRING) {
								String name = null;
								try {
									((H5ScalarDS)nexusObject).setConvertByteToString(true);
									name = ((String[])((H5ScalarDS)nexusObject).getData())[0];
								} catch (Exception e) {
									e.printStackTrace();
								}
								if (name != null) {
									if (name != null && name.toLowerCase().contains(NXTOMO)) return true;
								}
							}
						}
					}
					
					return false;
				}
			};
			
			List<HObject> out = NexusUtils.nexusBreadthFirstSearch(finder, object, true);
			
			if (out != null && !out.isEmpty()){
				return out.get(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				file.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Bean to handle the custom aspects of converting tomography data
	 *
	 */
	public static final class TomoInfoBean {
		private IDataset imageKey;
		//paths contain %s for path to full fill (-minus ext)
		//also contain %xd for number position and width
		private String darkPath, flatPath, projectionPath;
		private String outputPath,tomoPath,filePath;
		private String extension = "tiff";
		private int dark,flat,projection = 0;
		private int nBits = 8;
		
		/**
		 * Method to produce a full file path when given the path and
		 * a filename containing %s and %0xd where x is integer
		 */
		public static String convertToFullPath(String path, String filename) {
			
			String output = createFullPath(path,filename,123);
			
			return output;
		}
		
		
		/**
		 * Method to check is a valid tomograpy file (returns true)
		 * and populate some internal data
		 *
		 */
		public boolean setTomographyDefinition(String path) {
			
			HObject ob = findGroupContainingDefinition(path);
			
			if (ob == null) return false;
			filePath = path;
			tomoPath = ob.getPath();
			return true;
			
		}
		
		public int getNumberOfImages() {
			
			if (imageKey != null) {
				return imageKey.getShape()[0];
			}
			
			
			return -1;
		}
		
		public void setImageKey(IDataset imageKey) {
			this.imageKey = imageKey;
		}
		
		public IDataset getImageKey() {
			return imageKey;
		}
		
		public void setOutputPath(String path){
			this.outputPath = path;
		}
		
		public String getOutputPath(){
			return this.outputPath;
		}
		
		public String getTomoDataName() {
			if (tomoPath == null) return null;
			return tomoPath + DATA_LOCATION;
		}
		
		
		public void setDarkFieldPath(String path){
			this.darkPath = path;
		}
		
		public void setFlatFieldPath(String path) {
			this.flatPath = path;
		}
		
		public void setProjectionPath(String path) {
			this.projectionPath = path;
		}
		
		public String getNextFileName() {
			int sum = dark+flat+projection;
			
			switch (imageKey.getInt(sum)) {
			case 0:
				return getNextProjectionName();
			case 1:
				return getNextFlatFieldName();
			case 2:
				return getNextDarkFieldName();
			}
			
			return null;
		}
		
		private String getNextFlatFieldName() {
			String path = buildPath(flatPath,flat);
			flat++;
			return path;
		}
		
		private String getNextDarkFieldName() {
			String path = buildPath(darkPath,dark);
			dark++;
			return path;
		}
		
		private String getNextProjectionName() {
			String path = buildPath(projectionPath,projection);
			projection++;
			return path;
		}
		
		private String buildPath(String path, int number) {
			
			if (outputPath == null) {
				int index = filePath.lastIndexOf(".");
				outputPath = filePath.substring(0, index);
			}
			
			return createFullPath(outputPath,path, number);
		}
		
		private static String createFullPath(String path, String name, int number) {
			String output = name.replace("%s", path);
			
			Pattern p = Pattern.compile("%0\\d+d");
			Matcher m = p.matcher(name);
			
			while (m.find()) {
				String sub = name.substring(m.start(), m.end());
				String result = String.format(sub, number);
				output = output.replace(sub, result);
			}
			
			return output;
		}
		

		public void setExtension(String imageFormat) {
			extension = imageFormat;
			
		}
		
		public String getExtension() {
			return extension;
			
		}

		public void setBits(int bitDepth) {
			nBits = bitDepth;
			
		}
		
		public int getBits() {
			return nBits;
			
		}
		
	}
}
