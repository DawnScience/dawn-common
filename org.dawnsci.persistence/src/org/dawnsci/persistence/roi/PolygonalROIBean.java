package org.dawnsci.persistence.roi;

import java.util.Arrays;
import java.util.List;


/**
 * Polygonal ROI bean
 * @author wqk87977
 *
 */
public class PolygonalROIBean extends ROIBean{

	private List<double[]> points;

	public PolygonalROIBean(){
		type = "PolygonalROI";
	}

	/**
	 * Set the list of points 
	 * @param lengths
	 */
	public void setPoints(List<double[]> points){
		this.points = points;
	}

	/**
	 * Returns the list of points (x[0] and y[1] coordinates)
	 * @return points
	 */
	public List<double[]> getPoints(){
		return points;
	}

	@Override
	public String toString(){
		return String.format("{\"type\": \"%s\", \"name\": \"%s\", \"startPoint\": \"%s\", \"points\": \"%s\"}", 
				type, name, Arrays.toString(startPoint), Arrays.toString(points.toArray()));
	}
}
