/*-
 *******************************************************************************
 * Copyright (c) 2017 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Timothy Spain - elementary functions, floor and ceil, max, min and a few
 *    				  others
 *******************************************************************************/


package org.dawnsci.jexl.internal;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Maths;

public class JexlMaths {
	
	public static Dataset power(final Object a, final Object b) {
		return Maths.power(a, b);
	}

	public static Dataset minimum(final Object a, final Object b) {
		return Maths.minimum(a, b);
	}

	public static Dataset maximum(final Object a, final Object b) {
		return Maths.maximum(a, b);
	}

	public static Dataset sqrt(final Object a) {
		return Maths.sqrt(a);
	}

	public static Dataset cbrt(final Object a) {
		return Maths.cbrt(a);
	}

	public static Dataset square(final Object a) {
		return Maths.square(a);
	}

	public static Dataset signum(final Object a) {
		return Maths.signum(a);
	}

	public static Dataset floor(final Object a) {
		return Maths.floor(a);
	}

	public static Dataset ceil(final Object a) {
		return Maths.ceil(a);
	}

	public static Dataset exp(final Object a) {
		return Maths.exp(a);
	}

	public static Dataset expm1(final Object a) {
		return Maths.expm1(a);
	}

	public static Dataset log(final Object a) {
		return Maths.log(a);
	}

	public static Dataset log10(final Object a) {
		return Maths.log10(a);
	}

	public static Dataset log1p(final Object a) {
		return Maths.log1p(a);
	}

	public static Dataset log2(final Object a) {
		return Maths.log2(a);
	}

	public static Dataset sin(final Object a) {
		return Maths.sin(a);
	}

	public static Dataset cos(final Object a) {
		return Maths.cos(a);
	}

	public static Dataset tan(final Object a) {
		return Maths.tan(a);
	}

	public static Dataset arcsin(final Object a) {
		return Maths.arcsin(a);
	}

	public static Dataset arccos(final Object a) {
		return Maths.arccos(a);
	}

	public static Dataset arctan(final Object a) {
		return Maths.arctan(a);
	}

	public static Dataset toDegrees(final Object a) {
		return Maths.toDegrees(a);
	}

	public static Dataset toRadians(final Object a) {
		return Maths.toRadians(a);
	}

	public static Dataset sinh(final Object a) {
		return Maths.sinh(a);
	}

	public static Dataset cosh(final Object a) {
		return Maths.cosh(a);
	}

	public static Dataset tanh(final Object a) {
		return Maths.tanh(a);
	}

	public static Dataset arcsinh(final Object a) {
		return Maths.arcsinh(a);
	}

	public static Dataset arccosh(final Object a) {
		return Maths.arccosh(a);
	}

	public static Dataset arctanh(final Object a) {
		return Maths.arctanh(a);
	}

}
