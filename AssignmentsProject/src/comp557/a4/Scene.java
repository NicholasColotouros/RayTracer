package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;

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

    private ReentrantLock nextLineLock;
    private int nextLine;
    
    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    	nextLineLock = new ReentrantLock();
    }
    
    public int getNextLineToRender() {
    	nextLineLock.lock();
    	int intToReturn = nextLine;
    	nextLine--;
    	nextLineLock.unlock();
    	return intToReturn;
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
    	Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        cam.initialize();
        render.init(w, h, showPanel);
        nextLine = h-1;
        int numThreads = render.numThreads;
        
        // Threads are subdivided by column because costly operations tend to be
        // in the center of the image instead of at the top. This way the harder bits
        // are kind of distributed among threads.
        
        // Calculate AA values
        int numSamples = render.samples;
        int subPixelGridSize = (int) Math.sqrt(numSamples);
        int leftoverRays = (int) (numSamples - Math.pow(subPixelGridSize, 2));
        double distanceBetweenGidPoints = 0;
        
        // Makes things easier when subpixel grid is 1x1
        if (subPixelGridSize == 1) {
        	leftoverRays = render.samples;
        	subPixelGridSize = 0;
        }
        else { 
        	distanceBetweenGidPoints = 1.0 / (subPixelGridSize - 1);
        }
        
        Thread[] threads = new Thread[numThreads];
        for(int t = 0; t < numThreads; t++) {
        	RaycasterThread rct = new RaycasterThread(surfaceList, lights, render, ambient, cam, 
        			w, this, subPixelGridSize, leftoverRays, distanceBetweenGidPoints);
        	Thread thread = new Thread(rct);
        	threads[t] = thread;
        	thread.start();
        }
        
        for(Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
    }
    
}
