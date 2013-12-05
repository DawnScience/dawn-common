package org.dawnsci.persistence.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.CircularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolylineROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.ROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.ROIBeanConverter;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RectangularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.SectorROIBean;
import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

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
	public void testRectangularROIConversionToRectangularROIBean() {
		// RectangularROI
		IROI roi = new RectangularROI(startPoint[0], startPoint[1], lengths[0], lengths[1], 0);
		RectangularROIBean rbean = (RectangularROIBean) ROIBeanConverter.iroiToROIBean("rectangle1", roi);
		assertArrayEquals(startPoint, rbean.getStartPoint(), 0);
		assertArrayEquals(lengths, rbean.getLengths(), 0);
	}

	@Test
	public void testCircularROIConversionToCircularROIBean() {
		//CircularROI
		double radius = 100;
		IROI roi = new CircularROI(radius, startPoint[0], startPoint[1]);
		CircularROIBean cbean = (CircularROIBean)ROIBeanConverter.iroiToROIBean("circle1", roi);
		assertArrayEquals(startPoint, cbean.getStartPoint(), 0);
		assertEquals(radius, cbean.getRadius(), 0);
	}

	@Test
	public void testPolylineROIConversionToPolylineROIBean(){
		//PolylineROI
		IROI roi = new PolylineROI(startPoint);
		double[] point0 = {102, 102}, point1 = {105, 105};
		((PolylineROI)roi).insertPoint(point0);
		((PolylineROI)roi).insertPoint(point1);
		PolylineROIBean pbean = (PolylineROIBean)ROIBeanConverter.iroiToROIBean("Polyline", roi);
		assertArrayEquals(startPoint, pbean.getStartPoint(), 0);
		assertArrayEquals(startPoint, pbean.getPoints().get(0), 0);
		assertArrayEquals(point0, pbean.getPoints().get(1), 0);
		assertArrayEquals(point1, pbean.getPoints().get(2), 0);
	}

	@Test
	public void testSectorROIConversionToSectorROIBean(){
		//SectorROI
		double[] radii = {30, 50}, angles = {6, 9};
		double dpp = 20; int symmetry = 5;
		IROI roi = new SectorROI();
		((SectorROI)roi).setAngles(angles);
		((SectorROI)roi).setPoint(startPoint);
		((SectorROI)roi).setRadii(radii[0], radii[1]);
		((SectorROI)roi).setDpp(dpp);
		((SectorROI)roi).setSymmetry(symmetry);
		SectorROIBean sbean = (SectorROIBean)ROIBeanConverter.iroiToROIBean("Sector", roi);
		assertArrayEquals(startPoint, sbean.getStartPoint(), 0);
		assertArrayEquals(radii, sbean.getRadii(), 0);
		assertArrayEquals(angles, sbean.getAngles(), 0);
		assertEquals(dpp, sbean.getDpp(), 0);
		assertEquals(symmetry, sbean.getSymmetry());
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
		RectangularROI rroi = (RectangularROI)ROIBeanConverter.roiBeanToIROI(rbean);
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
		CircularROI croi = (CircularROI)ROIBeanConverter.roiBeanToIROI(rbean);
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
		PolylineROI proi = (PolylineROI)ROIBeanConverter.roiBeanToIROI(rbean);
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

		SectorROI sroi = (SectorROI)ROIBeanConverter.roiBeanToIROI(rbean);
		assertArrayEquals(startPoint, sroi.getPoint(), 0);
		assertArrayEquals(radii, sroi.getRadii(), 0);
		assertArrayEquals(angles, sroi.getAngles(), 0);
		assertEquals(dpp, sroi.getDpp(), 0);
		assertEquals(symmetry, sroi.getSymmetry());
	}
}
