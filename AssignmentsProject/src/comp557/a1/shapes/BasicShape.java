package comp557.a1.shapes;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import comp557.a1.DAGNode;
import mintools.parameters.DoubleParameter;

/**
 * 
 * @author Nicholas Nathan Colotouros, 260531370
 *
 * Is the base class for drawing shapes
 */
public class BasicShape extends DAGNode {
	
	// Offset from origin to draw this
	public DoubleParameter x;
	public DoubleParameter y;
	public DoubleParameter z;
	
	// All shapes for this assignment will have a width and height
	public DoubleParameter width;
	public DoubleParameter height;
	
	// Used so that the shape can be drawn rotated without the need for a joint
	double rx;
	double ry;
	double rz;
	
	// Color of the shape
	float[] color;
	
	public BasicShape(double w, double h, double xOffset, double yOffset, double zOffset, float r, float g, float b, boolean canDistort){
		width = new DoubleParameter("width", w, 0, 1.5 * w);
		height = new DoubleParameter("height", h, 0, 1.5 * h);
		
		// Used primarily for eyes/mouth. 0 to 1.5x is used because it's a cartoon character so exaggerated movements are common
		if(canDistort){
			dofs.add(width);
			dofs.add(height);
		}
		
		x = new DoubleParameter("X", xOffset, xOffset, xOffset);
		y = new DoubleParameter("Y", yOffset, yOffset, yOffset);
		z = new DoubleParameter("Z", zOffset, zOffset, zOffset);
		
		color = new float[]{r,g,b,1};
	}
	
	public BasicShape(double w, double h, double xOffset, double yOffset, double zOffset){
		width = new DoubleParameter("width", w, 0.1, 2 * w);
		height = new DoubleParameter("height", h, 0.1, 2*h);
		
		x = new DoubleParameter("X", xOffset, xOffset, xOffset);
		y = new DoubleParameter("Y", yOffset, yOffset, yOffset);
		z = new DoubleParameter("Z", zOffset, zOffset, zOffset);
		
		// Set color to white unless otherwise specified
		color = new float[]{1,1,1,1};
	}
	
	public BasicShape(double w, double h){
		width = new DoubleParameter("width", w, 0.1, 2 * w);
		height = new DoubleParameter("height", h, 0.1, 2*h);
		
		double xOffset = 0;
		double yOffset = 0;
		double zOffset = 0;
		
		
		x = new DoubleParameter("X", xOffset, xOffset, xOffset);
		y = new DoubleParameter("Y", yOffset, yOffset, yOffset);
		z = new DoubleParameter("Z", zOffset, zOffset, zOffset);
		
		// Set color to white unless otherwise specified
		color = new float[]{1,1,1,1};
	}
	
	public void SetColor(float r, float g, float b){
		color = new float[]{r,g,b,1};
	}
	
	// only to be called during character construction
	public void SetRotation(double rotX, double rotY, double rotZ){
		rx = rotX;
		ry = rotY;
		rz = rotZ;
	}
	
	protected void drawColor(GL2 gl){
		gl.glMaterialfv( GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0 );
        gl.glMaterialfv( GL.GL_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, color, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50 );
	}
	
	// The rotation is not to be articulated or moved. This is convenience for drawing a sideways cone or whatnot.
	protected void applyOffsetAndRotation(GL2 gl){
		gl.glTranslated(x.getValue(), y.getValue(), z.getValue());
		
		gl.glRotated(rx, 1, 0, 0);
		gl.glRotated(ry, 0, 1, 0);
		gl.glRotated(rz, 0, 0, 1);
	}
}
