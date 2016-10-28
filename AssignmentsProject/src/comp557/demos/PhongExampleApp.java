package comp557.demos;

import java.awt.Dimension;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.FancyAxis;
import mintools.viewer.SceneGraphNode;

/**
 * Simple demo of opengl materials and lights
 * @author kry
 */
public class PhongExampleApp implements SceneGraphNode {

    static public void main( String[] args ) {
        new PhongExampleApp();
    }
    
    public PhongExampleApp() {
        new EasyViewer( "Phong demo", this, new Dimension(500,500),new Dimension(500,500));
    }
    
    FancyAxis fa = new FancyAxis();
    
    /**
     * Helper class for setting colours
     * @author kry
     */
    class Colour {
        String name;
        DoubleParameter red = new DoubleParameter("r", 1, 0, 1); 
        DoubleParameter green = new DoubleParameter("g", 1, 0, 1);
        DoubleParameter blue = new DoubleParameter("b", 1, 0, 1);
        float[] v = new float[4];
        public Colour(String name, double dr, double dg, double db ) {
            this.name = name;
            red.setDefaultValue(dr);
            green.setDefaultValue(dg);
            blue.setDefaultValue(db);
        }
        float[] get() {
            v[0] = red.getFloatValue();
            v[1] = green.getFloatValue();
            v[2] = blue.getFloatValue();
            v[3] = 1;
            return v;
        }
        JPanel getControls() {
            VerticalFlowPanel vfp = new VerticalFlowPanel();
            vfp.add( new JLabel(name));
            vfp.add( red.getSliderControls(false) );
            vfp.add( green.getSliderControls(false) );
            vfp.add( blue.getSliderControls(false) );            
            return vfp.getPanel();
        }        
    }
    
    /**
     * Helper class for setting a grey level
     * @author kry
     */
    class GreyColour {
        String name;
        DoubleParameter r = new DoubleParameter("greylevel", 1, 0, 1);         
        float[] v = new float[4];
        public GreyColour(String name, double d) {
            this.name = name;
            r.setDefaultValue(d);
        }
        float[] get() {
            v[0] = r.getFloatValue();
            v[1] = r.getFloatValue();
            v[2] = r.getFloatValue();
            v[3] = 1;
            return v;
        }
        JPanel getControls() {
            VerticalFlowPanel vfp = new VerticalFlowPanel();
            vfp.add( new JLabel(name));
            vfp.add( r.getSliderControls(false) );                      
            return vfp.getPanel();
        }        
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        if ( wire.getValue() ) {
        	gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_LINE );
        } else {
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_FILL );            
        }
        
        if ( flat.getValue() ) {
            gl.glShadeModel( GL2.GL_FLAT );
        } else {
            gl.glShadeModel( GL2.GL_SMOOTH );
        }
        
        gl.glDisable( GL2.GL_LIGHT1 );
        
        // put a dim light at the back of the room, in case anyone wants to 
        // look at the back side of objects
        int lightNumber = 0;
        float[] position = { 10f, 10f, 10f, 1 };
        position[0] = lightPos.getFloatValue();
        position[1] = lightPos.getFloatValue();
        position[2] = lightPos.getFloatValue();        
        gl.glLightfv(GL2.GL_LIGHT0 + lightNumber, GL2.GL_SPECULAR, lightColour.get(), 0);
        gl.glLightfv(GL2.GL_LIGHT0 + lightNumber, GL2.GL_DIFFUSE, lightColour.get(), 0);
        gl.glLightfv(GL2.GL_LIGHT0 + lightNumber, GL2.GL_AMBIENT, lightColourAmbient.get(), 0);
        gl.glLightfv(GL2.GL_LIGHT0 + lightNumber, GL2.GL_POSITION, position, 0);
        gl.glEnable( GL2.GL_LIGHT0 + lightNumber );

        // GL_SPOT_CUTOFF, 
        // GL_SPOT_DIRECTION, 
        // GL_SPOT_EXPONENT, 
        gl.glLightf(GL2.GL_LIGHT0 + lightNumber, GL2.GL_CONSTANT_ATTENUATION, a.getFloatValue()); 
        gl.glLightf(GL2.GL_LIGHT0 + lightNumber, GL2.GL_LINEAR_ATTENUATION, b.getFloatValue() );
        gl.glLightf(GL2.GL_LIGHT0 + lightNumber, GL2.GL_QUADRATIC_ATTENUATION, c.getFloatValue() ); 
        
        gl.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, lightModelAmbient.get(), 0 );
        
        // draw the light position
        gl.glPushMatrix();
        gl.glTranslated( position[0], position[1], position[2] );
        gl.glDisable( GL2.GL_LIGHTING );
        gl.glColor4f(1,1,0,1);
        EasyViewer.glut.glutSolidSphere(.3, 60, 40);
        gl.glEnable( GL2.GL_LIGHTING );
        gl.glPopMatrix();
        
        gl.glPushMatrix();
        gl.glTranslated( -3,-3,-3);
        float[] white = {1,1,1,1};
        float[] black = {0,0,0,1};
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, black, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION, black, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, black, 0 );

        fa.draw(gl);
        gl.glPopMatrix();
        
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, materialDiffuse.get(), 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, materialAmbient.get(), 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, materialSpecular.get(), 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION, materialEmission.get(), 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shinyness.getFloatValue() );

        
        EasyViewer.glut.glutSolidSphere(3, res.getValue()*2, res.getValue());
     
        
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, white, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, black, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION, black, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, black, 0 );
        gl.glPushMatrix();
        gl.glTranslated(0,0,-3.5);
        gl.glScaled(30,30,0.1);
        if ( cube.getValue() ) {
            EasyViewer.glut.glutSolidCube(1);
        } else {         
            gl.glScaled(1,1,0.1);
            EasyViewer.glut.glutSolidSphere(1,60,40);
        }
        gl.glPopMatrix();
        
    }
    
    BooleanParameter wire = new BooleanParameter( "wire frame", false );
    
    BooleanParameter flat = new BooleanParameter( "flat shading", false );
    
    BooleanParameter cube = new BooleanParameter( "cube for floor", true );
    
    IntParameter res = new IntParameter( "resolution", 30, 3, 60 );
    
    DoubleParameter lightPos =new DoubleParameter("light position", 10, 1.85, 1000 );
    
    DoubleParameter shinyness = new DoubleParameter("shynyness", 128, 1, 128 );
    
    Colour materialDiffuse = new Colour("materialDiffuse", 1,1,1);
    Colour materialAmbient = new Colour("materialAmbient", 0.1,0.1,0.1);
    Colour materialSpecular = new Colour("materialSpecular", 1,1,1);
    Colour materialEmission = new Colour("materialEmission", 0,0,0);
    
    
    Colour lightColour = new Colour("lightColor", 1,1,1);
    GreyColour lightColourAmbient = new GreyColour("lightColor ambient", 0 );
    
    GreyColour lightModelAmbient = new GreyColour("light model ambient", 0.1 );
    
    DoubleParameter a = new DoubleParameter("attenuation a (constant)", 1, 0, 1);
    DoubleParameter b = new DoubleParameter("attenuation b (linear)", 0, 0, .1);
    DoubleParameter c = new DoubleParameter("attenuation c (quadratic)", 0, 0, .1);
    
    @Override
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( wire.getControls() );
        vfp.add( flat.getControls() );
        vfp.add( cube.getControls() );
        vfp.add( res.getSliderControls() );
        vfp.add( shinyness.getSliderControls(false) );
        vfp.add( materialDiffuse.getControls() );
        vfp.add( materialAmbient.getControls() );
        vfp.add( materialSpecular.getControls() );
        vfp.add( materialEmission.getControls() );
        vfp.add( lightColour.getControls() );       
        vfp.add( lightColourAmbient.getControls() );
        vfp.add( lightModelAmbient.getControls() );
        vfp.add( a.getSliderControls(false) );
        vfp.add( b.getSliderControls(false) );
        vfp.add( c.getSliderControls(false) );
        vfp.add( lightPos.getSliderControls(true));
        return vfp.getPanel();
    }
    @Override
    public void init(GLAutoDrawable drawable) {
        // do nothing
    }
    
}
