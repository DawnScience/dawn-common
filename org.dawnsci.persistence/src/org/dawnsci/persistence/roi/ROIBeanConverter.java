package org.dawnsci.persistence.roi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * Class used to convert from a ROIBase to a ROIBean and vice-versa
 * @author wqk87977
 *
 */
public class ROIBeanConverter {

	static private Logger logger = LoggerFactory.getLogger(ROIBeanConverter.class);

	/**
	 * Method that converts a ROIBase to a ROIBean
	 * @param name
	 * @param roi
	 * @return ROIBean
	 */
	public static ROIBean ROIBaseToROIBean(String name, ROIBase roi){
		if(roi instanceof RectangularROI){
			RectangularROI rroi = (RectangularROI) roi;
			RectangularROIBean rroibean = new RectangularROIBean();
			rroibean.setName(name);
			rroibean.setStartPoint(rroi.getPoint());
			rroibean.setEndPoint(rroi.getEndPoint());
			rroibean.setAngle(rroi.getAngle());
			rroibean.setLengths(rroi.getLengths());
			return rroibean;
		} else if(roi instanceof PolylineROI){
			PolylineROI plroi = (PolylineROI) roi;
			PolylineROIBean plroibean = new PolylineROIBean();
			plroibean.setName(name);
			plroibean.setStartPoint(plroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for(int i = 0; i<plroi.getNumberOfPoints(); i++){
				points.add(plroi.getPoint(i).getPoint());
			}
			plroibean.setPoints(points);
			return plroibean;
		} else if (roi instanceof SectorROI){
			SectorROI sroi = (SectorROI) roi;
			SectorROIBean sroibean = new SectorROIBean();
			sroibean.setName(name);
			sroibean.setStartPoint(sroi.getPoint());
			sroibean.setAngles(sroi.getAngles());
			sroibean.setRadii(sroi.getRadii());
			sroibean.setSymmetry(sroi.getSymmetry());
			sroibean.setDpp(sroi.getDpp());
			return sroibean;
		} else if(roi instanceof CircularROI){
			CircularROI croi = (CircularROI) roi;
			CircularROIBean croibean = new CircularROIBean();
			croibean.setName(name);
			croibean.setStartPoint(croi.getCentre());
			croibean.setRadius(croi.getRadius());
			return croibean;
		} else {
			logger.debug("This type is not supported");
		}
		return null;
	}

	/**
	 * Method that converts a roi bean to a ROiBase
	 * @param rbean
	 * @return ROIBase
	 */
	public static ROIBase ROIBeanToROIBase(ROIBean rbean){
		if(rbean instanceof RectangularROIBean){
			RectangularROIBean rroibean = (RectangularROIBean) rbean;
			RectangularROI rroi = new RectangularROI(rroibean.getStartPoint()[0], 
					rroibean.getStartPoint()[1], rroibean.getLengths()[0], 
					rroibean.getLengths()[1], rroibean.getAngle());
			return rroi;
		} else if(rbean.type.equals("PolylineROI")){
			PolylineROIBean plroibean = (PolylineROIBean) rbean;
			PolylineROI plroi = new PolylineROI(plroibean.getStartPoint());
			Iterator<double[]> it = plroibean.getPoints().iterator();
			while (it.hasNext()){
				double[] point = it.next();
				plroi.insertPoint(point);
			}
			return plroi;
		} else if (rbean.type.equals("SectorROI")){
			SectorROIBean sroibean = (SectorROIBean) rbean;
			
			SectorROI sroi = new SectorROI();
			sroi.setPoint(sroibean.getStartPoint());
			sroi.setRadii(sroibean.getRadii());
			sroi.setAngles(sroibean.getAngles());
			sroi.setDpp(sroibean.getDpp());
			sroi.setSymmetry(sroibean.getSymmetry());

			return sroi;
		} else if(rbean.type.equals("CircularROI")){
			CircularROIBean croibean = (CircularROIBean) rbean;

			CircularROI croi = new CircularROI(croibean.getRadius(), 
					croibean.getStartPoint()[0], croibean.getStartPoint()[1]);
			
			return croi;
		} else {
			logger.debug("This type is not supported");
		}
		return null;
	}

	/**
	 * Method that returns a ROIBean from a JSON string
	 * @param gson
	 * @param json
	 * @return ROIBean
	 */
	public static ROIBean getROIBeanfromJSON(Gson gson, String json){
		ROIBean roibean = gson.fromJson(json, ROIBean.class);

		if(roibean.getType().equals("RectangularROI")){
			return gson.fromJson(json, RectangularROIBean.class);
		} else if(roibean.getType().equals("CircularROI")){
			return gson.fromJson(json, CircularROIBean.class);
		} else if(roibean.getType().equals("PolylineROI")){
			return gson.fromJson(json, PolylineROIBean.class);
		} else if(roibean.getType().equals("SectorROI")){
			return gson.fromJson(json, SectorROIBean.class);
		}
		return null;
	}
}
