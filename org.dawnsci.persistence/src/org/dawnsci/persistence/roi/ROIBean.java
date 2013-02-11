package org.dawnsci.persistence.roi;

/**
 * ROI bean
 * @author wqk87977
 *
 */
public class ROIBean {

	protected String type;

	protected String name;

	protected double startPoint[]; // start or centre coordinates

	public ROIBean(){
		
	}

	/**
	 * Returns the type of roibean
	 * @return type
	 */
	public String getType(){
		return type;
	}

	/**
	 * Returns the ROI name
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the start (or centre) point x[0] and y[1]
	 * @return startPoint
	 */
	public double[] getStartPoint() {
		return startPoint;
	}

	/**
	 * Set the type of roibean
	 * @param type
	 */
	public void setType(String type){
		this.type = type;
	}

	/**
	 * Set the name
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * Set the Start point of the ROI x,y coordinates with x=[0] and y=[1]
	 * @param startPoint
	 */
	public void setStartPoint(double[] startPoint){
		this.startPoint = startPoint;
	}
}
