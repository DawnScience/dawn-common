package org.dawnsci.persistence.roi;

import java.util.Arrays;


/**
 * Rectangular ROI bean
 * @author wqk87977
 *
 */
public class RectangularROIBean extends ROIBean{

	private double[] lengths; // width and height

	private double angle;   // angle in radians

	private double[] endPoint; // end point

	public RectangularROIBean(){
		type = "RectangularROI";
	}

	/**
	 * Returns the lengths (width[0] and height[1])
	 * @return
	 */
	public double[] getLengths(){
		return lengths;
	}

	/**
	 * Returns the angle
	 * @return
	 */
	public double getAngle(){
		return angle;
	}

	/**
	 * Returns the End point of the rectangle
	 * @return
	 */
	public double[] getEndPoint(){
		return endPoint;
	}

	/**
	 * Set the width[0] and height[1] 
	 * @param lengths
	 */
	public void setLengths(double[] lengths){
		this.lengths = lengths;
	}

	/**
	 * Set the angle
	 * @param angle
	 */
	public void setAngle(double angle){
		this.angle = angle;
	}

	/**
	 * Set the end point of the Rectangle
	 * @param endPoint
	 */
	public void setEndPoint(double[] endPoint){
		this.endPoint = endPoint;
	}

	@Override
	public String toString(){
		return String.format("{\"type\": \"%s\", \"name\": \"%s\", \"startPoint\": \"%s\", \"endPoint\": \"%s\", \"angle\": \"%s\"}", 
				type, name, Arrays.toString(startPoint), Arrays.toString(endPoint), angle);
	}
}
