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
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Box;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Cubic;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.CubicSpline;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.GaussianND;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Quadratic;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.Step;
//import uk.ac.diamond.scisoft.analysis.fitting.functions.StraightLine;


public enum FunctionType {

//	BOX(Box.class),
//	CUBIC(Cubic.class),
//	CUBIC_SPLINE(CubicSpline.class),
	FERMI(Fermi.class),
	GAUSSIAN(Gaussian.class);
//	GAUSSIAN_ND(GaussianND.class),
//	LORENTZIAN(Lorentzian.class),
//	OFFSET(Offset.class),
//	PEARSON_VII(PearsonVII.class),
//	PSEUDO_VOIGT(PseudoVoigt.class),
//	QUADRATIC(Quadratic.class),
//	STEP(Step.class),
//	STRAIGHT_LINE(StraightLine.class);
	
	private Class<? extends AFunction> clazz;

	FunctionType(Class<? extends AFunction> clazz) {
		this.clazz = clazz;
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
		return clazz.getSimpleName();
	}

	public static FunctionType getType(int index) {
		final FunctionType[] ops = FunctionType.values();
		return ops[index];
	}

	public AFunction getFunction() throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

	public static int getIndex(Class<? extends AFunction> class1) {
		final FunctionType[] ops = FunctionType.values();
		for (FunctionType functionType : ops) {
			if (functionType.clazz == class1) return functionType.getIndex();
		}
		return -1;
	}

	public static AFunction createNew(int selectionIndex) throws InstantiationException, IllegalAccessException {
		final FunctionType function = getType(selectionIndex);
		return function.clazz.newInstance();
	}
}
