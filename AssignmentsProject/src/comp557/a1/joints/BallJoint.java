package comp557.a1.joints;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import comp557.a1.DAGNode;
import mintools.parameters.DoubleParameter;

/**
 * 
 * @author Nicholas Nathan Colotouros, 260531370
 * 
 * Reasonable max/mins depend on the type of ball joint.
 * They are reasonably set during character creation.
 */
public class BallJoint extends DAGNode {
	public DoubleParameter xTrans;
	public DoubleParameter yTrans;
	public DoubleParameter zTrans;
	
	public DoubleParameter xRot;
	public DoubleParameter yRot;
	public DoubleParameter zRot;

	public BallJoint(String name, double xOffset, double yOffset, double zOffset, double startXAngle, double startYAngle, double startZAngle){
		xTrans = new DoubleParameter(name + " - x trans", xOffset, xOffset, xOffset);
		yTrans = new DoubleParameter(name + " - y trans", yOffset, yOffset, yOffset);
		zTrans = new DoubleParameter(name + " - z trans", zOffset, zOffset, zOffset);
		
		xRot = new DoubleParameter(name + " - x", startXAngle, 0, 360);
		yRot = new DoubleParameter(name + " - x", startYAngle, 0, 360);
		zRot = new DoubleParameter(name + " - x", startZAngle, 0, 360);
	
		dofs.add(xRot);
		dofs.add(yRot);
		dofs.add(zRot);
	}
	
	public void setMaximumAngle(double x, double y, double z){
		xRot.setMaximum(x);
		yRot.setMaximum(y);
		zRot.setMaximum(z);
	}
	
	public void setMinimumAngle(double x, double y, double z){
		xRot.setMinimum(x);
		yRot.setMinimum(y);
		zRot.setMinimum(z);
	}

	@Override
	public void display( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glPushMatrix();

		gl.glTranslated(xTrans.getValue(), yTrans.getValue(), zTrans.getValue());
		gl.glRotated(xRot.getValue(), 1, 0, 0);
		gl.glRotated(yRot.getValue(), 0, 1, 0);
		gl.glRotated(zRot.getValue(), 0, 0, 1);
		
		super.display(drawable);
		gl.glPopMatrix();
	}

}
