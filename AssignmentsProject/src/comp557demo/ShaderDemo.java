package comp557demo;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

public class ShaderDemo implements GLEventListener {
	
	public static void main( String[] args ) {
		new ShaderDemo();	
	}
	
	public ShaderDemo() {
		GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        GLCanvas glcanvas = new GLCanvas( glcapabilities );
        glcanvas.addGLEventListener(this);
        glcanvas.addKeyListener( new KeyAdapter() {
        	@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
        		useShader = !useShader;
			}
		});
        FPSAnimator animator; 
        animator = new FPSAnimator(glcanvas, 60);
        animator.start();
        final JFrame jframe = new JFrame( "JOGL GLSL Test" ); 
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                jframe.dispose();
                System.exit( 0 );
            }
        });
        jframe.getContentPane().add( glcanvas, BorderLayout.CENTER );
        jframe.setSize( 640, 480 );
        jframe.setVisible( true );
	}
	
	GLU glu = new GLU();
	GLUT glut = new GLUT();
	
	boolean useShader = false;
	
	ShaderState state = new ShaderState();

	@Override
	public void init(GLAutoDrawable drawable) {
		drawable.setGL( new DebugGL2( drawable.getGL().getGL2() ) );
		GL2 gl = drawable.getGL().getGL2();

        gl.glClearColor( 0, 0, 0, 1 );  // Black Background
        gl.glClearDepth( 1 );          	// Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do

        gl.glEnable( GL2.GL_LIGHTING );
        gl.glEnable( GL2.GL_LIGHT0 );
        gl.glEnable( GL2.GL_NORMALIZE );	// normalize normals before lighting
       
        // TODO: choose the program to set up
        //String shaderName = "basic";
        //String shaderName = "perVertexLighting";
        String shaderName = "perFragmentLighting";
        ShaderCode vsCode = ShaderCode.create( gl, GL2.GL_VERTEX_SHADER, this.getClass(), "shaders", "shader/bin", shaderName, false );
        ShaderCode fsCode = ShaderCode.create( gl, GL2.GL_FRAGMENT_SHADER, this.getClass(), "shaders", "shader/bin", shaderName, false );	
        ShaderProgram program = new ShaderProgram();
        program.add( vsCode );
        program.add( fsCode );
		if ( !program.link(gl, System.err) ) {
			throw new GLException("Couldn't link program: " + program );
		}	
		state.attachShaderProgram( gl, program, false );
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // TODO: choose the shading model for the standard OpenGL pipeline
		//gl.glShadeModel( GL2.GL_FLAT );
        gl.glShadeModel( GL2.GL_SMOOTH ); // Enable Smooth Shading (Gouraud)

		gl.glMatrixMode( GL2.GL_PROJECTION );
		gl.glLoadIdentity();
		int width = drawable.getSurfaceWidth();
		int height = drawable.getSurfaceHeight();
		float fovy = 45;
		float aspectRatio = (float) width / (float) height;
		float near = 1;
		float far = 15;
        glu.gluPerspective( fovy, aspectRatio, near, far );

		gl.glMatrixMode( GL2.GL_MODELVIEW );
		gl.glLoadIdentity();
		glu.gluLookAt( 0, 0, 5, 0, 0, 0, 0, 1, 0 );
		
				
		int lightNumber = 0;
		float[] position = { 5, 5, 5 , 1 };
        float[] colour = { 1f, 1f, 1f, 1 };
        float[] acolour = {0,0,0,1};//{ .05f, .05f, .05f, 1 };
        gl.glLightfv( GL2.GL_LIGHT0 + lightNumber, GL2.GL_SPECULAR, colour, 0 );
        gl.glLightfv( GL2.GL_LIGHT0 + lightNumber, GL2.GL_DIFFUSE, colour, 0 );
        gl.glLightfv( GL2.GL_LIGHT0 + lightNumber, GL2.GL_AMBIENT, acolour, 0 );
        gl.glLightfv( GL2.GL_LIGHT0 + lightNumber, GL2.GL_POSITION, position, 0 ); // transformed by the modelview matrix when glLight is called
        gl.glEnable( GL2.GL_LIGHT0 + lightNumber );

		gl.glRotated( 5*Math.cos(System.nanoTime()*1e-9), 0, 1, 0);

        
        // TODO: press any key to toggle use of the shader
        state.useProgram( gl, useShader );
        
        // TODO: try providing different parameters to the shaders with uniforms
                
        gl.glMaterialfv( GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {.5f,.5f,0,1}, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {1,1,1,1}, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50 );
		glut.glutSolidSphere( 1, 20, 15 );
		
        gl.glMaterialfv( GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,1,1,1}, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {1,1,1,1}, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50 );
		gl.glPushMatrix();
		gl.glTranslated(2, 2, -4);
		gl.glScaled(5,5,0.1);
		glut.glutSolidCube(2);
		gl.glPopMatrix();
		
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// can let application exit dispose of our resources
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// glViewport already called by component		
	}
	
}
