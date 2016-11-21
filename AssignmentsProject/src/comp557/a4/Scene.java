package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();

    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
    	
    	// Provided setup
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        cam.initialize();
        
        render.init(w, h, showPanel);
        int argb;
        
        // Additional declarations needed for the loop
        final double[] offset = {0, 0};
        Ray ray = new Ray();
        IntersectResult result = new IntersectResult();
        ArrayList<Color4f> colours = new ArrayList<>();
        
        for ( int i = 0; i < h && !render.isDone(); i++ ) {
            for ( int j = 0; j < w && !render.isDone(); j++ ) {
            	
                // Objective 1: generate a ray (use the generateRay method)
            	generateRay(j, i, offset, cam, ray);
            	
                // Objective 2: test for intersection with scene surfaces
            	// TODO make the surfaceList a scenenode so i don't need this
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
            	
            	argb = convertColorToARGB(colours.get(0));
            	render.setPixel(j, i, argb);
            	colours.clear();
            }
        }
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
    }
    
    // Does the lighting computations
    public Color4f calculateColor(Ray ray, IntersectResult result) {
    	Material material = result.material;
    	Vector3d normalizedMaterialNormal = new Vector3d(result.n);
    	normalizedMaterialNormal.normalize();
        		
    	// TODO Ambient lighting this is Ia where is Ka?
    	Color3f reflectedAmbient = new Color3f(ambient);
    	
    	Color3f reflectedSpecular = new Color3f();
    	Color3f reflectedDiffuse = new Color3f();
    	for(Light light : lights.values()) {
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
		// TODO there is either a bug in this or in the camera code. Something is making the y inverted
		// Objective 1: finish this method.  
		// Formula from slides that map coordinates of image to pixel
		double u = cam.l + ( cam.r-cam.l ) * ( ( i + 0.5 + offset[0] ) / cam.imageSize.width );
		double v = cam.b + ( cam.t-cam.b ) * ( ( j + 0.5 + offset[1] ) / cam.imageSize.height );
		
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
	public static boolean inShadow(final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: finish this method and use it in your lighting computation
		
		return false;
	}    
}
