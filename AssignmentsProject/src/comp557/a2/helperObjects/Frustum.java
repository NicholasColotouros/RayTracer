package comp557.a2.helperObjects;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import comp557.a2.Scene;
import mintools.viewer.FlatMatrix4d;

/***
 * Contains all functionality relating to the frustum.
 * This includes calculations of near, far and focal planes, drawing the outlines and viewing through it.
 * 
 * @author Nicholas Nathan Colotouros, 560531370
 *
 */
public class Frustum extends Drawable {
	private static enum FrustumMode {DISPLAY, VIEW};
	
	public double top, bottom, left, right, near, far;
	public double focalTop, focalBottom, focalLeft, focalRight, focalZ;
	public Eye eye;
	
	/**
	 * Does the calculations for the sides of the near plane. Assumes everything to be in world coordinates.
	 * @param pEye the coordinates of the eye
	 * @param pNear Z coordinate of near plane
	 * @param pFar Z coordinate of far plane
	 * @param rectangleTop Y coordinate of the top of the rectangle (screen height /2)
	 * @param rectangleRight X coordinate of the top of the rectangle (screen width /2)
	 * @param pFocalZ The focal Z parameter
	 */
	public Frustum (Eye pEye, double pNear, double pFar, double rectangleTop, double rectangleRight, double pFocalZ){
		eye = pEye;
		this.colour = eye.colour;
		
		// Convert near and far to camera coordinates
		near = pNear - eye.z;
		far = pFar - eye.z;
		
		// define the rectangle sides in terms of camera
		double rectCamTop = rectangleTop - eye.y;
		double rectCamBottom = (-1 * rectangleTop) - eye.y;
		double rectCamRight = rectangleRight  - eye.x;
		double rectCamLeft = (-1* rectangleRight)  - eye.x;
		
		double originZCam = 0 - eye.z;
		
		// Objective 4 -- Frustums for generalized eye position 
		top = (near/originZCam) * rectCamTop;
		bottom = (near/originZCam) * rectCamBottom;
		
		right = (near/originZCam) * rectCamRight;
		left = (near/originZCam) * rectCamLeft;
		
		// Objective 4 -- focal points for generalized eye position
		focalZ = pFocalZ - eye.z;
		focalTop = (focalZ/originZCam) * rectCamTop;
		focalBottom = (focalZ/originZCam) * rectCamBottom;
		
		focalRight = (focalZ/originZCam) * rectCamRight;
		focalLeft = (focalZ/originZCam) * rectCamLeft;		
	}
	
	/***
	 * Used for offset for defocus blur samples.
	 * 
	 * Takes the same parameters as the previous constructors but now the rectangle top/right/left/bottom
	 * are those of the focal plane in world coordinates for which the frustum will fit exactly. They are
	 * expected to be in world coordinates.
	 * 
	 * The calculations are the same as the above constructors but with the parameters altered to accomodate
	 * for the change in what's being used to calculate the near plane.
	 * 
	 */
	public Frustum(Eye pEye, double pNear, double pFar, double pFocalZ,
			double rectangleTop, double rectangleBottom, double rectangleRight, double rectangleLeft){
		eye = pEye;
		this.colour = eye.colour;
		
		// Convert near and far to camera coordinates
		near = pNear - eye.z;
		far = pFar - eye.z;
		
		// define the focal plane in camera coordinates
		focalZ = pFocalZ - eye.z;
		
		focalTop = rectangleTop - eye.y;
		focalBottom = rectangleBottom - eye.y;
		focalRight = rectangleRight  - eye.x;
		focalLeft = rectangleLeft  - eye.x;
		
		// Similar triangles used to compute the sides of the near plane  
		top = (near/focalZ) * focalTop;
		bottom = (near/focalZ) * focalBottom;
		
		right = (near/focalZ) * focalRight;
		left = (near/focalZ) * focalLeft;		
	}
	
	@Override
	public void display(GLAutoDrawable drawable, GLUT glut) {
		createFrustum(drawable, glut, FrustumMode.DISPLAY, null);
	}
	
	public void lookThroughFrustum(GLAutoDrawable drawable, GLUT glut, Scene scene) {
		createFrustum(drawable, glut, FrustumMode.VIEW, scene);
	}
	
	public Rectangle getFocalRectangleInWorldCoordinates() {
		Rectangle focalPoint = new Rectangle(focalLeft + eye.x, focalRight + eye.x, 
											focalBottom + eye.y, focalTop + eye.y, focalZ + eye.z);
		focalPoint.colour = new float[]{0.5f, 0.5f, 0.5f};
		return focalPoint;
	}

	GLU glu = new GLU();
	
	private void createFrustum(GLAutoDrawable drawable, GLUT glut, FrustumMode mode, Scene scene){
		GL2 gl = drawable.getGL().getGL2();
		
		// Allocate matrices
		FlatMatrix4d V = new FlatMatrix4d();
		FlatMatrix4d Vinv = new FlatMatrix4d();
		FlatMatrix4d P = new FlatMatrix4d();
		FlatMatrix4d Pinv = new FlatMatrix4d();

		gl.glPushMatrix();
		
		// make and get the viewing transformation
		gl.glLoadIdentity();		
		glu.gluLookAt( eye.x, eye.y, eye.z, 	// center of eye in world coords
						eye.x, eye.y, eye.z-1,	// Look dead ahead 
						0,1,0 ); 				// Up in world coords
		gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, V.asArray(), 0 );
		
		gl.glLoadIdentity();
		gl.glFrustum(left, right, bottom, top, -near, -far);
		gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, P.asArray(), 0 );
		gl.glPopMatrix();
		
		V.reconstitute();
		P.reconstitute();
		Vinv.getBackingMatrix().invert( V.getBackingMatrix() );
		Pinv.getBackingMatrix().invert( P.getBackingMatrix() );
		
		// If we're looking through the frustum we need to set the projection matrix and draw the world
		if(mode == FrustumMode.VIEW){
			gl.glMatrixMode( GL2.GL_PROJECTION );
			gl.glLoadMatrixd( P.asArray(), 0 );
			gl.glMatrixMode( GL2.GL_MODELVIEW );
			gl.glLoadMatrixd( V.asArray(), 0 );
			scene.display(drawable);
		}
		
		// Draw the frustum and focal point if we're not looking through it.
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glPushMatrix();
		gl.glMultMatrixd( Vinv.asArray(), 0 ); // Eye space
		gl.glColor3f(colour[0],colour[1],colour[2]);
		gl.glMultMatrixd( Pinv.asArray(), 0 ); // Post-projection space
		
		if(mode == FrustumMode.DISPLAY){
			glut.glutWireCube(2);			
		}
		
		gl.glPopMatrix();
		
		// World space
		
		gl.glEnable(GL2.GL_LIGHTING);
		
		// Draw the eye after we're done
		if(mode == FrustumMode.DISPLAY){
			eye.display(drawable, glut);
		}
	}
}
