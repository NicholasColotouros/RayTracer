package comp557.a2.helperObjects;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

/***
 * Representation of the Eye.
 * 
 * @author Nicholas Nathan Colotouros, 560531370
 *
 */
public class Eye extends Drawable {
	public double x;
	public double y;
	public double z;
	public double radius;
	
	public Eye(double worldX, double worldY, double worldZ, double radiusInPixels){
		x = worldX;
		y = worldY;
		z = worldZ;
		radius = radiusInPixels;
	}

	@Override
	public void display(GLAutoDrawable drawable, GLUT glut) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glPushMatrix();
        gl.glColor3f(colour[0], colour[1], colour[2]);
        gl.glTranslated(x, y, z);
        glut.glutSolidSphere( radius, 10, 10);
        gl.glPopMatrix();
        gl.glEnable(GL2.GL_LIGHTING);
	}	
}
