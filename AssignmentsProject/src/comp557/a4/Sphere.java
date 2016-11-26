package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    	// e - c
    	Vector3d eyeVector = new Vector3d();
    	eyeVector.sub(ray.eyePoint, center);
    	
    	double a = ray.viewDirection.dot(ray.viewDirection);
    	double b = ray.viewDirection.dot(eyeVector);
    	double c = eyeVector.dot(eyeVector) - Math.pow(radius, 2);
    	double ac = a*c;
    	double discriminant = Math.pow(2*b,2) - 4*ac;
    	
    	//Positive therefore two intersections
    	Double t = null;
    	if (discriminant > 0)
    	{
    		double delta = Math.sqrt(Math.pow(b, 2) - ac); 
    		double t1 = (-b + delta) / a;
    		double t2 = ( -b - delta ) / a;
    		if ( t1 > 0 && t2 > 0 )
    		{
    			t = Math.min(t1, t2);
    		} else if ( t2 > 0 )
    		{
    			t = t2;
    		}
    	}
    	//Zero therefore one intersection
    	else if (-EPSILON < discriminant && discriminant < EPSILON)
    	{
    		t = -b/a;
    		
    	}
    	
    	if(t != null && t > 0 && t < result.t)
    	{
    		result.t = t;
    		result.p = new Point3d();
    		ray.getPoint(result.t, result.p);
    		Vector3d n = new Vector3d();
    		n.sub(result.p, center);
    		n.scale(1.0 / radius);
    		result.n = n;
    		result.material = material;
    	}
    }
}
