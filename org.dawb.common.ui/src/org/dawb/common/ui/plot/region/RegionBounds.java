package org.dawb.common.ui.plot.region;

import java.text.DecimalFormat;
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
	 * The center, which may be null.
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

	private DecimalFormat format;
	
	public RegionBounds() {
		super();
	}
	public RegionBounds( double[] p1,  double[] p2) {
	    this.p1 = p1;
	    this.p2 = p2;
		this.format = new DecimalFormat("##0.#####E0");
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
	 * If a specific value for center has not been set via setCenter(...), then
	 * a center will be returned from p1 and p2.
	 * 
	 * If a center value has been set, it is saved and even if this is
	 * not the real center and even if p1 and p2 change!
	 * 
	 * @return
	 */
	public double[] getCentre() {
		if (centre==null) {
			return new double[]{getP2()[0]-getP1()[0], getP2()[1]-getP1()[1]};
		}
		return centre;
	}
	
	/**
	 * Use carefully, once set this is the centre even if p1 and/or p2 change.
	 * @param center
	 */
	public void setCentre(double[] center) {
		this.centre = center;
	}
}
