package org.dawnsci.persistence.test;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

import org.dawnsci.persistence.roi.CircularROIBean;
import org.dawnsci.persistence.roi.PolylineROIBean;
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
		//fail("Not yet implemented");
	}

}
