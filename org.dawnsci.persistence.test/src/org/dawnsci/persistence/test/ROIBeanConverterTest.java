package org.dawnsci.persistence.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.persistence.json.roi.CircularROIBean;
import org.dawnsci.persistence.json.roi.PolylineROIBean;
import org.dawnsci.persistence.json.roi.ROIBean;
import org.dawnsci.persistence.json.roi.RectangularROIBean;
import org.dawnsci.persistence.json.roi.SectorROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.junit.Before;
import org.junit.Test;

public class ROIBeanConverterTest {

	double[] startPoint = new double[2];
	double[] lengths = new double[2];

	@Before
	public void setup() {
		startPoint[0] = 100;
		startPoint[1] = 100;
		lengths[0] = 400;
		lengths[1] = 200;
	}

	@Test
	public void testRectangularROIBeanConversionToRectangularROI(){
		//RectangularROI
		ROIBean rbean = new RectangularROIBean();
		((RectangularROIBean)rbean).setName("rectangle");
		((RectangularROIBean)rbean).setStartPoint(startPoint);
		((RectangularROIBean)rbean).setType("RectangularROI");
		((RectangularROIBean)rbean).setLengths(lengths);
		((RectangularROIBean)rbean).setAngle(0);
		RectangularROI rroi = (RectangularROI)rbean.getROI();
		assertArrayEquals(startPoint, rroi.getPoint(), 0);
		assertArrayEquals(lengths, rroi.getLengths(), 0);
	}

	@Test
	public void testCircularROIBeanConversionToCircularROI(){
		//CircularROI
		double radius = 100;
		ROIBean rbean = new CircularROIBean();
		((CircularROIBean)rbean).setName("circle");
		((CircularROIBean)rbean).setStartPoint(startPoint);
		((CircularROIBean)rbean).setType("CircularROI");
		((CircularROIBean)rbean).setRadius(radius);
		CircularROI croi = (CircularROI)rbean.getROI();
		assertArrayEquals(startPoint, croi.getPoint(), 0);
		assertEquals(radius, croi.getRadius(), 0);
	}

	@Test
	public void testPolylineROIBeanConversionToPolylineROI(){
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
		PolylineROI proi = (PolylineROI)rbean.getROI();
		assertArrayEquals(startPoint, proi.getPoint(), 0);
		assertArrayEquals(point0, proi.getPoint(1).getPoint(), 0);
		assertArrayEquals(point1, proi.getPoint(2).getPoint(), 0);
	}

	@Test
	public void testSectorROIBeanConversionToSectorROI(){
		//SectorROI
		double[] radii = {30, 50}, angles = {6, 9};
		double dpp = 20; int symmetry = 5;
		ROIBean rbean = new SectorROIBean();
		((SectorROIBean)rbean).setAngles(angles);
		((SectorROIBean)rbean).setStartPoint(startPoint);
		((SectorROIBean)rbean).setRadii(radii);
		((SectorROIBean)rbean).setDpp(dpp);
		((SectorROIBean)rbean).setSymmetry(symmetry);

		SectorROI sroi = (SectorROI)rbean.getROI();
		assertArrayEquals(startPoint, sroi.getPoint(), 0);
		assertArrayEquals(radii, sroi.getRadii(), 0);
		assertArrayEquals(angles, sroi.getAngles(), 0);
		assertEquals(dpp, sroi.getDpp(), 0);
		assertEquals(symmetry, sroi.getSymmetry());
	}
}
