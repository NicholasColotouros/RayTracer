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
public class FreeJoint extends DAGNode {
	public DoubleParameter xTrans;
	public DoubleParameter yTrans;
	public DoubleParameter zTrans;
	
	public DoubleParameter xRot;
	public DoubleParameter yRot;
	public DoubleParameter zRot;
	
	public FreeJoint(){
		double maxTranslation = 10;
		double minTranslation = -1 * maxTranslation;
		xTrans = new DoubleParameter("x Translation", 0, minTranslation, maxTranslation);
		yTrans = new DoubleParameter("y Translation", 0, minTranslation, maxTranslation);
		zTrans = new DoubleParameter("z Translation", 0, minTranslation, maxTranslation);

		double minRotation = 0;
		double maxRotation = 360;
		xRot = new DoubleParameter("x Rotation", 0, minRotation, maxRotation);
		yRot = new DoubleParameter("y Rotation", 0, minRotation, maxRotation);
		zRot = new DoubleParameter("z Rotation", 0, minRotation, maxRotation);
		
		dofs.add(xTrans);
		dofs.add(yTrans);
		dofs.add(zTrans);
		
		dofs.add(xRot);
		dofs.add(yRot);
		dofs.add(zRot);
	}
	
	public void setTranslationParameters(double xVal, double xMin, double xMax, 
										double yVal, double yMin, double yMax, 
										double zVal, double zMin, double zMax){
		xTrans.setMinimum(xMin);
		xTrans.setMaximum(xMax);
		xTrans.setValue(xVal);
		
		yTrans.setMinimum(yMin);
		yTrans.setMaximum(yMax);
		yTrans.setValue(yVal);
		
		zTrans.setMinimum(zMin);
		zTrans.setMaximum(zMax);
		zTrans.setValue(zVal);
	}
	
	public void setRotationParameters(double xVal, double xMin, double xMax, 
			double yVal, double yMin, double yMax, 
			double zVal, double zMin, double zMax){
		xRot.setMinimum(xMin);
		xRot.setMaximum(xMax);
		xRot.setValue(xVal);
		
		yRot.setMinimum(yMin);
		yRot.setMaximum(yMax);
		yRot.setValue(yVal);
		
		zRot.setMinimum(zMin);
		zRot.setMaximum(zMax);
		zRot.setValue(zVal);
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
