/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.gpu;


import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

/**
 * Operation which supports basic operators.
 * 
 * @author Matthew Gerring
 *
 */
class BasicGPUOperation implements IOperation {

	private ArrayOperationKernel  arrayKernel;
	private ScalarOperationKernel scalarKernel;
	
	@Override
	public Dataset process(Dataset a, double b, Operator operation) {
		
		if (Boolean.getBoolean("org.dawb.common.gpu.operation.use.cpu")) {
			switch (operation) {
			case ADD:
				return Maths.add(a,b);
			case SUBTRACT:
				return Maths.subtract(a,b);
			case MULTIPLY:
				return Maths.multiply(a,b);
			case DIVIDE:
				return Maths.divide(a,b);
			}
		}
		
		Object[] prims = getPrimitives(a);
	    int[]    ia    = (int[])prims[0];
	    double[] da    = (double[])prims[1];

		if (scalarKernel==null) {
			scalarKernel = new ScalarOperationKernel();
		}

		scalarKernel.setOperation(operation.getIndex());
		scalarKernel.setUseInt(ia!=null);
		scalarKernel.setIa(ia!=null?ia:new int[]{1});    // Not allowed nulls
		scalarKernel.setDa(da!=null?da:new double[]{1}); // Not allowed nulls
		scalarKernel.setB(b);

		Range range = Range.create(a.getSize()); 
		scalarKernel.execute(range);

		return new DoubleDataset(scalarKernel.getResult(), a.getShape());
	}

	@Override
	public Dataset process(Dataset a, Dataset b, Operator operation) {
		
		if (Boolean.getBoolean("org.dawb.common.gpu.operation.use.cpu")) {
			switch (operation) {
			case ADD:
				return Maths.add(a,b);
			case SUBTRACT:
				return Maths.subtract(a,b);
			case MULTIPLY:
				return Maths.multiply(a,b);
			case DIVIDE:
				return Maths.divide(a,b);
			}
		}
		
		Object[] prims = getPrimitives(a);
	    int[]    ia    = (int[])prims[0];
	    double[] da    = (double[])prims[1];

		prims = getPrimitives(b);
	    int[]    ib    = (int[])prims[0];
	    double[] db    = (double[])prims[1];
     
        if (arrayKernel==null) {
        	arrayKernel = new ArrayOperationKernel();
        }

        arrayKernel.setUseAInt(ia!=null);
        arrayKernel.setIa(ia!=null?ia:new int[]{1});    // Not allowed nulls
        arrayKernel.setDa(da!=null?da:new double[]{1}); // Not allowed nulls
       
        arrayKernel.setUseBInt(ib!=null);
        arrayKernel.setIb(ib!=null?ib:new int[]{1});    // Not allowed nulls
        arrayKernel.setDb(db!=null?db:new double[]{1}); // Not allowed nulls
        
        arrayKernel.setOperation(operation.getIndex());
        
		Range range = Range.create(a.getSize()); 
		arrayKernel.execute(range);

        return new DoubleDataset(arrayKernel.getResult(), a.getShape());
	}
	
	private static final Object[] getPrimitives(Dataset a) {
		
		final int[]     ia = a instanceof IntegerDataset
                           ? ((IntegerDataset)a).getData()
		                   : null;
                           
        final double[]  da = ia==null
		                   ? ((DoubleDataset)DatasetUtils.cast(a, Dataset.FLOAT)).getData()
	                       : null;
		                   
		return new Object[]{ia,da};
	}

	/**
	 * Dispose is not a final state. You can still reuse the IOperation after this.
	 */
	@Override
	public void deactivate() {
		if (arrayKernel!=null) {
			arrayKernel.dispose();
			arrayKernel = null;
		}
		if (scalarKernel!=null) {
			scalarKernel.dispose();
			scalarKernel = null;
		}
	}
	
	private static class ArrayOperationKernel extends Kernel {

		private boolean   useAInt=true;
		private boolean   useBInt=true;
		private double[]  da, db;
		private int[]     ia, ib;
		private double[]  result;
		private int operation;
		public double[] getResult() {
			return result;
		}
		public void setDa(double[] da) {
			this.da = da;
		}
		public void setDb(double[] db) {
			this.db = db;
		}
		public void setOperation(int operation) {
			this.operation = operation;
		}
		
		public Kernel execute(Range range) {
			result = new double[Math.max(useAInt?ia.length:da.length, useBInt?ib.length:db.length)];
			return super.execute(range);
		}
		
		@Override 
		public void run(){

			int i       = getGlobalId();
			double aVal = useAInt ? ia[i] : da[i];
			double bVal = useBInt ? ib[i] : db[i];
			
			if (operation==0) {
				result[i] = aVal + bVal;

			} else if (operation==1) {
				result[i] = aVal - bVal;

			} else if (operation==2) {
				result[i] = aVal * bVal;

			} else if (operation==3) {
				result[i] = aVal / bVal;
				return;
			}
		}
		public void setUseAInt(boolean useAInt) {
			this.useAInt = useAInt;
		}
		public void setUseBInt(boolean useBInt) {
			this.useBInt = useBInt;
		}
		public void setIa(int[] ia) {
			this.ia = ia;
		}
		public void setIb(int[] ib) {
			this.ib = ib;
		}
	}


	static class ScalarOperationKernel extends Kernel {
		
		private int       operation;
		private boolean   useInt=true;
		private int[]     ia;
		private double[]  da;
		private double    b;
		private double[]  result;
		public double[] getResult() {
			return result;
		}
		public void setDa(double[] da) {
			this.da = da;
		}
		public void setB(double b) {
			this.b = b;
		}
		
		public Kernel execute(Range range) {
			result = new double[useInt?ia.length:da.length];
			return super.execute(range);
		}
		
		@Override 
		public void run(){
			int i     = getGlobalId();
			if (operation==0) {
				if (useInt) {
					result[i]  = ia[i]+b;
				} else {
					result[i]  = da[i]+b;
				}

			} else if (operation==1) {
				if (useInt) {
					result[i]  = ia[i]-b;
				} else {
					result[i]  = da[i]-b;
				}

			} else if (operation==2) {
				if (useInt) {
					result[i]  = ia[i]*b;
				} else {
					result[i]  = da[i]*b;
				}

			} else if (operation==3) {
				if (useInt) {
					result[i]  = ia[i]/b;
				} else {
					result[i]  = da[i]/b;
				}
			}

		}
		public void setIa(int[] ia) {
			this.ia = ia;
		}
		public void setUseInt(boolean useInt) {
			this.useInt = useInt;
		}
		public void setOperation(int operation) {
			this.operation = operation;
		}
	}

}
