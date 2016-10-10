package comp557.a2;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.TrackBallCamera;

import com.jogamp.opengl.util.gl2.GLUT;

import comp557.a2.helperObjects.*;

/**
 * Assignment 2 - depth of field blur, and anaglyphys
 * 
 * For additional information, see the following paper, which covers
 * more on quality rendering, but does not cover anaglyphs.
 * 
 * The Accumulation Buffer: Hardware Support for High-Quality Rendering
 * Paul Haeberli and Kurt Akeley
 * SIGGRAPH 1990
 * 
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
 * GPU Gems [2007] has a slightly more recent survey of techniques.
 *
 * http://www.cs.duke.edu/courses/fall01/cps124/resources/p309-haeberli.pdf
 * @author Nicholas Colotouros, 260531370
 */
public class A2App extends A2Base {

	private String name = "Comp 557 Assignment 2 - Nicholas Colotouros 260531370";
	
    /** Viewing mode as specified in the assignment */
    int viewingMode = 1;
    
    /**
     * Note that limits are set to try to prevents you from choosing bad values (e.g., a near plane behind the eye,
     * or near plane beyond far plane).  But feel free to change the limits and experiment.
     * Also note that these quantities give the z coordinate in the world coordinate system!
     * */
    
    // centerEyeOnZAxis is used to demonstrate the center eye frustum that does not account for x or y displacement
    // having it unchecked will have the center eye frustum account for the x,y displacement
    private BooleanParameter centerEyeOnZAxis = new BooleanParameter( "Objective 1-3: z-axis center eye", false ); 
    private DoubleParameter eyeZPosition = new DoubleParameter( "eye z position in world", 0.5, 0.25, 3 ); 
    private DoubleParameter near = new DoubleParameter( "near z position in world", 0.25, -0.2, 0.5 ); 
    private DoubleParameter far  = new DoubleParameter( "far z position in world", -0.5, -2, -0.25 ); 
    private DoubleParameter focalPlaneZPosition = new DoubleParameter( "focal z position in world", 0, -1.5, 0.4 );     
    
    /** samples and aperture size are for drawing depth of field blur */    
    private IntParameter samples = new IntParameter( "samples", 2, 1, 100 );   
    
    /** In the human eye, pupil diameter ranges between approximately 2 and 8 mm */
    private DoubleParameter aperture = new DoubleParameter( "aperture size", 0.003, 0, 0.01 );
    
    /** x eye offsets for testing (see objective 4) */         
    private DoubleParameter eyeXOffset = new DoubleParameter("eye offset in x", 0.0, -0.3, 0.3);
    /** y eye offsets for testing (see objective 4) */
    private DoubleParameter eyeYOffset = new DoubleParameter("eye offset in y", 0.0, -0.3, 0.3);
    
    /** controls for drawing frustums, or not, as drawing lots of frustums will be confusing!! */
    private BooleanParameter drawCenterEyeFrustum = new BooleanParameter( "draw center eye frustum", true );    
    private BooleanParameter drawEyeFrustums = new BooleanParameter( "draw left and right eye frustums", true );
    
	/**
	 * The eye disparity should be constant, but can be adjusted to test the
	 * creation of left and right eye frustums or likewise, can be adjusted for
	 * your own eyes!! Note that 63 mm is a good inter occular distance for the
	 * average human, but you may likewise want to lower this to reduce the
	 * depth effect (images may be hard to fuse with cheap 3D colour filter
	 * glasses). Setting the disparity negative should help you check if you
	 * have your left and right eyes reversed!
	 */
    private DoubleParameter eyeDisparity = new DoubleParameter("eye disparity", 0.063, -0.1, 0.1 );

    private GLUT glut = new GLUT();
    
    private Scene scene = new Scene();

    /**
     * Launches the application
     * @param args
     */
    public static void main(String[] args) {
        new A2App();
    }
    
    /** Main trackball for viewing the world and the two eye frustums */
    private TrackBallCamera tbc = new TrackBallCamera();
    /** Second trackball for rotating the scene */
    private TrackBallCamera tbc2 = new TrackBallCamera();
    
    /**
     * Creates the application
     */
    public A2App() {      
    	
    	// initialise A2Base
    	super();
    	
    	// add tabs to the control frame
        controlFrame.add("Camera", tbc.getControls());
        controlFrame.add("Scene TrackBall", tbc2.getControls());
        controlFrame.add("Scene", getControls());
    
        tbc.attach( glCanvas );
        tbc2.attach( glCanvas );
        // initially disable second trackball, and improve default parameters given our intended use
        tbc2.enable(false);
        tbc2.setFocalDistance( 0 );
        tbc2.panRate.setValue(5e-5);
        tbc2.advanceRate.setValue(0.005);
        this.attach( glCanvas );
        
        mainFrame.setTitle(name);
        
    	// add code here if additional initialisation is needed
        
        // start the app
        start();
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// nothing to do
    }
        
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // do nothing
    }
    
    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
                    viewingMode = e.getKeyCode() - KeyEvent.VK_1 + 1;
                } else if ( e.getKeyCode() == KeyEvent.VK_0 ) {
                	viewingMode = 10;
                } else if ( e.getKeyCode() == KeyEvent.VK_MINUS ) {
                	viewingMode = 11;
                }
                if ( viewingMode == 1 ) {
                	tbc.enable(true);
                	tbc2.enable(false);
	            } else {
                	tbc.enable(false);
                	tbc2.enable(true);
	            }
                System.out.println("viewing mode " + viewingMode );
            }
        });
    }
    
    /**
     * @return a control panel
     */
    public JPanel getControls() {     
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add(centerEyeOnZAxis.getControls());
        vfp.add( eyeZPosition.getSliderControls(false));        
        vfp.add ( drawCenterEyeFrustum.getControls() );
        vfp.add( near.getSliderControls(false));
        vfp.add( far.getSliderControls(false));        
        vfp.add( focalPlaneZPosition.getSliderControls(false));        
        vfp.add( eyeXOffset.getSliderControls(false ) );
        vfp.add( eyeYOffset.getSliderControls(false ) );        
        vfp.add ( aperture.getSliderControls(false) );
        vfp.add ( samples.getSliderControls() );        
        vfp.add( eyeDisparity.getSliderControls(false) );        
        vfp.add ( drawEyeFrustums.getControls() );        
        VerticalFlowPanel vfp2 = new VerticalFlowPanel();
        vfp2.setBorder( new TitledBorder("Scene size and position" ));
        vfp2.add( scene.getControls() );
        vfp.add( vfp2.getPanel() );        
        return vfp.getPanel();
    }
             
    @Override
    public void init( GLAutoDrawable drawable ) {    	
    	// init gl drawable with default configuration
    	super.init(drawable);
    	
    	// add code here if additional gl drawable customization is needed
    }
 
    double w;
    double h;

	// Objective 1 - adjust for your screen resolution and dimension to something reasonable.
	double screenWidthPixels = 1920;
	double screenWidthMeters = 0.3453384; // 15.6 inch diagonal on a 16:9 screen comes out to 13.596 inches.
	double metersPerPixel = screenWidthMeters / screenWidthPixels;
    
    @Override
    public void display(GLAutoDrawable drawable) {        
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);            
        w = drawable.getSurfaceWidth() * metersPerPixel;
        h = drawable.getSurfaceHeight() * metersPerPixel;

        // Do some calculations that are used in multiple views
        double eyeRadius = 0.0125;
        Rectangle screenRectangle = new Rectangle(w, h, 0);
        
        // Create the center eye and its frustum
        Eye centerEye = new Eye(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue(), eyeRadius);
        centerEye.colour = new float[]{1,0,1};

        // The checkbox for centering the eye on the z-axis is for showing objectives 1-3 which assumes 
        // the center eye is on the z-axis.
        Frustum centerEyeFrustum;
        if(centerEyeOnZAxis.getValue()){
        	centerEyeFrustum = new Frustum(eyeZPosition.getValue(), near.getValue(), far.getValue(), 
        									h/2, w/2, focalPlaneZPosition.getValue());
        }
        else{
            centerEyeFrustum = new Frustum(centerEye, near.getValue(), far.getValue(), 
            								h/2, w/2, focalPlaneZPosition.getValue());        	
        }
        
        centerEyeFrustum.colour = centerEye.colour;
        
        // Set up the left and right eyes
        double eyeDist = eyeDisparity.getValue()/2; // Half will be applies to the left eye and half to the right
        Eye leftEye = new Eye(eyeXOffset.getValue() - eyeDist, eyeYOffset.getValue(), eyeZPosition.getValue(), eyeRadius);
        Eye rightEye = new Eye(eyeXOffset.getValue() + eyeDist, eyeYOffset.getValue(), eyeZPosition.getValue(), eyeRadius);
        
        leftEye.colour = new float[]{0,0,1}; // using the same colours as the 3D glasses
        rightEye.colour = new float[]{1,0,0};
        
        // Set up the frustums for the left and right eyes
        Frustum leftEyeFrustum = new Frustum(leftEye, near.getValue(), far.getValue(), 
				h/2, w/2, focalPlaneZPosition.getValue());
        Frustum rightEyeFrustum = new Frustum(rightEye, near.getValue(), far.getValue(), 
				h/2, w/2, focalPlaneZPosition.getValue());

        if ( viewingMode == 1 ) {
        	// We will use a trackball camera
            tbc.prepareForDisplay(drawable);
            // apply an arbitrary scale in this mode only just to make the 
            // scene and frustums small enough to be easily seen with the 
            // default track ball camera settings.
            gl.glScaled(15,15,15); 
            
            gl.glPushMatrix();
            tbc2.applyViewTransformation(drawable); // only the view transformation
            scene.display( drawable );
            gl.glPopMatrix();
            
            // Objective 1: draw screen rectangle and center eye
            screenRectangle.display(drawable, glut);
            
            
            // Objective 2 - draw camera frustum if drawCenterEyeFrustum is true
            if(drawCenterEyeFrustum.getValue()){
            	centerEyeFrustum.display(drawable, glut);	
            }
            // Objective 6 - draw left and right eye frustums if drawEyeFrustums is true
            if(drawEyeFrustums.getValue()){
            	leftEyeFrustum.display(drawable, glut);
            	rightEyeFrustum.display(drawable, glut);
            }

        }  if ( viewingMode == 2 ) {
        	// Objective 2 - draw the center eye camera view
        	gl.glPushMatrix();
        	centerEyeFrustum.lookThroughFrustum(drawable, glut, scene);
            gl.glPopMatrix();
        	
        } else if ( viewingMode == 3 ) {       
        	// Objective 5 - draw center eye with depth of field blur 
        	applyMotionBlurAndDraw(centerEyeFrustum, drawable, gl);
        	
        	// TODO remove: used for debugging/trials
        	gl.glPushMatrix();
        	leftEyeFrustum.lookThroughFrustum(drawable, glut, scene);
            gl.glPopMatrix();
            gl.glAccum(GL2.GL_LOAD, 0.5f);
            
            gl.glPushMatrix();
        	rightEyeFrustum.lookThroughFrustum(drawable, glut, scene);
            gl.glPopMatrix();
            gl.glAccum(GL2.GL_ACCUM, 0.5f);
            
            
            gl.glAccum(GL2.GL_RETURN, 1.0f);
            
        } else if ( viewingMode == 4 ) {
        	
            // Objective 6 - draw the left eye view
        	gl.glPushMatrix();
        	leftEyeFrustum.lookThroughFrustum(drawable, glut, scene);
            gl.glPopMatrix();
            
        } else if ( viewingMode == 5 ) {  
        	
        	// Objective 6 - draw the right eye view
        	gl.glPushMatrix();
        	rightEyeFrustum.lookThroughFrustum(drawable, glut, scene);
            gl.glPopMatrix();
            
        } else if ( viewingMode == 6 ) {            
        	
        	// Objective 7 - draw the anaglyph view using glColouMask
        	// Left Eye
        	gl.glPushMatrix();
        	gl.glColorMask( true, false, false, true );
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	leftEyeFrustum.lookThroughFrustum(drawable, glut, scene);
        	gl.glPopMatrix();
        	
        	// Right eye
        	gl.glPushMatrix();
        	gl.glColorMask( false, false, true, true );
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	rightEyeFrustum.lookThroughFrustum(drawable, glut, scene);
        	gl.glPopMatrix();
        	
        	gl.glColorMask( true, true, true, true );
        	
        	
        } else if ( viewingMode == 7 || viewingMode == 8 ) { 
        	// I had to modify the modes to comply with the assignment instructions
        	// Objective 8 
        	
        	// Shared setup for calculating frustums based on the new viewports
        	int leftWPixels = drawable.getSurfaceWidth() / 2;
        	int rightWPixels = drawable.getSurfaceWidth() - leftWPixels; // Makes sure every pixel is used
        	int heightPixels = drawable.getSurfaceHeight();
        	
        	double leftWidth = leftWPixels * metersPerPixel;
        	double rightWidth = rightWPixels * metersPerPixel;
        	
        	// Recalculate the frustums for the squished vision
        	leftEyeFrustum = new Frustum(leftEye, near.getValue(), far.getValue(), 
        			h/2, leftWidth/2, focalPlaneZPosition.getValue());
            rightEyeFrustum = new Frustum(rightEye, near.getValue(), far.getValue(), 
    				h/2, rightWidth/2, focalPlaneZPosition.getValue());
            
            if ( viewingMode == 7 ){
            	// Objective 8 - Left Right side by side
            	gl.glViewport(0, 0, leftWPixels, heightPixels);
            	gl.glPushMatrix();
            	leftEyeFrustum.lookThroughFrustum(drawable, glut, scene);
                gl.glPopMatrix();            	
                
                gl.glViewport(leftWPixels, 0, rightWPixels, heightPixels);
            	gl.glPushMatrix();
            	rightEyeFrustum.lookThroughFrustum(drawable, glut, scene);
                gl.glPopMatrix();
            }
            else if (viewingMode == 8) {
            	// Objective 8 - Right Left side by side
            	gl.glViewport(0, 0, rightWPixels, heightPixels);
            	gl.glPushMatrix();
            	rightEyeFrustum.lookThroughFrustum(drawable, glut, scene);
                gl.glPopMatrix();
                
                gl.glViewport(rightWPixels, 0, leftWPixels, heightPixels);
            	gl.glPushMatrix();
            	leftEyeFrustum.lookThroughFrustum(drawable, glut, scene);
                gl.glPopMatrix();            	
            }
            
            gl.glViewport(0, 0, drawable.getSurfaceWidth(), heightPixels);
            
        } else if ( viewingMode == 9 ) {            
        	// TODO: Bonus: The combination of a depth of field blur for each eye show as an anaglyph
        	// Left Eye
        	gl.glPushMatrix();
        	gl.glColorMask( true, false, false, true );
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	applyMotionBlurAndDraw(leftEyeFrustum, drawable, gl);
        	gl.glPopMatrix();
        	
        	// Right eye
        	gl.glPushMatrix();
        	gl.glColorMask( false, false, true, true );
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	applyMotionBlurAndDraw(rightEyeFrustum, drawable, gl);
        	gl.glPopMatrix();
        	
        	gl.glColorMask( true, true, true, true );
        	
        } else if (viewingMode == 10 || viewingMode == 11) {
        	// Objective 10 and 11 common setup
        	
        	// Shared setup for calculating frustums based on the new viewports
        	int leftWPixels = drawable.getSurfaceWidth() / 2;
        	int rightWPixels = drawable.getSurfaceWidth() - leftWPixels; // Makes sure every pixel is used
        	int heightPixels = drawable.getSurfaceHeight();
        	
        	double leftWidth = leftWPixels * metersPerPixel;
        	double rightWidth = rightWPixels * metersPerPixel;
        	
        	// Recalculate the frustums for the squished vision
        	leftEyeFrustum = new Frustum(leftEye, near.getValue(), far.getValue(), 
        			h/2, leftWidth/2, focalPlaneZPosition.getValue());
            rightEyeFrustum = new Frustum(rightEye, near.getValue(), far.getValue(), 
    				h/2, rightWidth/2, focalPlaneZPosition.getValue());
            
        	if ( viewingMode == 10 ) {            
            	// TODO: Bonus: Defocus with a left right side by side view.
        		gl.glViewport(0, 0, leftWPixels, heightPixels);
            	gl.glPushMatrix();
            	applyMotionBlurAndDraw(leftEyeFrustum, drawable, gl);
                gl.glPopMatrix();            	
                
                gl.glViewport(leftWPixels, 0, rightWPixels, heightPixels);
            	gl.glPushMatrix();
            	applyMotionBlurAndDraw(rightEyeFrustum, drawable, gl);
                gl.glPopMatrix();
            	
            	
            }else if ( viewingMode == 11 ) {            
            	// TODO: Bonus: Defocus with a right left side by side view.
            	gl.glViewport(0, 0, rightWPixels, heightPixels);
            	gl.glPushMatrix();
            	applyMotionBlurAndDraw(rightEyeFrustum, drawable, gl);
                gl.glPopMatrix();
                
                gl.glViewport(rightWPixels, 0, leftWPixels, heightPixels);
            	gl.glPushMatrix();
            	applyMotionBlurAndDraw(leftEyeFrustum, drawable, gl);
                gl.glPopMatrix();
            }
        }        
    }
    
    /***
     * Applies motion blue to the supplies frustum and renders the scene.
     */
    private void applyMotionBlurAndDraw(Frustum f, GLAutoDrawable drawable, GL2 gl){
    	Eye eye = f.eye;
    	// TODO Focal plane distance = focal length??? What math am i doing
    	double focalPlaneDistance = focalPlaneZPosition.getValue() - eye.z;
    	double apertureSize = focalPlaneDistance / aperture.getValue();
    	
    	// TODO: Objective 5 - draw center eye with depth of field blur
    	FastPoissonDisk fpd = new FastPoissonDisk();
    	f.lookThroughFrustum(drawable, glut, scene);
    	for (int i = 1; i <= samples.getValue(); i++) {
//    		fpd.get(p, i, N);
    	}
    	gl.glAccum(GL2.GL_RETURN, 1.0f);    	

    }
    
}
