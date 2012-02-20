package org.dawb.common.ui.plot.region;

import java.util.Arrays;

import org.dawb.common.util.text.NumberUtils;

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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(p1);
		result = prime * result + Arrays.hashCode(p2);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegionBounds other = (RegionBounds) obj;
		if (!Arrays.equals(p1, other.p1))
			return false;
		if (!Arrays.equals(p2, other.p2))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "RegionBounds [p1=" + Arrays.toString(p1) + ", p2="
				+ Arrays.toString(p2) + "]";
	}

	public RegionBounds getDiff(final RegionBounds with) {
		return new RegionBounds(new double[]{p1[0]-with.p1[0], p1[1]-with.p1[1]}, 
				                new double[]{p2[0]-with.p2[0], p2[1]-with.p2[1]});
	}
	
	/**
	 * Equals within a tolerance for the rounding error
	 * @param r1
	 * @param i
	 * @param d
	 * @return
	 */
	public boolean equalsTolerance(RegionBounds with, double xTolerance, double yTolerance) {
		
		if (this == with) return true;
		if (with == null) return false;
	
		if (!NumberUtils.equalsTolerance(p1[0], with.p1[0], xTolerance)) return false;
		if (!NumberUtils.equalsTolerance(p2[0], with.p2[0], xTolerance)) return false;
		
		if (!NumberUtils.equalsTolerance(p1[1], with.p1[1], yTolerance)) return false;
		if (!NumberUtils.equalsTolerance(p2[1], with.p2[1], yTolerance)) return false;
		
		return true;
	}
}
