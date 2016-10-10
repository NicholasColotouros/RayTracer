package comp557.a2.helperObjects;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;


/***
 * 
 * @author Nicholas Nathan Colotouros, 560531370
 *
 */
public class Rectangle extends Drawable {
	public double top;
	public double bottom;
	public double left;
	public double right;

	public double z;
	
	public Rectangle (double width, double height, double worldZ){
		top = height/2;
		bottom = -top;
		
		right = width/2;
		left = -right;
		
		z = worldZ;
	}
	
	public Rectangle(double l, double r, double b, double t, double zCoordinate){
		left = l;
		right = r;
		top = t;
		bottom = b;
		
		z = zCoordinate;
	}
	
	@Override
	public void display(GLAutoDrawable drawable, GLUT glut) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glDisable(GL2.GL_LIGHTING);
	
        gl.glColor3f(colour[0], colour[1], colour[2]);
        gl.glBegin(GL2.GL_LINE_LOOP);
        	gl.glVertex3d(left, bottom, z); // bottom left
        	gl.glVertex3d(right, bottom, z); // bottom right
        	gl.glVertex3d(right, top, z); // upper right
        	gl.glVertex3d(left, top, z); // upper left
        gl.glEnd();

        gl.glEnable(GL2.GL_LIGHTING);
	}
}
