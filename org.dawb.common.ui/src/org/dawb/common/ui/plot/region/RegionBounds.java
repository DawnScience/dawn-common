package org.dawb.common.ui.plot.region;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.dawb.common.util.text.NumberUtils;

/**
 * Class attempts to construct a bound in graph coordinates (not pixels) within
 * which the region should be drawn. This can them be used to get and
 * set the location and size of the region which will be drawn within
 * these bounds.
 * 
 * @author fcp94556
 *
 */
public class RegionBounds {
	
	/**
	 * The inner radius if this is circular bounds
	 */
	protected double inner = Double.NaN;
	
	/**
	 * The outer radius if this is circular bounds
	 */
	protected double outer = Double.NaN;


	/**
	 * The centre, which may be null.
	 */
	protected double[] center;

	/**
	 * The upper left if a box, the start point if a line.
	 */
	protected double[] p1;
	
	/**
	 * The lower right if a box, the end point if a line.
	 */
	protected double[] p2;

	private DecimalFormat format;
	
	public RegionBounds() {
		super();
	}

	/**
	 * Rectangular region
	 * @param p1
	 * @param p2
	 */
	public RegionBounds(double[] p1,  double[] p2) {
	    this.p1 = p1;
	    this.p2 = p2;
		this.format = new DecimalFormat("##0.00E0");
	}

	/**
	 * Cicular region
	 * 
	 * @param center2
	 * @param inRad
	 * @param outRad
	 */
	public RegionBounds(double[] center, double inRad, double outRad) {
		this.center = center;
		this.inner  = inRad;
		this.outer  = outRad;
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
		result = prime * result + Arrays.hashCode(center);
		long temp;
		temp = Double.doubleToLongBits(inner);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(outer);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (!Arrays.equals(center, other.center))
			return false;
		if (Double.doubleToLongBits(inner) != Double
				.doubleToLongBits(other.inner))
			return false;
		if (Double.doubleToLongBits(outer) != Double
				.doubleToLongBits(other.outer))
			return false;
		if (!Arrays.equals(p1, other.p1))
			return false;
		if (!Arrays.equals(p2, other.p2))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (p1==null || p2==null) return "Empty region";
		final StringBuilder buf = new StringBuilder();
		buf.append("(");
		buf.append(format.format(p1[0]));
		buf.append(", ");
		buf.append(format.format(p1[1]));
		buf.append(") to (");
		buf.append(format.format(p2[0]));
		buf.append(", ");
		buf.append(format.format(p2[1]));
		buf.append(")");
		return buf.toString();
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
	
	/**
	 * If a specific value for centre has not been set via setCentre(...), then
	 * a centre will be returned from p1 and p2.
	 * 
	 * If a centre value has been set, it is saved and even if this is
	 * not the real centre and even if p1 and p2 change!
	 * 
	 * @return
	 */
	public double[] getCenter() {
		if (center==null) {
			return new double[]{p2[0]-p1[0], p2[1]-p1[1]};
		}
		return center;
	}
	
	/**
	 * Use carefully, once set this is the centre even if p1 and/or p2 change (unless it is set to null).
	 * @param centre (can be null)
	 */
	public void setCenter(double[] centre) {
		this.center = centre;
	}

	public double getDx() {
		return p2[0]-p1[0];
	}

	public double getDy() {
		return p2[1]-p1[1];
	}

	public double getLength() {
		return Math.hypot(getDx(), getDy());
	}
	
	/**
	 * Gets the low y coordinate
	 * @return
	 */
	public double getY() {
		return Math.min(p1[1], p2[1]);
	}
	
	/**
	 * Gets the low x coordinate
	 * @return
	 */
	public double getX() {
		return Math.min(p1[0], p2[0]);
	}

	/**
	 * Gets the width as a scalar, i.e. no sign to indicate direction, value only.
	 * @return
	 */
	public double getWidth() {
		return Math.abs(getDx());
	}
	
	/**
	 * Gets the height as a scalar, i.e. no sign to indicate direction, value only.
	 * @return
	 */
	public double getHeight() {
		return Math.abs(getDy());
	}

	public boolean isRectange() {
		return p1!=null && p2!=null && Double.isNaN(inner) && Double.isNaN(outer);
	}
	
	public boolean isCircle() {
		return p1==null && p2==null && !Double.isNaN(inner) && !Double.isNaN(outer) && this.center!=null;
	}

	public double getInner() {
		return inner;
	}

	public void setInner(double inner) {
		this.inner = inner;
	}

	public double getOuter() {
		return outer;
	}

	public void setOuter(double outer) {
		this.outer = outer;
	}

	/**
	 * Returns a RegionBounds bounding the inner circle
	 * @return
	 */
	public RegionBounds getInnerRectangle() {
		if (!isCircle()) throw new RuntimeException("Can only calculate inner circle bounds if it is circle bounds!");
		final double diff = Math.hypot(inner, inner);
		return new RegionBounds(new double[]{center[0]-diff, center[1]-diff}, new double[]{center[0]+diff, center[1]+diff});
	}

	public RegionBounds getOuterRectangle() {
		if (!isCircle()) throw new RuntimeException("Can only calculate outer circle bounds if it is circle bounds!");
		final double diff = Math.hypot(outer, outer);
		return new RegionBounds(new double[]{center[0]-diff, center[1]-diff}, new double[]{center[0]+diff, center[1]+diff});
	}

}
