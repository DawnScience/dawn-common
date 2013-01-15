package org.dawb.common.ui.databinding.model;

import org.dawb.common.ui.databinding.AbstractModelObject;

/**
 * Model object for a Region Of Interest row used in a Region Table
 * @author wqk87977
 *
 */
public class RegionRowDataModel extends AbstractModelObject {
	private String name;
	private double xStart;
	private double xEnd;
	private double width;
	private double yStart;
	private double yEnd;
	private double height;

	public RegionRowDataModel() {
	}

	public RegionRowDataModel(String name, double xStart, double xEnd, double width,
			double yStart, double yEnd, double height) {
		this.name = name;
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.width = width;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.height = height;
	}

	public String getName() {
		return name;
	}

	public double getxStart() {
		return xStart;
	}

	public double getxEnd() {
		return xEnd;
	}

	public double getWidth() {
		return width;
	}

	public double getyStart() {
		return yStart;
	}

	public double getyEnd() {
		return yEnd;
	}

	public double getHeight() {
		return height;
	}

	public void setName(String name){
		String oldValue = this.name;
		this.name = name;
		firePropertyChange("name", oldValue, this.name);
	}

	public void setxStart(double xStart) {
		double oldValue = this.xStart;
		this.xStart = xStart;
		firePropertyChange("xStart", oldValue, this.xStart);
	}

	public void setxEnd(double xEnd) {
		double oldValue = this.xEnd;
		this.xEnd = xEnd;
		firePropertyChange("xEnd", oldValue, this.xEnd);
	}

	public void setWidth(double width) {
		double oldValue = this.width;
		this.width = width;
		firePropertyChange("width", oldValue, this.width);
	}

	public void setyStart(double yStart) {
		double oldValue = this.yStart;
		this.yStart = yStart;
		firePropertyChange("yStart", oldValue, this.yStart);
	}

	public void setyEnd(double yEnd) {
		double oldValue = this.yEnd;
		this.yEnd = yEnd;
		firePropertyChange("yEnd", oldValue, this.yEnd);
	}

	public void setHeight(double height) {
		double oldValue = this.height;
		this.height = height;
		firePropertyChange("height", oldValue, this.height);
	}
}