package org.dawb.common.ui.plot.region;

/**
 * Class attempts to contruct a bound in graph coordinates within
 * which the region should be drawn. This can them be used to get and
 * set the location and size of the region which will be drawn within
 * these bounds.
 * 
 * @author fcp94556
 *
 */
public class RegionBounds {

	/**
	 * The upper left if a box, the start point if a line.
	 */
	protected double[] p1;
	
	/**
	 * The lower right if a box, the end point if a line.
	 */
	protected double[] p2;
	
	public RegionBounds() {
		super();
	}
	public RegionBounds( double[] p1,  double[] p2) {
	    this.p1 = p1;
	    this.p2 = p2;
	}
	public double[] getP1() {
		return p1;
	}
	public void setP1(double[] p1) {
		this.p1 = p1;
	}
	public double[] getP2() {
		return p2;
	}
	public void setP2(double[] p2) {
		this.p2 = p2;
	}
}
