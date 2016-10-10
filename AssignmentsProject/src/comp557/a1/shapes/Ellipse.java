package comp557.a1.shapes;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * 
 * @author Nicholas Nathan Colotouros, 260531370
 *
 */
public class Ellipse extends BasicShape {
	
	public Ellipse(double rad, double h) {
		super(rad, h);
	}

	public Ellipse(double rad, double h, double xOffset, double yOffset, double zOffset) {
		super(rad, h, xOffset, yOffset, zOffset);
	}

	public Ellipse(double rad, double h, double xOffset, double yOffset, double zOffset, float r, float g, float b, boolean canDistort) {
		super(rad, h, xOffset, yOffset, zOffset, r, g, b, canDistort);
	}

	@Override
	public void display( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glPushMatrix();
		drawColor(gl);
		applyOffsetAndRotation(gl);
		gl.glPushMatrix();
		gl.glScaled(width.getValue(), height.getValue(), width.getValue());
		glut.glutSolidSphere(1, 10, 10);
		gl.glPopMatrix();
		super.display(drawable);		
		gl.glPopMatrix();
	}
}
