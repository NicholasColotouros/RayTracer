package raytracer;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	
    
    public Box(Point3d pmin, Point3d pmax) {
    	min = pmin;
    	max = pmax;
    }

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// Based on the algorithm provided by 
		// http://www.cs.utah.edu/~awilliam/box/box.pdf
		double tmin, tmax, txmin, txmax, tymin, tymax, tzmin, tzmax;
		
		// Calculate the x intersections
		if (ray.viewDirection.x >= 0) {
			txmin = (min.x - ray.eyePoint.x) / ray.viewDirection.x;
			txmax = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
		}
		else {
			txmin = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
			txmax = (min.x - ray.eyePoint.x) / ray.viewDirection.x;
		}
		
		// Calculate y intersections
		if (ray.viewDirection.y >= 0) {
			tymin = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
			tymax = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
		}
		else {
			tymin = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
			tymax = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
		}
		
		// Now we can infer if the ray intersects the box.
		// If it doesn't we don't need to compute z
		if ( txmin > tymax || tymin > txmax )
			return;
		
		// Calculate z intersections
		if (ray.viewDirection.z >= 0) {
			tzmin = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
			tzmax = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
		}
		else {
			tzmin = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
			tzmax = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
		}

		// Update tmax based on what has been computed so far
		tmin = Math.max(txmin, tymin);
		tmax = Math.min(txmax, tymax);
		
		// Do not proceed if the ray does not intersect
		if ( (tmin > tzmax) || (tzmin > tmax) )
			return;
		
		// If we have a valid intersection compute the normal and update the result
		double t = Math.max(tmin, tzmin);
		if(t > 0 && t < result.t) {
			// Start by computing the normal
			// This is done by figuring out which slab we're on
			// and setting one of the x,y,z values to 1 or -1 
			Point3d p = new Point3d();
			ray.getPoint(t, p);
			
			Vector3d n = new Vector3d();
			if (Math.abs(p.x - max.x) < EPSILON) {
				n.x = 1;
			} 
			else if (Math.abs(p.x - min.x) < EPSILON) {
				n.x = -1;
			} 
			else if (Math.abs(p.y - max.y) < EPSILON) {
				n.y = 1;
			} 
			else if (Math.abs(p.y - min.y) < EPSILON) {
				n.y = -1;
			} 
			else if (Math.abs(p.z - max.z) < EPSILON) {
				n.z = 1;
			}
			else if (Math.abs(p.z - min.z) < EPSILON) {
				n.z = -1;
			} 

			// Set the result
			result.t = t;
			result.p = p;
			result.t = t;
			result.n = n;
			result.material = material;
		}
	}	

}
