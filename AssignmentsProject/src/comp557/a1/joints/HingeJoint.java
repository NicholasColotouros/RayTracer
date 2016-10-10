package comp557.a1.joints;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import comp557.a1.DAGNode;
import mintools.parameters.DoubleParameter;

/**
 * 
 * @author Nicholas Nathan Colotouros, 260531370
 *
 */
public class HingeJoint extends DAGNode {

	// Offset
	public DoubleParameter xTrans;
	public DoubleParameter yTrans;
	public DoubleParameter zTrans;
	
	// Axis about which to rotate
	public double xAxis;
	public double yAxis;
	public double zAxis;
	
	
	public DoubleParameter hingeAngle;
	
	
	public HingeJoint(String name, double xOffset, double yOffset, double zOffset, double xRot, double yRot, double zRot, double hingeStartAngle, double hingeMinAngle, double hingeMaxAngle){
		xTrans = new DoubleParameter("xOffset", xOffset, xOffset, xOffset);
		yTrans = new DoubleParameter("yOffset", yOffset, yOffset, yOffset);
		zTrans = new DoubleParameter("zOffset", zOffset, zOffset, zOffset);
		
		xAxis = xRot;
		yAxis = yRot;
		zAxis = zRot;
		
		hingeAngle = new DoubleParameter(name + " angle", hingeStartAngle, hingeMinAngle, hingeMaxAngle);
			
		dofs.add(hingeAngle);
	}
	
	@Override
	public void display( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glPushMatrix();

		gl.glTranslated(xTrans.getValue(), yTrans.getValue(), zTrans.getValue());
		gl.glRotated(hingeAngle.getValue(), xAxis, yAxis, zAxis);
		
		super.display(drawable);
		gl.glPopMatrix();
	}
}
