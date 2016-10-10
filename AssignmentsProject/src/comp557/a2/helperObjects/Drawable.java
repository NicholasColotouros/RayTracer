package comp557.a2.helperObjects;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

/***
 * Based on the display method from A1 and some convenience added in for colour.
 * I'm using this for Eclipse to auto-fill my objects and to keep a consistent signature.
 * 
 * @author Nicholas Colotouros, 260531370
 *
 */
public abstract class Drawable {
	public float[] colour = new float[]{1,1,1}; // white by default
	
	// Exactly like the display method in the DAGNode, except it expects the glut from the A2app
	// I'm not sure if using it's own glut will change anything so I won't take chances
	public abstract void display( GLAutoDrawable drawable, GLUT glut);	
}
