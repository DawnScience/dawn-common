package org.dawnsci.persistence.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

import org.dawnsci.persistence.roi.CircularROIBean;
import org.dawnsci.persistence.roi.PolylineROIBean;
import org.dawnsci.persistence.roi.ROIBean;
import org.dawnsci.persistence.roi.ROIBeanConverter;
import org.dawnsci.persistence.roi.RectangularROIBean;
import org.dawnsci.persistence.roi.SectorROIBean;

public class ROIBeanConverterTest {

	@Test
	public void testROIBaseToROIBean() {
		//RectangularROI
		double[] startPoint = {100,100};
		double[] lengths = {400, 200};
		ROIBase roi = new RectangularROI(startPoint[0], startPoint[1], lengths[0], lengths[1], 0);
		RectangularROIBean rbean = (RectangularROIBean)ROIBeanConverter.ROIBaseToROIBean("rectangle1", roi);
		assertArrayEquals(startPoint, rbean.getStartPoint(), 0);
		assertArrayEquals(lengths, rbean.getLengths(), 0);
		
		//CircularROI
		double radius = 100;
		roi = new CircularROI(radius, startPoint[0], startPoint[1]);
		CircularROIBean cbean = (CircularROIBean)ROIBeanConverter.ROIBaseToROIBean("circle1", roi);
		assertArrayEquals(startPoint, cbean.getStartPoint(), 0);
		assertEquals(radius, cbean.getRadius(), 0);

		//PolylineROI
		roi = new PolylineROI(startPoint);
		double[] point0 = {102, 102}, point1 = {105, 105};
		((PolylineROI)roi).insertPoint(point0);
		((PolylineROI)roi).insertPoint(point1);
		PolylineROIBean pbean = (PolylineROIBean)ROIBeanConverter.ROIBaseToROIBean("Polyline", roi);
		assertArrayEquals(startPoint, pbean.getStartPoint(), 0);
		assertArrayEquals(startPoint, pbean.getPoints().get(0), 0);
		assertArrayEquals(point0, pbean.getPoints().get(1), 0);
		assertArrayEquals(point1, pbean.getPoints().get(2), 0);

		//SectorROI
		double[] radii = {30, 50}, angles = {6, 9};
		double dpp = 20; int symmetry = 5;
		roi = new SectorROI();
		((SectorROI)roi).setAngles(angles);
		((SectorROI)roi).setPoint(startPoint);
		((SectorROI)roi).setRadii(radii[0], radii[1]);
		((SectorROI)roi).setDpp(dpp);
		((SectorROI)roi).setSymmetry(symmetry);

		SectorROIBean sbean = (SectorROIBean)ROIBeanConverter.ROIBaseToROIBean("Sector", roi);
		assertArrayEquals(startPoint, sbean.getStartPoint(), 0);
		assertArrayEquals(radii, sbean.getRadii(), 0);
		assertArrayEquals(angles, sbean.getAngles(), 0);
		assertEquals(dpp, sbean.getDpp(), 0);
		assertEquals(symmetry, sbean.getSymmetry());
		
	}

	@Test
	public void testROIBeanToROIBase() {
		//RectangularROI
		double[] startPoint = {100,100};
		double[] lengths = {400, 200};
		ROIBean rbean = new RectangularROIBean();
		((RectangularROIBean)rbean).setName("rectangle");
		((RectangularROIBean)rbean).setStartPoint(startPoint);
		((RectangularROIBean)rbean).setType("RectangularROI");
		((RectangularROIBean)rbean).setLengths(lengths);
		((RectangularROIBean)rbean).setAngle(0);
		RectangularROI rroi = (RectangularROI)ROIBeanConverter.ROIBeanToROIBase(rbean);
		assertArrayEquals(startPoint, rroi.getPoint(), 0);
		assertArrayEquals(lengths, rroi.getLengths(), 0);

		//CircularROI
		double radius = 100;
		rbean = new CircularROIBean();
		((CircularROIBean)rbean).setName("circle");
		((CircularROIBean)rbean).setStartPoint(startPoint);
		((CircularROIBean)rbean).setType("CircularROI");
		((CircularROIBean)rbean).setRadius(radius);
		CircularROI croi = (CircularROI)ROIBeanConverter.ROIBeanToROIBase(rbean);
		assertArrayEquals(startPoint, croi.getPoint(), 0);
		assertEquals(radius, croi.getRadius(), 0);

		//PolylineROI
		rbean = new PolylineROIBean();
		double[] point0 = {102, 102}, point1 = {105, 105};
		List<double[]> points = new ArrayList<double[]>();
		//points.add(startPoint);
		points.add(point0);
		points.add(point1);
		((PolylineROIBean)rbean).setName("Polyline");
		((PolylineROIBean)rbean).setPoints(points);
		((PolylineROIBean)rbean).setStartPoint(startPoint);
		((PolylineROIBean)rbean).setType("PolylineROI");
		PolylineROI proi = (PolylineROI)ROIBeanConverter.ROIBeanToROIBase(rbean);
		assertArrayEquals(startPoint, proi.getPoint(), 0);
		assertArrayEquals(point0, proi.getPoint(1).getPoint(), 0);
		assertArrayEquals(point1, proi.getPoint(2).getPoint(), 0);

		//SectorROI
		double[] radii = {30, 50}, angles = {6, 9};
		double dpp = 20; int symmetry = 5;
		rbean = new SectorROIBean();
		((SectorROIBean)rbean).setAngles(angles);
		((SectorROIBean)rbean).setStartPoint(startPoint);
		((SectorROIBean)rbean).setRadii(radii);
		((SectorROIBean)rbean).setDpp(dpp);
		((SectorROIBean)rbean).setSymmetry(symmetry);

		SectorROI sroi = (SectorROI)ROIBeanConverter.ROIBeanToROIBase(rbean);
		assertArrayEquals(startPoint, sroi.getPoint(), 0);
		assertArrayEquals(radii, sroi.getRadii(), 0);
		assertArrayEquals(angles, sroi.getAngles(), 0);
		assertEquals(dpp, sroi.getDpp(), 0);
		assertEquals(symmetry, sroi.getSymmetry());
	}

}
