/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.slicing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;

public class DimsDataList implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5902704017223885965L;
	
	
	private List<DimsData> dimsData;
		
	public DimsDataList() {
	}

	public DimsDataList(List<DimsData> chunk) {
		dimsData = chunk;
	}
	
	public DimsDataList(int[] dataShape, SliceObject slice) throws Exception {
		
		// TODO Read axis maybe from nexus data?
		//final IHierarchicalDataFile file = HierarchicalDataFactory.getReader(slice.getPath());
		try {
			
			//final Dataset set = (Dataset)file.getData(slice.getName());
			//final List   meta = set.getMetadata();
			
			
			// For now we just assume the first dimensions are the slow ones to make an axis out
			// of. Later read the axis from the meta list but we do not have examples of this so
			// far.
			int xaxis=-1,yaxis=-1;
			for (int i = 0; i<dataShape.length; ++i) {
				add(new DimsData(i));
			}
			for (int i = dataShape.length-1; i>=0; i--) {
				
				if (dataShape[i]>1) {
					if (yaxis<0) {
						getDimsData(i).setAxis(1);
						yaxis = i;
						continue;
					} else  if (xaxis<0) {
						getDimsData(i).setAxis(0);
						xaxis = i;
						continue;
					}
				}
			}
			
			// If we only found a y it may be a multiple-dimension set with only 1D possible.
			// In that case change y to x.
			if (yaxis>-1 && xaxis<0) {
				getDimsData(yaxis).setAxis(0);
			}
		} finally {
			//file.close();
		}
	}

	public List<DimsData> getDimsData() {
		return dimsData;
	}

	public void setDimsData(List<DimsData> slices) {
		this.dimsData = slices;
	}
	
	public void add(DimsData dimension) {
		if (dimsData==null) dimsData = new ArrayList<DimsData>(3);
		if (dimsData.size()>dimension.getDimension() && dimension.getDimension()>-1) {
			dimsData.set(dimension.getDimension(), dimension);
		} else {
			dimsData.add(dimension);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimsData == null) ? 0 : dimsData.hashCode());
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
		DimsDataList other = (DimsDataList) obj;
		if (dimsData == null) {
			if (other.dimsData != null)
				return false;
		} else if (!dimsData.equals(other.dimsData))
			return false;
		return true;
	}

	public static Object[] getDefault() {
		return new DimsData[]{new DimsData(0)};
	}
	
	public Object[] getElements() {
		if (dimsData==null) return null;
		return dimsData.toArray(new DimsData[dimsData.size()]);
	}

	public int size() {
		if (dimsData==null) return 0;
		return dimsData.size();
	}

	public DimsData getDimsData(int i) {
		if (dimsData==null) return null;
		return dimsData.get(i);
	}

	public Iterator<DimsData> iterator() {
		if (dimsData==null) return null;
		return dimsData.iterator();
	}
	
	public void clear() {
		if (dimsData!=null) dimsData.clear();
	}
	
	public String toString() {
		return toString(null);
	}
	public String toString(int[] shape) {
		
		final StringBuilder buf = new StringBuilder();
		buf.append("[ ");
		
		int index = 0;
		for (DimsData d : dimsData) {
			
			final int upper = shape!=null ? shape[index] : -1;
			buf.append(d.getUserString(upper));
			if (d!=dimsData.get(dimsData.size()-1)) buf.append(",  ");
			++index;
		}
		buf.append(" ]");
		return buf.toString();
	}

	public boolean isRangeDefined() {
		for (DimsData data : getDimsData()) {
			if (data.getSliceRange()!=null) return true;
		}
		return false;
	}
	
	public int getAxisCount() {
		if (dimsData==null) return -1;
		int count = 0;
		for (DimsData dd : dimsData) {
			if (dd.getAxis()>-1) count++;
		}
		return count;
	}

	public boolean is2D() {
		return getAxisCount()==2;
	}
	
	public DimsDataList clone() {
		final DimsDataList clone = new DimsDataList();
		for (DimsData dd : getDimsData()) {
			DimsData dnew = dd.clone();
			add(dnew);
		}
		return clone;
	}

	/**
	 * Sets any axes there are to  the axis passed in
	 */
	public void normalise(int iaxis) {
		for (DimsData dd : getDimsData()) {
			if (dd.getAxis()>-1) dd.setAxis(iaxis);
		}
	}

	/**
	 * Probably not best algorithm but we are dealing with very small arrays here.
	 * 
	 * @param iaxisToFind
	 */
	public void setSingleAxisOnly(int iaxisToFind, int iaxisValue) {
		DimsData found = null;
		for (DimsData dd : getDimsData()) {
			if (dd.getAxis()==iaxisToFind) {
				dd.setAxis(iaxisValue);
				found=dd;
			}
		}
		
		if (found!=null) {
			for (DimsData dd : getDimsData()) {
				if (dd==found) continue;
				dd.setAxis(-1);
			}
			return;
		} else { // We have to decide which of the others is x
			
			for (DimsData dd : getDimsData()) {
				if (dd.getAxis()>-1) {
				    dd.setAxis(iaxisValue);
				    found=dd;
				}
			}
			for (DimsData dd : getDimsData()) {
				if (dd==found) continue;
				dd.setAxis(-1);
			}
		}
	}

	public void setTwoAxisOnly(int firstAxis, int secondAxis) {
		boolean foundFirst = false, foundSecond = false;
		for (DimsData dd : getDimsData()) {
			if (dd.getAxis()==firstAxis)  foundFirst  = true;
			if (dd.getAxis()==secondAxis) foundSecond = true;
		}
		
		if (foundFirst&&foundSecond) {
			for (DimsData dd : getDimsData()) {
				if (dd.getAxis()==firstAxis)  continue;
				if (dd.getAxis()==secondAxis) continue;
				dd.setAxis(-1);
			}
			return;
		} else { // We have to decide which of the others is first and second
			
			if (!foundFirst) {
				for (DimsData dd : getDimsData()) {
					if (dd.getAxis()>-1 && dd.getAxis()!=secondAxis) {
					    dd.setAxis(firstAxis);
					    foundFirst = true;
					    break;
					}
				}	
				if (!foundFirst) {
					for (DimsData dd : getDimsData()) {
						if (dd.getAxis()!=secondAxis) {
						    dd.setAxis(firstAxis);
						    foundFirst = true;
						    break;
						}
					}						
				}
			}
			if (!foundSecond) {
				for (DimsData dd : getDimsData()) {
					if (dd.getAxis()>-1 && dd.getAxis()!=firstAxis) {
					    dd.setAxis(secondAxis);
					    foundSecond = true;
					    break;
					}
				}	
				if (!foundSecond) {
					for (DimsData dd : getDimsData()) {
						if (dd.getAxis()!=firstAxis) {
						    dd.setAxis(secondAxis);
						    foundSecond = true;
						    break;
						}
					}	
				}
			}
			
			for (DimsData dd : getDimsData()) {
				if (dd.getAxis()==firstAxis)  continue;
				if (dd.getAxis()==secondAxis) continue;
				dd.setAxis(-1);
			}
			return;
				
		}
		
	}

	public boolean isXFirst() {
		for (DimsData dd : getDimsData()) {
			if (dd.getAxis()<0) continue;
			return dd.getAxis()==0;
		}
		return false;
	}
}
