/*
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.common.ui.plot.function;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.CubicSpline;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.GaussianND;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Step;

/**
 * These are the functions editable with the edit table and as such 
 * are not *all* those available from the FunctionFactory 
 * 
 * This class now uses FunctionFactory, although this does not actually
 * make much difference as it was fine as it was. It does mean that 
 * package private constructors could be used for functions and 
 * package private classes to make functions truely swappable at some
 * point in the future.
 */
public enum FunctionType {

	BOX("Box"),
	CUBIC("Cubic"),
//	CUBIC_SPLINE(CubicSpline.class),
	FERMI("Fermi"),
	FERMIGAUSS("Fermi * Gaussian"),
	GAUSSIAN("Gaussian"),
//	GAUSSIAN_ND(GaussianND.class),
	LORENTZIAN("Lorentzian"),
//	OFFSET(Offset.class),
	PEARSON_VII("PearsonVII"),
	POLYNOMIAL("Polynomial"),
	PSEUDO_VOIGT("PseudoVoigt"),
	QUADRATIC("Quadratic"),
//	STEP(Step.class),
	STRAIGHT_LINE("Linear");
	
	private String functionName;

	FunctionType(String functionName) {
		this.functionName = functionName;
	}
	
	public int getIndex() {
		final FunctionType[] ops = FunctionType.values();
		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
		return -1;
	}

	public static String[] getTypes() {
		final FunctionType[] ops = FunctionType.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public String getName() {
		return functionName;
	}

	public static FunctionType getType(int index) {
		final FunctionType[] ops = FunctionType.values();
		return ops[index];
	}

	public AFunction getFunction() throws Exception {
		return FunctionFactory.getFunction(functionName);
	}

	public static int getIndex(Class<? extends AFunction> class1) {
		
		try {
			String name = FunctionFactory.getName(class1);
			final FunctionType[] ops = FunctionType.values();
			for (FunctionType functionType : ops) {
				if (functionType.functionName == name) return functionType.getIndex();
			}
		} catch (Exception e) {
			return -1;
		}
		return -1;
	}

	public static AFunction createNew(int selectionIndex) throws Exception {
		final FunctionType function = getType(selectionIndex);
		return FunctionFactory.getFunction(function.functionName);
	}

	public static AFunction createNew(FunctionType function) throws Exception {
		return FunctionFactory.getFunction(function.functionName);
	}
}
