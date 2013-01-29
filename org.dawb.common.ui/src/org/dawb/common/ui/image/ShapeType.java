package org.dawb.common.ui.image;

public enum ShapeType {
	SQUARE, TRIANGLE, CIRCLE, NONE;
	
	public String getId() {
		return getClass().getName()+"."+name();
	}
}
