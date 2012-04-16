package org.dawb.common.ui.plot.region;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.util.text.NumberUtils;

/**
 * Class attempts to construct a bound in graph coordinates (not pixels) within
 * which the region should be drawn. This can then be used to get and
 * set the location and size of the region which will be drawn within
 * these bounds.
 * 
 * TODO This class could split into 3 - rectangle, ring, and points with an
 * abstract super class.
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
	protected double[] centre;

	/**
	 * The upper left if a box, the start point if a line.
	 */
	protected double[] p1;
	
	/**
	 * The lower right if a box, the end point if a line.
	 */
	protected double[] p2;
	
	/**
	 * The points list if this is a points region.
	 */
	protected Collection<double[]> points;

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
	 * Circular region
	 * 
	 * @param centre
	 * @param inRad
	 * @param outRad
	 */
	public RegionBounds(double[] centre, double inRad, double outRad) {
		this.centre = centre;
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
		result = prime * result + Arrays.hashCode(centre);
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
		if (!Arrays.equals(centre, other.centre))
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
	public double[] getCentre() {
		if (centre==null) {
			return new double[]{p2[0]-p1[0], p2[1]-p1[1]};
		}
		return centre;
	}
	
	/**
	 * Use carefully, once set this is the centre even if p1 and/or p2 change (unless it is set to null).
	 * @param centre (can be null)
	 */
	public void setCentre(double[] centre) {
		this.centre = centre;
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
		if (points!=null) return false;
		return p1!=null && p2!=null && Double.isNaN(inner) && Double.isNaN(outer);
	}
	
	public boolean isCircle() {
		if (points!=null) return false;
		return p1==null && p2==null && !Double.isNaN(inner) && !Double.isNaN(outer) && this.centre!=null;
	}
	
	public boolean isPoints() {
		return points!=null;
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
		return new RegionBounds(new double[]{centre[0]-diff, centre[1]-diff}, new double[]{centre[0]+diff, centre[1]+diff});
	}

	public RegionBounds getOuterRectangle() {
		if (!isCircle()) throw new RuntimeException("Can only calculate outer circle bounds if it is circle bounds!");
		final double diff = Math.hypot(outer, outer);
		return new RegionBounds(new double[]{centre[0]-diff, centre[1]-diff}, new double[]{centre[0]+diff, centre[1]+diff});
	}

	public Collection<double[]> getPoints() {
		return points;
	}

	public void setPoints(Collection<double[]> points) {
		this.points = points;
	}
	
	/**
	 * As soon as you add a point, this RegionBounds becomes a points list region bounds.
	 * @param pnt
	 */
	public void addPoint(final double[] pnt) {
		if (points==null) points = new ArrayList<double[]>(89);
		points.add(pnt);
	}

}
