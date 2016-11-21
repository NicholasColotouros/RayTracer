package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Use this class to store the result of an intersection, or modify it to suit your needs!
 */
public class IntersectResult {
	
	/** The normal at the intersection */ 
	public Vector3d n = new Vector3d();
	
	/** Intersection position */
	public Point3d p = new Point3d();
	
	/** The material of the intersection */
	public Material material = null;
		
	/** Parameter on the ray giving the position of the intersection */
	public double t = Double.POSITIVE_INFINITY; 
	
	/**
	 * Default constructor.
	 */
	IntersectResult() {
		// do nothing
	}
	
	/**
	 * Copy constructor.
	 */
	IntersectResult( IntersectResult other ) {
		n.set( other.n );
		p.set( other.p );
		t = other.t;
		material = other.material;
	}
	
	/***
	 * Clears the intersection to make it as if it were newly constructed and reused.
	 */
	public void clear() {
		n = new Vector3d();
		p = new Point3d();
		material = null;
		t = Double.POSITIVE_INFINITY;
	}
	
	public void set(IntersectResult ir) {
		n = ir.n;
		p = ir.p;
		material = ir.material;
		t = ir.t;
	}
}
