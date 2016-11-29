package raytracer;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }
    public static final Point3d p = new Point3d(0, 0, 0);
    
        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // Objective 4: finish this class
    	
    	// https://www.cs.princeton.edu/courses/archive/fall00/cs426/lectures/raycast/sld017.htm
    	Vector3d eyeVector = new Vector3d(ray.eyePoint.x, ray.eyePoint.y, ray.eyePoint.z);
    	double numerator = -eyeVector.dot(n);
    	double denominator = ray.viewDirection.dot(n);
    	
    	boolean isNumeratorZero = Math.abs(numerator) < EPSILON;
    	boolean isDenominatorZero = Math.abs(denominator) < EPSILON;
    	
    	Double t = null;
    	
    	// Infinitely many intersections
    	if(isDenominatorZero && isNumeratorZero)
    	{
    		t = 0.001;
    	}
    	else if(isDenominatorZero) { // Don't want to divide by 0
    		return;
    	}
    	else
    	{
    		t = numerator / denominator;
    	}
    	
    	// Set the result if we have one
    	if(t != null && t > 0 && t < result.t)
    	{
    		result.t = t;
    		result.n = (Vector3d) n.clone();
    		result.n.normalize();
    		result.p = new Point3d();
    		ray.getPoint(t, result.p);

    		// Determine which material to use
    		result.material = material;
    		if(material2 != null)
    		{
				int xQuadrant = (int) Math.ceil(result.p.x);
				int zQuadrant = (int) Math.floor(result.p.z);
				
				boolean bothEven = xQuadrant % 2 == 0 && zQuadrant % 2 == 0;
				boolean bothOdd =xQuadrant % 2 != 0 && zQuadrant % 2 != 0;
    			
				if( bothEven || bothOdd )
    			{
    				result.material = material2;
    			}
    		}
    	}
    }
    
}
