package comp557.a4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// Raycasters used for multithreading in the scene class
public class RaycasterThread implements Runnable {
	
	// References to the properties in the Scene class
	public List<Intersectable> surfaceList;
	public Map<String,Light> lights;
    public Render render;
    public Color3f ambient;
    public Camera camera;
    
    // Defines what pixels will be rendered by this particular ray
    public int starti, endi;
    public int startj, endj;
    
    private static final int MAX_REFLECTION_DEPTH = 3;
    private static final int MAX_OPACITY_DEPTH = 5;
    private static final double EPSILON = 0.001;
    
    // Antialiasing variables
    private int subPixelGridSize;
    private int leftoverRays;
    private double distanceBetweenGridPoints;
    private Random rng;
    
    public RaycasterThread ( List<Intersectable> s, Map<String, Light> l, Render r, Color3f a, Camera c, 
    		int si, int ei, int sj, int ej,
    		int spgs, int lor, double distbwpts) {
    	surfaceList = s;
    	lights = l;
    	render = r;
    	ambient = a;
    	camera = c;
    	
    	starti = si;
    	endi = ei;
    	startj = sj;
    	endj = ej;
    	
    	subPixelGridSize = spgs;
    	leftoverRays = lor;
    	distanceBetweenGridPoints = distbwpts;
    	
    	rng = new Random();
    }
	
	@Override
	public void run() {
		int argb;
        
        // Additional declarations needed for the loop
        final double[] offset = {0, 0};
        Ray ray = new Ray();
        IntersectResult result = new IntersectResult();
        ArrayList<Color4f> colours = new ArrayList<>();
        
        for ( int i = starti; i < endi && !render.isDone(); i++ ) {
            for ( int j = startj; j < endj && !render.isDone(); j++ ) {
        		// Supersampling offset
        		// - 0.5 because the generate ray method adds 0.5 to make the ray at the center of the pixel.

            	// For supersampling make a grid of the number of samples and distribute rays evenly on a grid within the pixel.
        		for(int x = 0; x < subPixelGridSize; x++) {
        			for(int y = 0; y < subPixelGridSize; y++) {
        				
    					offset[0] = distanceBetweenGridPoints * x - 0.5;
    					offset[1] = distanceBetweenGridPoints * y - 0.5;
        			
    					// If we have jittering, add in some random movement within at most the distance of a subpixel
    					if (render.useJittering) {
    						offset[0] += 2*distanceBetweenGridPoints * rng.nextDouble() - distanceBetweenGridPoints;
    						offset[1] += 2*distanceBetweenGridPoints * rng.nextDouble() - distanceBetweenGridPoints;
    					}
    					
        				colours.add(castRayAndGetColour(i, j, offset, camera, ray, result));
        			}
        		}
        		
        		// Since we can have a number of samples that are not always square numbers, cast the remaining
        		// rays randomly within the subpixel. Jitter doesn't matter in this case because it's already random
        		for(int k = 0; k < leftoverRays && render.samples > 1; k++) {
        			offset[0] = rng.nextDouble() - 0.5;
    				offset[1] = rng.nextDouble() - 0.5;
    				colours.add(castRayAndGetColour(i, j, offset, camera, ray, result));
        		}
        		
        		// AA is off (1 sample)
        		if (render.samples == 1) {
        			if ( render.useJittering) {
        				offset[0] += distanceBetweenGridPoints * rng.nextDouble();
						offset[1] += distanceBetweenGridPoints * rng.nextDouble();
        			}
        			colours.add(castRayAndGetColour(i, j, offset, camera, ray, result));
        		}
        		
        		// Now that we've cast all of our rays, set the pixel colour
            	if(colours.size() == 1)
            		argb = convertColorToARGB(colours.get(0));
            	else
            		argb = convertColorToARGB(averageColours(colours));
            	
            	render.setPixel(j, i, argb);
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
		avgColours.clamp(0f, 1f);
		
    	return avgColours;
    }
    
    public Color4f castRayAndGetColour(final int i, final int j, final double[] offset, final Camera cam, Ray ray, IntersectResult result) {
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
    		ray.viewDirection.normalize();
    		return new Color4f(
    							render.bgcolor.x * (float) Math.abs( ray.viewDirection.x), 
    							render.bgcolor.y * (float) Math.abs(ray.viewDirection.y), 
    							render.bgcolor.z * (float) Math.abs(ray.viewDirection.z), 
    							1f
    					);
    	}
    	
    	// Intersection
    	else { 
            return calculateColor(ray, result, 0, 0);
    	}
    }
    
    // Does the lighting computations
    public Color4f calculateColor(Ray ray, IntersectResult result, int reflectionDepth, int transparencyDepth) {
    	Material material = result.material;
    	result.n.normalize();
    	
    	Color3f reflectedAmbient = new Color3f(ambient);
    	Color3f reflectedSpecular = new Color3f();
    	Color3f reflectedDiffuse = new Color3f();
    	Color3f reflectedReflection = new Color3f();
    	Color3f colourFromTransparency = new Color3f();
    	
    	// Ambient lighting
    	reflectedAmbient.x *= material.diffuse.x; 
    	reflectedAmbient.y *= material.diffuse.y;
    	reflectedAmbient.z *= material.diffuse.z;
    	
    	Ray shadowRay = new Ray();
    	IntersectResult shadowResult = new IntersectResult();
    	
    	// Transparency, if applicable
    	if(result.material.opacity < 1 && transparencyDepth < MAX_OPACITY_DEPTH) {
    		Color3f clearColour = calculateTransparentColour(ray, result, shadowRay, shadowResult, reflectionDepth, transparencyDepth);
    		clearColour.clamp(0f, 1f);
    		colourFromTransparency.add(clearColour);
    	}
    	
    	// Reflections, if any
    	if ( result.material.isReflective && reflectionDepth < MAX_REFLECTION_DEPTH) {
			Color3f reflectedColour = calculateReflectiveColour(ray, result, shadowRay, shadowResult, reflectionDepth, transparencyDepth);
			reflectedColour.clamp(0f, 1f);
			reflectedReflection.add(reflectedColour);
		}
		
		
    	for(Light light : lights.values()) {

    		// Before proceding check to make sure light has an effect at all
    		shadowResult.clear();
    		if (inShadow(result, light, surfaceList, shadowResult, shadowRay))
    			continue;
    		
    		
    		Vector3d lightVector = new Vector3d();
    		lightVector.sub(light.from, result.p);
    		lightVector.normalize();
    		
    		// Diffuse lighting
    		Color3f diffuseColour = calculateDiffuseColour(light, ray, result, lightVector);
    		Color3f specularColour = calculateSpecularColour(light, ray, result, lightVector);

    		reflectedDiffuse.add(diffuseColour);
    		reflectedSpecular.add(specularColour);
    	}
    	
    	
    	reflectedAmbient.add(reflectedDiffuse);
    	reflectedAmbient.add(reflectedSpecular);
    	reflectedAmbient.add(reflectedReflection);
    	reflectedAmbient.clamp(0f, 1f);
    	reflectedAmbient.scale(material.opacity);
    	colourFromTransparency.scale((float) (1.0 - material.opacity));
    	reflectedAmbient.add(colourFromTransparency);
    	
    	
    	Color4f reflectedLight = new Color4f(reflectedAmbient.x, reflectedAmbient.y, reflectedAmbient.z, 1);
    	reflectedLight.clamp(0f, 1f);
		return reflectedLight;
	}
    
    private Color3f calculateTransparentColour(Ray ray, IntersectResult result, Ray shadowRay, IntersectResult shadowResult, int reflectionDepth, int transparencyDepth) {
    	
    	// Nudge the start point just onto the other side of the surface
    	shadowResult.clear();
    	Point3d nudgedStart = new Point3d(result.p);
    	Vector3d tinyNormal = new Vector3d(result.n);
		tinyNormal.scale(-EPSILON);
		
		// Cast the ray again
		shadowRay.eyePoint = nudgedStart;
		shadowRay.viewDirection = ray.viewDirection;
		shadowResult.clear();
    	for(Intersectable surface : surfaceList) {
			surface.intersect(shadowRay, shadowResult);
		}
    	
    	Color4f reflectedColour; 
		if(shadowResult.t < Double.POSITIVE_INFINITY)
			reflectedColour = calculateColor(shadowRay, shadowResult, reflectionDepth, transparencyDepth + 1);
		else
			reflectedColour = new Color4f(
					render.bgcolor.x * (float) Math.abs( ray.viewDirection.x), 
					render.bgcolor.y * (float) Math.abs(ray.viewDirection.y), 
					render.bgcolor.z * (float) Math.abs(ray.viewDirection.z), 
					1f
			);
		Color3f ret = new Color3f(reflectedColour.x, reflectedColour.y, reflectedColour.z);
		return ret;
    }
    
    private Color3f calculateReflectiveColour(Ray ray, IntersectResult result, Ray shadowRay, IntersectResult shadowResult, int reflectionDepth, int transparencyDepth) {
    	
    	// Calculate the new ray to cast for reflections
    	Vector3d reflectedDirection = new Vector3d(result.n);
    	reflectedDirection.scale(-2 * ray.viewDirection.dot(result.n));
    	reflectedDirection.add(ray.viewDirection, reflectedDirection);
    	reflectedDirection.normalize();
    	
    	Vector3d tinyNormal = new Vector3d(result.n);
		tinyNormal.scale(EPSILON);
		
		shadowRay.eyePoint = new Point3d(result.p);
		shadowRay.eyePoint.add(tinyNormal);
		shadowRay.viewDirection = reflectedDirection;
		
		shadowResult.clear();
    	for(Intersectable surface : surfaceList) {
			surface.intersect(shadowRay, shadowResult);
		}
		Color4f km = result.material.reflective;
		Color4f reflectedColour; 
		if(shadowResult.t < Double.POSITIVE_INFINITY)
			reflectedColour = calculateColor(shadowRay, shadowResult, reflectionDepth + 1, transparencyDepth);
		else
			reflectedColour = new Color4f(
					render.bgcolor.x * (float) Math.abs( ray.viewDirection.x), 
					render.bgcolor.y * (float) Math.abs(ray.viewDirection.y), 
					render.bgcolor.z * (float) Math.abs(ray.viewDirection.z), 
					1f
			);
		Color3f ret = new Color3f(
				reflectedColour.x * km.x, 
				reflectedColour.y * km.y, 
				reflectedColour.z * km.z
			);
		return ret;
    }
    
    private Color3f calculateSpecularColour(Light light, Ray ray, IntersectResult result, Vector3d lightVector){
    	Material material = result.material;
    	Vector3d cameraVector = new Vector3d();
		cameraVector.sub(ray.eyePoint, result.p);
		cameraVector.normalize();
		
		Vector3d h = new Vector3d(lightVector);
		h.add(cameraVector);
		h.normalize();
		
		double cosFactor = Math.max(0, result.n.dot(h));
		Color3f specularColour = new Color3f();
		
		if (cosFactor > 0) {
			float specularScalar = (float) Math.pow( cosFactor, material.shinyness);
			specularColour = new Color3f(
										material.specular.x * specularScalar * light.color.x,
										material.specular.y * specularScalar * light.color.y,
										material.specular.z * specularScalar * light.color.z
									);
			specularColour.clamp(0f, 1f);
			
		}
		return specularColour;
    }
    
    private Color3f calculateDiffuseColour(Light light, Ray ray, IntersectResult result, Vector3d lightVector){
    	Material material = result.material;
    	float diffuseScalar = (float) (light.power * Math.max(0.0, result.n.dot(lightVector)));
		Color3f diffuseColour = new Color3f(
									material.diffuse.x * diffuseScalar * light.color.x,
									material.diffuse.y * diffuseScalar * light.color.y,
									material.diffuse.z * diffuseScalar * light.color.z
								);
		
		diffuseColour.clamp(0f, 1f);
		return diffuseColour;
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
		Vector3d tinyNormal = new Vector3d(result.n);
		tinyNormal.scale(EPSILON);
		
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
