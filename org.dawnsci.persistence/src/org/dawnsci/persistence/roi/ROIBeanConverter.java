package org.dawnsci.persistence.roi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.FreeDrawROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RingROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

import com.google.gson.Gson;

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
	public static ROIBean ROIBaseToROIBean(String name, IROI roi){
		Class<? extends IROI> roiClass = roi.getClass();
		if(roiClass == RectangularROI.class){
			RectangularROI rroi = (RectangularROI) roi;
			RectangularROIBean rroibean = new RectangularROIBean();
			rroibean.setName(name);
			rroibean.setStartPoint(rroi.getPoint());
			rroibean.setEndPoint(rroi.getEndPoint());
			rroibean.setAngle(rroi.getAngle());
			rroibean.setLengths(rroi.getLengths());
			return rroibean;

		} else if(roiClass == PolylineROI.class){
			PolylineROI plroi = (PolylineROI) roi;
			PolylineROIBean plroibean = new PolylineROIBean();
			plroibean.setName(name);
			plroibean.setStartPoint(plroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for (int i = 0; i < plroi.getNumberOfPoints(); i++) {
				points.add(plroi.getPoint(i).getPoint());
			}
			plroibean.setPoints(points);
			return plroibean;

		} else if(roiClass == PolygonalROI.class){
			PolygonalROI pgroi = (PolygonalROI) roi;
			PolygonalROIBean pgroibean = new PolygonalROIBean();
			pgroibean.setName(name);
			pgroibean.setStartPoint(pgroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for (int i = 0; i < pgroi.getNumberOfPoints(); i++) {
				points.add(pgroi.getPoint(i).getPoint());
			}
			pgroibean.setPoints(points);
			return pgroibean;

		} else if(roiClass == FreeDrawROI.class){
			FreeDrawROI fdroi = (FreeDrawROI) roi;
			FreedrawROIBean fdroibean = new FreedrawROIBean();
			fdroibean.setName(name);
			fdroibean.setStartPoint(fdroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for(int i = 0; i<fdroi.getNumberOfPoints(); i++){
				points.add(fdroi.getPoint(i).getPoint());
			}
			fdroibean.setPoints(points);
			return fdroibean;

		} else if(roiClass == SectorROI.class){
			SectorROI sroi = (SectorROI)roi;
			SectorROIBean sroibean = new SectorROIBean();
			sroibean.setName(name);
			sroibean.setStartPoint(sroi.getPoint());
			sroibean.setAngles(sroi.getAngles());
			sroibean.setRadii(sroi.getRadii());
			sroibean.setSymmetry(sroi.getSymmetry());
			sroibean.setDpp(sroi.getDpp());
			return sroibean;

		} else if(roiClass == RingROI.class){
			RingROI rroi = (RingROI) roi;
			RingROIBean rroibean = new RingROIBean();
			rroibean.setName(name);
			rroibean.setStartPoint(rroi.getPoint());
			rroibean.setAngles(rroi.getAngles());
			rroibean.setRadii(rroi.getRadii());
			rroibean.setSymmetry(rroi.getSymmetry());
			rroibean.setDpp(rroi.getDpp());
			return rroibean;

		} else if(roiClass == CircularROI.class){
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
		} else if(rbean instanceof PolylineROIBean){
			PolylineROIBean plroibean = (PolylineROIBean) rbean;
			PolylineROI plroi = new PolylineROI(plroibean.getStartPoint());
			Iterator<double[]> it = plroibean.getPoints().iterator();
			while (it.hasNext()) {
				double[] point = it.next();
				plroi.insertPoint(point);
			}
			return plroi;
		} else if(rbean instanceof PolygonalROIBean){
			PolygonalROIBean pgroibean = (PolygonalROIBean) rbean;
			PolygonalROI pgroi = new PolygonalROI(pgroibean.getStartPoint());
			Iterator<double[]> it = pgroibean.getPoints().iterator();
			while (it.hasNext()) {
				double[] point = it.next();
				pgroi.insertPoint(point);
			}
			return pgroi;
		} else if(rbean instanceof FreedrawROIBean){
			FreedrawROIBean fdroibean = (FreedrawROIBean) rbean;
			FreeDrawROI fdroi = new FreeDrawROI(fdroibean.getStartPoint());
			Iterator<double[]> it = fdroibean.getPoints().iterator();
			while (it.hasNext()){
				double[] point = it.next();
				fdroi.insertPoint(point);
			}
			return fdroi;
		} else if (rbean instanceof SectorROIBean){
			SectorROIBean sroibean = (SectorROIBean) rbean;
			SectorROI sroi = new SectorROI();
			sroi.setPoint(sroibean.getStartPoint());
			sroi.setRadii(sroibean.getRadii());
			sroi.setAngles(sroibean.getAngles());
			sroi.setDpp(sroibean.getDpp());
			sroi.setSymmetry(sroibean.getSymmetry());
			return sroi;
		} else if(rbean instanceof RingROIBean){
			RingROIBean sroibean = (RingROIBean) rbean;
			RingROI sroi = new RingROI();
			sroi.setPoint(sroibean.getStartPoint());
			sroi.setRadii(sroibean.getRadii());
			sroi.setAngles(sroibean.getAngles());
			sroi.setDpp(sroibean.getDpp());
			sroi.setSymmetry(sroibean.getSymmetry());
			return sroi;
		} else if(rbean instanceof CircularROIBean){
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
		if(roibean == null) return null;
		if(roibean!=null && roibean.getType() == null) return null;
		
		if(roibean.getType().equals("RectangularROI")){
			return gson.fromJson(json, RectangularROIBean.class);
		} else if(roibean.getType().equals("CircularROI")){
			return gson.fromJson(json, CircularROIBean.class);
		} else if(roibean.getType().equals("PolylineROI")){
			return gson.fromJson(json, PolylineROIBean.class);
		} else if(roibean.getType().equals("PolygonalROI")){
			return gson.fromJson(json, PolygonalROIBean.class);
		} else if(roibean.getType().equals("FreedrawROI")){
			return gson.fromJson(json, FreedrawROIBean.class);
		} else if(roibean.getType().equals("SectorROI")){
			return gson.fromJson(json, SectorROIBean.class);
		} else if(roibean.getType().equals("RingROI")){
			return gson.fromJson(json, RingROIBean.class);
		}
		return null;
	}

	/**
	 * Method that returns true if the type of ROI is supported by the ROIBeanConverter
	 * @return
	 */
	public static boolean isROISupported(IROI roi){
		if(roi instanceof CircularROI)return true;
		else if(roi instanceof RectangularROI)return true;
		else if(roi instanceof SectorROI)return true;
		else if(roi instanceof RingROI)return true;
		else if(roi instanceof FreeDrawROI)return true;
		else if(roi instanceof PolylineROI)return true;
		else if(roi instanceof PolygonalROI)return true;
		else return false;
	}
}
