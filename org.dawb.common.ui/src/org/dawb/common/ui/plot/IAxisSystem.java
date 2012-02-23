package org.dawb.common.ui.plot;

public interface IAxisSystem {

	/**
	 * Use this method to create axes other than the default y and x axes.
	 * @param title
	 * @param isYAxis, normally it is.
	 * @param side - either SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM
	 * @return
	 */
	public IAxis createAxis(final String title, final boolean isYAxis, final int side);
	
	/**
	 * The current y axis to plot to. Intended for 1D plotting with multiple axes.
	 * @return
	 */
	public IAxis getSelectedYAxis();
	
	/**
	 * Set the current plotting yAxis. Intended for 1D plotting with multiple axes.
	 * May be called with null to reset to the primary axis.
	 * @param yAxis
	 */
	public void setSelectedYAxis(IAxis yAxis);
	
	/**
	 * The current x axis to plot to. Intended for 1D plotting with multiple axes.
	 * @return
	 */
	public IAxis getSelectedXAxis();
	
	/**
	 * Set the current plotting xAxis. Intended for 1D plotting with multiple axes.
	 * May be called with null to reset to the primary axis.
	 * 
	 * @param xAxis
	 */
	public void setSelectedXAxis(IAxis xAxis);
	

}
