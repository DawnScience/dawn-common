/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.roi.json.CircularROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.LinearROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.PointROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.PolylineROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBeanFactory;
import org.eclipse.dawnsci.analysis.dataset.roi.json.RectangularROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.SectorROIBean;
import org.junit.Before;
import org.junit.Test;

public class ROIBeanConverterTest {

	double[] startPoint = new double[2];
	double[] lengths = new double[2];
	double[] endPoint = new double[2];

	@Before
	public void setup() {
		startPoint[0] = 100;
		startPoint[1] = 100;
		endPoint[0] = 500;
		endPoint[1] = 300;
		lengths[0] = 400;
		lengths[1] = 200;
	}

	@Test
	public void testRectangularROIBeanConversionToRectangularROI() throws Exception {
		//RectangularROI
		ROIBean rbean = new RectangularROIBean();
		((RectangularROIBean)rbean).setName("rectangle");
		((RectangularROIBean)rbean).setStartPoint(startPoint);
		((RectangularROIBean)rbean).setType("RectangularROI");
		((RectangularROIBean)rbean).setLengths(lengths);
		((RectangularROIBean)rbean).setAngle(0);
		RectangularROI rroi = (RectangularROI)ROIBeanFactory.decapsulate(rbean);
		assertArrayEquals(startPoint, rroi.getPoint(), 0);
		assertArrayEquals(lengths, rroi.getLengths(), 0);
	}

	@Test
	public void testCircularROIBeanConversionToCircularROI()throws Exception {
		//CircularROI
		double radius = 100;
		ROIBean rbean = new CircularROIBean();
		((CircularROIBean)rbean).setName("circle");
		((CircularROIBean)rbean).setStartPoint(startPoint);
		((CircularROIBean)rbean).setType("CircularROI");
		((CircularROIBean)rbean).setRadius(radius);
		CircularROI croi = (CircularROI)ROIBeanFactory.decapsulate(rbean);
		assertArrayEquals(startPoint, croi.getPoint(), 0);
		assertEquals(radius, croi.getRadius(), 0);
	}

	@Test
	public void testPolylineROIBeanConversionToPolylineROI() throws Exception {
		//PolylineROI
		ROIBean rbean = new PolylineROIBean();
		double[] point0 = {102, 102}, point1 = {105, 105};
		List<double[]> points = new ArrayList<double[]>();
		//points.add(startPoint);
		points.add(point0);
		points.add(point1);
		((PolylineROIBean)rbean).setName("Polyline");
		((PolylineROIBean)rbean).setPoints(points);
		((PolylineROIBean)rbean).setStartPoint(startPoint);
		((PolylineROIBean)rbean).setType("PolylineROI");
		PolylineROI proi = (PolylineROI)ROIBeanFactory.decapsulate(rbean);
		assertArrayEquals(startPoint, proi.getPoint(), 0);
		assertArrayEquals(point0, proi.getPoint(1).getPoint(), 0);
		assertArrayEquals(point1, proi.getPoint(2).getPoint(), 0);
	}

	@Test
	public void testSectorROIBeanConversionToSectorROI() throws Exception {
		//SectorROI
		double[] radii = {30, 50}, angles = {6, 9};
		double dpp = 20; int symmetry = 5;
		ROIBean rbean = new SectorROIBean();
		((SectorROIBean)rbean).setAngles(angles);
		((SectorROIBean)rbean).setStartPoint(startPoint);
		((SectorROIBean)rbean).setRadii(radii);
		((SectorROIBean)rbean).setDpp(dpp);
		((SectorROIBean)rbean).setSymmetry(symmetry);

		SectorROI sroi = (SectorROI)ROIBeanFactory.decapsulate(rbean);
		assertArrayEquals(startPoint, sroi.getPoint(), 0);
		assertArrayEquals(radii, sroi.getRadii(), 0);
		assertArrayEquals(angles, sroi.getAngles(), 0);
		assertEquals(dpp, sroi.getDpp(), 0);
		assertEquals(symmetry, sroi.getSymmetry());
	}

	@Test
	public void testPointROIBeanEqual() throws Exception {
		ROIBean rbean = new PointROIBean();
		((PointROIBean)rbean).setName("point");
		((PointROIBean)rbean).setStartPoint(startPoint);
		((PointROIBean)rbean).setType("PointROI");
		PointROI proi = (PointROI)ROIBeanFactory.decapsulate(rbean);
		PointROIBean resultBean = (PointROIBean)ROIBeanFactory.encapsulate(proi);
		assertEquals(rbean, resultBean);
	}

	@Test
	public void testLinearROIBeanEqual() throws Exception {
		ROIBean rbean = new LinearROIBean();
		((LinearROIBean)rbean).setName("line");
		((LinearROIBean)rbean).setStartPoint(startPoint);
		((LinearROIBean)rbean).setType("LinearROI");
		((LinearROIBean)rbean).setEndPoint(endPoint);
		LinearROI lroi = (LinearROI)ROIBeanFactory.decapsulate(rbean);
		LinearROIBean resultBean = (LinearROIBean)ROIBeanFactory.encapsulate(lroi);
		assertEquals(rbean, resultBean);
	}

	@Test
	public void testRectangularROIBeanEqual() throws Exception {
		ROIBean rbean = new RectangularROIBean();
		((RectangularROIBean)rbean).setName("rectangle");
		((RectangularROIBean)rbean).setStartPoint(startPoint);
		((RectangularROIBean)rbean).setType("RectangularROI");
		((RectangularROIBean)rbean).setLengths(lengths);
		((RectangularROIBean)rbean).setEndPoint(endPoint);
		((RectangularROIBean)rbean).setAngle(0);
		RectangularROI rroi = (RectangularROI)ROIBeanFactory.decapsulate(rbean);
		RectangularROIBean resultBean = (RectangularROIBean)ROIBeanFactory.encapsulate(rroi);
		assertEquals(rbean, resultBean);
	}

	@Test
	public void testCircularROIBeanEqual()throws Exception {
		//CircularROI
		double radius = 100;
		ROIBean rbean = new CircularROIBean();
		((CircularROIBean)rbean).setName("circle");
		((CircularROIBean)rbean).setStartPoint(startPoint);
		((CircularROIBean)rbean).setType("CircularROI");
		((CircularROIBean)rbean).setRadius(radius);
		CircularROI croi = (CircularROI)ROIBeanFactory.decapsulate(rbean);
		CircularROIBean resultBean = (CircularROIBean)ROIBeanFactory.encapsulate(croi);
		assertEquals(rbean, resultBean);
	}

	@Test
	public void testSectorROIBeanEqual() throws Exception {
		//SectorROI
		double[] radii = {30, 50}, angles = {6, 9};
		double dpp = 20; int symmetry = 5;
		ROIBean rbean = new SectorROIBean();
		((SectorROIBean)rbean).setAngles(angles);
		((SectorROIBean)rbean).setStartPoint(startPoint);
		((SectorROIBean)rbean).setRadii(radii);
		((SectorROIBean)rbean).setDpp(dpp);
		((SectorROIBean)rbean).setSymmetry(symmetry);
		SectorROI sroi = (SectorROI)ROIBeanFactory.decapsulate(rbean);
		SectorROIBean resultBean = (SectorROIBean)ROIBeanFactory.encapsulate(sroi);
		assertEquals(rbean, resultBean);
	}
}
