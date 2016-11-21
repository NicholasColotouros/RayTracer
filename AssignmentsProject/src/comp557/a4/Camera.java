package comp557.a4;

import java.awt.Dimension;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple camera object, which could be extended to handle a variety of 
 * different camera settings (e.g., aperature size, lens, shutter)
 */
public class Camera {
	
	/** Camera name */
    public String name = "camera";

    /** The eye position */
    public Point3d from = new Point3d(0,0,10);
    
    /** The "look at" position */
    public Point3d to = new Point3d(0,0,0);
    
    /** Up direction, default is y up */
    public Vector3d up = new Vector3d(0,1,0);
    
    /** Vertical field of view (in degrees), default is 45 degrees */
    public double fovy = 45.0;
    
    /** The rendered image size */
    public Dimension imageSize = new Dimension(640,480);
    
    // 3 camera axes u, v, w
    public Vector3d u, v, w;
    
    
    // We make make an imaginary screen to know how we're generating pixels.
    // So we have a right, left, top, bottom
    public double l, r, t, b;
    
    /**
     * Default constructor
     */
    public Camera() {
    	// do nothing
    }
    
    // Initializes the axes of the camera
    public void initialize(){
		// TODO something is wrong in this or ray casting. Everything is upside down and boxes are skewed
    	// Can we actually take up for granted?
    	double aspectRatio = (double) imageSize.width / imageSize.height;
		double fovyRad = Math.toRadians(fovy);
	
		// Calculate our top/right/bottom/tom of our fake screen
		l = -aspectRatio * Math.tan(fovyRad/2.0);
		r = -l;
		t = Math.tan(fovyRad/2.0);
		b = -t;
		
		// Find our 3 axes
		// Cam is looking up, normalized
		v = (Vector3d) up.clone();
		v.normalize();
		
		// w is the direction we're looking away from, normalized
		w = new Vector3d();
		w.sub(from, to);
		w.normalize();
		
		// u is our third axis which is the cross between v and w
		u = new Vector3d();
		u.cross(v, w);
    }
}

