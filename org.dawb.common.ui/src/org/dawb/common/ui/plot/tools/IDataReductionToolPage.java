/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.plot.tools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;

/**
 * Interface used  to define this tool as a data reduction tool. 
 * 
 * Data Reduction tools generally reduce data from nD to (n-1)D,
 * for instance radial integration of an image (2d) to 1d. They can also be applied
 * n+1 data dimensions using the @see DataReductionWizard
 * 
 * @author Matthew Gerring
 *
 */
public interface IDataReductionToolPage extends IToolPage {

	/**
	 * Called before the data reduction starts, gives the tool a chance to setup for the first run
	 * Also returns a string that will be the main NXData entry name (or null to use default) 
	 * @return nxDataName
	 */
	public String exportInit();
	
	/**
	 * Export the tool results to an hdf5 file under the passed in group.
	 * 
	 * This method is used to run the tool multiple times on different slices of the data.
	 * 
	 * This method will not be called on the UI thread in most instances.
	 * 
	 * @param bean
	 */
	public DataReductionInfo export(DataReductionSlice bean) throws Exception;
	
	/**
	 * Called at the end of the data reduction in case clean up or final writes are required.
	 * @param context
	 */
	public void exportFinished() throws Exception ;

	/**
	 * Bean to contain data for slice of tool.
	 * @author Matthew Gerring
	 *
	 */
	public final class DataReductionSlice {

		/**
		 * The file we are writing to.
		 */
		private NexusFile file;
		/**
		 * The Group which the user chose.
		 */
		private String                 parent;
		/**
		 * The actual sliced data to operate on.
		 */
		private IDataset       data;
		
		/**
		 * May be null, 0 = x, 1 = y. Y may be omitted, in which case use indexes
		 */
		private List<IDataset> axes;
		
		/**
		 * The dataset names or regular expressions as typed by the user into the tool.
		 */
		private List<String> expandedDatasetNames;
		/**
		 * May be null, data which the tool may need for persistence.
		 */
		private Object                userData;
		private IMonitor              monitor;
		
		private Slice[]               slice;
		private int[]                 shape;

		public DataReductionSlice(NexusFile hf, String group, IDataset set, Object ud, Slice[] slice,
				int[] shape, IMonitor mon) {
			this.file = hf;
			this.parent = group;
			this.data = set;
			this.userData = ud;
			this.monitor = mon;
			this.slice = slice;
			this.shape = shape;
		}

		public NexusFile getFile() {
			return file;
		}
		public void setFile(NexusFile file) {
			this.file = file;
		}
		public String getParent() {
			return parent;
		}
		public void setParent(String parent) {
			this.parent = parent;
		}
		public IDataset getData() {
			return data;
		}
		public void setData(IDataset set) {
			this.data = set;
		}

		/** Appends data at path provided by this.getParent at index 0
		 *
		 * @param lazyWritables
		 *            Map of ILazyWriteableDataset
		 * @param more
		 *            dataset
		 * @throws Exception
		 */
		public void appendData(Map<String, ILazyWriteableDataset> lazyWritables, IDataset more) throws Exception {
			appendData(lazyWritables, more, parent);
		}

		/** Appends data at path provided by this.getParent
		 *
		 * @param lazyWritables
		 *            Map of ILazyWriteableDataset
		 * @param more
		 *            dataset
		 * @param index
		 *            of data reduction export
		 * @throws Exception
		 */
		public void appendData(Map<String, ILazyWriteableDataset> lazyWritables, IDataset more, int index) throws Exception {
			appendData(lazyWritables, more, parent, index);
		}

		/**
		 * Appends data at index 0
		 *
		 * @param lazyWritables
		 *            Map of ILazyWriteableDataset
		 * @param more
		 *            dataset
		 * @param group
		 *            group path of dataset to append to
		 * @throws Exception
		 */
		public void appendData(Map<String, ILazyWriteableDataset> lazyWritables, IDataset more, String group) throws Exception {
			appendData(lazyWritables, more, group, 0);
		}

		/**
		 * Appends data
		 *
		 * @param lazyWritables
		 *            Map of ILazyWriteableDataset
		 * @param more
		 *            dataset
		 * @param group
		 *            group path of dataset to append to
		 * @param index
		 *            of data reduction export
		 * @throws Exception
		 */
		public void appendData(Map<String, ILazyWriteableDataset> lazyWritables, IDataset more, String group, int index) throws Exception {
			int[] newShape = new int[more.getRank() + 1];
			for (int i = 0; i < newShape.length; i++) {
				if (i == 0)
					newShape[i] = shape[0];
				else {
					newShape[i] = more.getShape()[i - 1];
				}
			}
			String filename = file.getFilePath();
			String dataname = more.getName();
			Dataset dataset = (Dataset) more;
			int type = dataset.getDType();
			if (!lazyWritables.containsKey(dataname))
				lazyWritables.put(dataname, HDF5Utils.createLazyDataset(filename, group, more.getName(), newShape, null,
						newShape, type, null, false));
			
			ILazyWriteableDataset lwd = lazyWritables.get(dataname);
			lwd.setSlice(monitor, more, new SliceND(lwd.getShape(), new Slice(index, index+1)));
		}

		public Object getUserData() {
			return userData;
		}
		public void setUserData(Object userData) {
			this.userData = userData;
		}
		public IMonitor getMonitor() {
			return monitor;
		}
		public void setMonitor(IMonitor monitor) {
			this.monitor = monitor;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((axes == null) ? 0 : axes.hashCode());
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			result = prime * result
					+ ((expandedDatasetNames == null) ? 0 : expandedDatasetNames.hashCode());
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result
					+ ((monitor == null) ? 0 : monitor.hashCode());
			result = prime * result
					+ ((parent == null) ? 0 : parent.hashCode());
			result = prime * result + Arrays.hashCode(shape);
			result = prime * result + Arrays.hashCode(slice);
			result = prime * result
					+ ((userData == null) ? 0 : userData.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataReductionSlice other = (DataReductionSlice) obj;
			if (axes == null) {
				if (other.axes != null)
					return false;
			} else if (!axes.equals(other.axes))
				return false;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			if (expandedDatasetNames == null) {
				if (other.expandedDatasetNames != null)
					return false;
			} else if (!expandedDatasetNames.equals(other.expandedDatasetNames))
				return false;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			if (monitor == null) {
				if (other.monitor != null)
					return false;
			} else if (!monitor.equals(other.monitor))
				return false;
			if (parent == null) {
				if (other.parent != null)
					return false;
			} else if (!parent.equals(other.parent))
				return false;
			if (!Arrays.equals(shape, other.shape))
				return false;
			if (!Arrays.equals(slice, other.slice))
				return false;
			if (userData == null) {
				if (other.userData != null)
					return false;
			} else if (!userData.equals(other.userData))
				return false;
			return true;
		}
		public List<IDataset> getAxes() {
			return axes;
		}
		public void setAxes(List<IDataset> axes) {
			this.axes = axes;
		}
		public List<String> getExpandedDatasetNames() {
			return expandedDatasetNames;
		}
		public void setExpandedDatasetNames(List<String> datasets) {
			this.expandedDatasetNames = datasets;
		}
		
	}
	/**
	 * TODO May add a method here to define extra wizard pages if a tool requires it.
	 * public IWizardPage getToolExportWizardPage(...) {
	 */
	
	public final class DataReductionInfo {
		private IStatus status;
		/**
		 * used to provide data between calls of the tool to maintain
		 * state. For instance used in the peak fitting tool to provide
		 * peaks with state.
		 */
		private Object  userData;
		
		public DataReductionInfo(IStatus status) {
			this(status, null);
		}
		/**
		 * 
		 * @param status
		 * @param userData may be null.
		 */
		public DataReductionInfo(IStatus status, Object userData) {
			this.status   = status;
			this.userData = userData;
		}
		
		public IStatus getStatus() {
			return status;
		}
		public void setStatus(IStatus status) {
			this.status = status;
		}
		public Object getUserData() {
			return userData;
		}
		public void setUserData(Object userData) {
			this.userData = userData;
		}
		
	}

	/**
	 * Returns the export index
	 *
	 * @return index
	 */
	public int getExportIndex();

	/**
	 * Sets the incremented index
	 *
	 * @param index
	 */
	public void setExportIndex(int exportIndex);
}
