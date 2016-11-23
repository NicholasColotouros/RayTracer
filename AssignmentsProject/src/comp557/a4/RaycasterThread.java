package comp557.a4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// Raycasters used for multithreading in the scene class
public class RaycasterThread implements Runnable {
	private static ReentrantLock setPixelLock = new ReentrantLock();
	
	// References to the properties in the Scene class
	public List<Intersectable> surfaceList;
	public Map<String,Light> lights;
    public Render render;
    public Color3f ambient;
    public Camera camera;
    
    // Defines what pixels will be rendered by this particular ray
    public int starti, endi;
    public int startj, endj;
    
    public RaycasterThread ( List<Intersectable> s, Map<String, Light> l, Render r, Color3f a, Camera c, 
    		int si, int ei, int sj, int ej) {
    	surfaceList = s;
    	lights = l;
    	render = r;
    	ambient = a;
    	camera = c;
    	
    	starti = si;
    	endi = ei;
    	startj = sj;
    	endj = ej;
    }
	
	@Override
	public void run() {
		int argb;
        
        // Additional declarations needed for the loop
        final double[] offset = {0, 0};
        Ray ray = new Ray();
        IntersectResult result = new IntersectResult();
        ArrayList<Color4f> colours = new ArrayList<>();
        Random rng = new Random();
        int numSamples = ((render.samples < 1) ? 1 : render.samples);
        
        
        for ( int i = starti; i < endi && !render.isDone(); i++ ) {
            for ( int j = startj; j < endj && !render.isDone(); j++ ) {
            	for ( int k = 0; k < numSamples; k++) {
            		// TODO make the supersampling grid, apply jittering
            		// If we're using supersampling, apply the offset
            		if (numSamples > 1) {
            			offset[0] = rng.nextDouble() - 0.5;
            			offset[1] = rng.nextDouble() - 0.5;
            		}
            		
	                // Objective 1: generate a ray (use the generateRay method)
	            	generateRay(j, i, offset, camera, ray);
	            	
	                // Objective 2: test for intersection with scene surfaces
	            	result.clear();
	            	for(Intersectable intersectable : surfaceList) {
	            		intersectable.intersect(ray, result);
	            	}
	            	
	                
	            	// Get the color for the ray and update the render image
	            	
	            	// No intersection
	            	if(result.t == Double.POSITIVE_INFINITY) {
	            		Color4f bgColor = new Color4f(render.bgcolor.x, render.bgcolor.y, render.bgcolor.z, 1f);
	            		colours.add(bgColor);
	            	}
	            	
	            	// Intersection
	            	else { 
	                    colours.add(calculateColor(ray, result));
	            	}
	            }
            	
            	if(colours.size() == 1)
            		argb = convertColorToARGB(colours.get(0));
            	else
            		argb = convertColorToARGB(averageColours(colours));
            	
            	// TODO figure out if this is thread safe and remove
            	setPixelLock.lock();
            	render.setPixel(j, i, argb);
            	setPixelLock.unlock();
            	colours.clear();
	        }
        }        
	}
	
    public Color4f averageColours (List<Color4f> colours) {
    	Color4f avgColours = new Color4f();
    	for( Color4f c : colours ) {
    		avgColours.x += c.x;
    		avgColours.y += c.y;
    		avgColours.z += c.z;
    		avgColours.w += c.w;
    	}
    	
    	float numColours = colours.size();
    	avgColours.x /= numColours;
		avgColours.y /= numColours;
		avgColours.z /= numColours;
		avgColours.w /= numColours;
    	
    	return avgColours;
    }
    
    // Does the lighting computations
    public Color4f calculateColor(Ray ray, IntersectResult result) {
    	Material material = result.material;
    	Vector3d normalizedMaterialNormal = new Vector3d(result.n);
    	normalizedMaterialNormal.normalize();
        		
    	Color3f reflectedAmbient = new Color3f(ambient);
    	Color3f reflectedSpecular = new Color3f();
    	Color3f reflectedDiffuse = new Color3f();
    	
    	// Ambient lighting
    	reflectedAmbient.x *= material.diffuse.x; 
    	reflectedAmbient.y *= material.diffuse.y;
    	reflectedAmbient.z *= material.diffuse.z;
    	
    	
    	
    	Ray shadowRay = new Ray();
    	IntersectResult shadowResult = new IntersectResult();
    	for(Light light : lights.values()) {
    		
    		// Before proceding check to make sure light has an effect at all
    		shadowResult.clear();
    		if (inShadow(result, light, surfaceList, shadowResult, shadowRay))
    			continue;
    		
    		Vector3d lightVector = new Vector3d();
    		lightVector.sub(light.from, result.p);
    		lightVector.normalize();
    		
    		Vector3d cameraVector = new Vector3d();
    		cameraVector.sub(ray.eyePoint, result.p);
    		cameraVector.normalize();
    		
    		
    		// Diffuse lighting
    		float diffuseScalar = (float) (light.power * Math.max(0.0, normalizedMaterialNormal.dot(lightVector)));
    		Color3f diffuseColour = new Color3f(
    									material.diffuse.x * diffuseScalar * light.color.x,
    									material.diffuse.y * diffuseScalar * light.color.y,
    									material.diffuse.z * diffuseScalar * light.color.z
    								);
    		reflectedDiffuse.add(diffuseColour);
    		
    		// Specular lighting
    		Vector3d h = new Vector3d(lightVector);
    		h.add(cameraVector);
    		h.normalize();
    		
    		float specularScalar = (float) Math.pow( Math.max(0.0, normalizedMaterialNormal.dot(h) ), material.shinyness);
    		specularScalar *= light.power;
    		Color3f specularColour = new Color3f(
    									material.specular.x * specularScalar * light.color.x,
    									material.specular.y * specularScalar * light.color.y,
    									material.specular.z * specularScalar * light.color.z
    								);
    		reflectedSpecular.add(specularColour);
    	}
    	
    	
    	reflectedAmbient.add(reflectedDiffuse);
    	reflectedAmbient.add(reflectedSpecular);

    	Color4f reflectedLight = new Color4f(reflectedAmbient.x, reflectedAmbient.y, reflectedAmbient.z, 1);
    	reflectedLight.clamp(0f, 1f);
		return reflectedLight;
	}

	public static int convertColorToARGB(Color4f c){
    	int r = (int)(255*c.x);
        int g = (int)(255*c.y);
        int b = (int)(255*c.z);
        int a = 255;
        return (a<<24 | r<<16 | g<<8 | b);
    }
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		// Objective 1: finish this method.  
		// Formula from slides that map coordinates of image to pixel
		double u = cam.l + ( cam.r-cam.l ) * ( ( i + 0.5) / cam.imageSize.width );
		double v = (cam.b + ( cam.t-cam.b ) * ( ( j + 0.5 + offset[1] ) / cam.imageSize.height ));
		
		Vector3d vFrame = new Vector3d(cam.v);
		vFrame.scale(v);
		
		// scale by -1 because w is pointing away from the ray we're casting
		Vector3d wFrame = new Vector3d(cam.w);
		wFrame.scale(-1);
		
		Vector3d uFrame = new Vector3d(cam.u);
		uFrame.scale(u);
		
		ray.eyePoint = (Point3d) cam.from.clone();
		Point3d s = new Point3d();
		s.add(ray.eyePoint);
		s.add(uFrame);
		s.add(vFrame);
		s.add(wFrame);
		
	
		Vector3d dir = new Vector3d();
		dir.sub(s, ray.eyePoint); // Sum of uvw frames
		dir.normalize();
		ray.viewDirection = dir;
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final List<Intersectable> surfaces, IntersectResult shadowResult, Ray shadowRay) {
		// Objective 5: finish this method and use it in your lighting computation
		
		// Cast a ray from the point of intersection to the light and see if it hits anything
		result.n.normalize();
		Vector3d tinyNormal = new Vector3d(result.n);
		tinyNormal.scale(0.001);
		
		// Move the start by a tiny bit along the normal so it doesn't self shadow
		shadowRay.eyePoint = new Point3d(result.p);
		shadowRay.eyePoint.add(tinyNormal);
		
		Vector3d shadowDirection = new Vector3d();
		shadowDirection.sub(light.from, shadowRay.eyePoint);
		shadowRay.viewDirection = shadowDirection;
		
		for(Intersectable surface : surfaces) {
			surface.intersect(shadowRay, shadowResult);
		}
		
		// t = 1 means we hit something between the collision point between
		// the light from and the eye point exclusively
		return shadowResult.t > 0.0 && shadowResult.t < 1.0;
	}    

}
