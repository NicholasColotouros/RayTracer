package comp557.a1.shapes;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * 
 * @author Nicholas Nathan Colotouros, 260531370
 *
 */
public class Cone extends BasicShape {
	public Cone(double rad, double h) {
		super(rad, h);
	}

	public Cone(double rad, double h, double xOffset, double yOffset, double zOffset) {
		super(rad, h, xOffset, yOffset, zOffset);
	}

	public Cone(double rad, double h, double xOffset, double yOffset, double zOffset, float r, float g, float b, boolean canDistort) {
		super(rad, h, xOffset, yOffset, zOffset, r, g, b, canDistort);
	}

	@Override
	public void display( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glPushMatrix();
		drawColor(gl);
		applyOffsetAndRotation(gl);
		glut.glutSolidCone(width.getValue(), height.getValue(), 10, 10);
		super.display(drawable);		
		gl.glPopMatrix();
	}
}
