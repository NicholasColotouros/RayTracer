package comp557demos;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.Interactor;
import mintools.viewer.SceneGraphNode;

/**
 * Simple demo of texture filtering/sampling parameters
 * @author kry
 */
public class TextureDemoApp implements Interactor, SceneGraphNode {

    @Override
    public void attach(Component component) {
        component.addKeyListener( new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch ( e.getKeyCode() ) {
                case KeyEvent.VK_1:
                    whichTexture = 1;
                    setTexture = true;
                    break;
                case KeyEvent.VK_2:
                    whichTexture = 2;
                    setTexture = true;
                    break;
                case KeyEvent.VK_3:
                    whichTexture = 3;
                    setTexture = true;
                    break;
                case KeyEvent.VK_4:
                	whichTexture = 4;
                	setTexture = true;
                	break;
                case KeyEvent.VK_RIGHT:
                    minoption.setValue( minoption.getValue() +1 );
                    break;
                case KeyEvent.VK_LEFT:
                    minoption.setValue( minoption.getValue() -1 );
                    break;
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    magoption.setValue( ! magoption.getValue() );
                    break;
                case KeyEvent.VK_A:
                    anisotropic.setValue( ! anisotropic.getValue() );
                    break;
                }                
            }
        });        
    }    
    
    int width = 512;
    int height = 512;

    private int buffersize = 3 * width * height;

    private ByteBuffer rawdataBuffer;
    
    private ByteBuffer[] rdb = new ByteBuffer[8];
    private byte[][] rd = new byte[8][];

    private byte[] rawdata;

    int textureID;
    
    private void clearRawBuffer() {
        for ( int y = 0; y < width; y++ ) {
            for ( int x = 0; x < height; x++ ) {
                for ( int i = 0; i < 3; i++ ) {
                    rawdata[(y*width+x)*3+i] = (byte) (((y/8)%2 + (x/8)%2 == 1) ? 0xff : 0);
                }
            }
        }
    }
    
    int whichTexture = 0;
   
    boolean setTexture = false;
        
    /**
     * Builds mipmaps for a checkerboard pattern
     * @param drawable
     */
    private void setChecker1( GLAutoDrawable drawable ) {
        clearRawBuffer();
        //glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, 3, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, rawdataBuffer );
    }
    
    /**
     * Loads mipmaps for a checkerboard pattern
     * (note, not much difference in this case, as it is easy to resample the checkerboard to smaller sizes)
     * @param drawable
     */
    private void setChecker2( GLAutoDrawable drawable ) {
        loadFiles( drawable, "checker" );
    }
    
    /**
     * Loads LOL cats for different mipmap levels
     * @param drawable
     */
    private void setCats( GLAutoDrawable drawable ) {
        loadFiles( drawable, "cat" );
    }
    
    private void loadFiles( GLAutoDrawable drawable, String root ) {
        GL gl = drawable.getGL();
        try {
            BufferedImage[] img = new BufferedImage[8];
            for ( int i = 0; i < 8; i++ ) {
                img[i] = ImageIO.read( new File( root + i + ".png" ) );
                
                int w = width >> i;
                int h = height >> i;
                int[] imageData = new int[w*h];
                img[i].getRGB(0,0, w,h, imageData, 0, w);
                
                rdb[i] = ByteBuffer.allocate(buffersize);
                rd[i] = rdb[i].array();
                
                for ( int x = 0; x < w; x++ ) {
                    for ( int y = 0; y < h; y++ ) {
                        int data = imageData[y*w+x];
                        rd[i][(x*w+y)*3 + 0] = (byte)((data >> 0) & 0x0ff); 
                        rd[i][(x*w+y)*3 + 1] = (byte)((data >> 8) & 0x0ff);
                        rd[i][(x*w+y)*3 + 2] = (byte)((data >> 16) & 0x0ff);       
                    }
                }
                
                gl.glTexImage2D( GL.GL_TEXTURE_2D,
                      i,
                      GL.GL_RGB8, //GL.GL_RGBA8,
                      width >> i,
                      height >> i,
                      0,
                      GL.GL_BGR,
                      GL.GL_UNSIGNED_BYTE,
                      rdb[i] );
            }
            for ( int i = 8; i < 10; i++ ) {
                gl.glTexImage2D( GL.GL_TEXTURE_2D,
                        i,
                        GL.GL_RGB8, //GL.GL_RGBA8,
                        width >> i,
                        height >> i,
                        0,
                        GL.GL_BGR,
                        GL.GL_UNSIGNED_BYTE,
                        rdb[7] );
            }
                        
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private void initTexture( GLAutoDrawable drawable ) {
        
        rawdataBuffer = ByteBuffer.allocate(buffersize);
        rawdata = rawdataBuffer.array();
                
        GL2 gl = drawable.getGL().getGL2();
                
        int [] ids = new int[1];
        gl.glGenTextures( 1, ids, 0 );
        textureID = ids[0];

        gl.glEnable( GL.GL_TEXTURE_2D );
        gl.glActiveTexture( GL.GL_TEXTURE0  );
        gl.glBindTexture( GL.GL_TEXTURE_2D, textureID );
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP );
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP );
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST ); // will be set again elsewhere for demo        
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST ); // will be set again elsewhere for demo 
        gl.glTexEnvf( GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL );//, GL_BLEND ); // GL.GL_MODULATE );         
        
        clearRawBuffer();
        //glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, 3, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, rawdataBuffer );

        gl.glDisable( GL.GL_TEXTURE_2D );
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glColor4f( 1,1,1,1 );
        gl.glEnable( GL.GL_TEXTURE_2D );       
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glTexEnvf( GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE,  GL2.GL_DECAL); //GL.GL_MODULATE );   //   GL.GL_BLEND );                

        if ( setTexture ) {
            if ( whichTexture == 1 ) {
                setChecker1(drawable);
            } else if (whichTexture == 2 ) {
                setChecker2(drawable);
            } else if ( whichTexture == 3 ) {
                setCats( drawable );
            } else if ( whichTexture == 4 ) {
                loadFiles( drawable, "noise" );
            }
            setTexture = false;
        }
        
        String [] magOptions = { 
            "GL_TEXTURE_MAG_FILTER = GL_LINEAR",
            "GL_TEXTURE_MAG_FILTER = GL_NEAREST" 
        };
        
        String text = ""; 
        if ( magoption.getValue() ) {
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            text += magOptions[0] + "\n";
        } else {
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            text += magOptions[1] + "\n";
        }

        String [] minOptionStrs = {
                "GL_NEAREST",
                "GL_LINEAR",               
                "GL_NEAREST_MIPMAP_NEAREST",
                "GL_LINEAR_MIPMAP_NEAREST",
                "GL_NEAREST_MIPMAP_LINEAR",
                "GL_LINEAR_MIPMAP_LINEAR",
        };
        int [] minOptions = {
                GL.GL_NEAREST,
                GL.GL_LINEAR,
                GL.GL_NEAREST_MIPMAP_NEAREST,
                GL.GL_LINEAR_MIPMAP_NEAREST,
                GL.GL_NEAREST_MIPMAP_LINEAR,
                GL.GL_LINEAR_MIPMAP_LINEAR,
        };
        
        int option = minoption.getValue();
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, minOptions[option] );
        text += "GL_TEXTURE_MIN_FILTER = " + minOptionStrs[option];
                    
        if ( gl.glGetString(GL.GL_EXTENSIONS).contains("GL_EXT_texture_filter_anisotropic") ) {
            // do nothing!
        }
        if (anisotropic.getValue() ) {
            float[] fa= new float[1];
            gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, fa, 0 );
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, fa[0]);
            text += "\nGL_TEXTURE_MAX_ANISOTROPY_EXT = 16 (" + fa[0] + " maximum)";
        } else {
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, 1);
            text += "\nGL_TEXTURE_MAX_ANISOTROPY_EXT = 1"; 
        }

            
        
        int h = 2;
        gl.glPushMatrix();
        gl.glScaled( scalex.getValue(), scaley.getValue(), 1 );
        gl.glBegin( GL2.GL_POLYGON );
        gl.glTexCoord2d( 1, 0 ); gl.glVertex3d( -h, -h, 0 );
        gl.glTexCoord2d( 0, 0 ); gl.glVertex3d( -h,  h, 0 );
        gl.glTexCoord2d( 0, 1 ); gl.glVertex3d(  h,  h, 0 );
        gl.glTexCoord2d( 1, 1 ); gl.glVertex3d(  h, -h, 0 );
        gl.glEnd();
        gl.glPopMatrix();
        
        gl.glDisable( GL.GL_TEXTURE_2D );                        

        gl.glEnable( GL2.GL_LIGHTING );
        
        EasyViewer.beginOverlay(drawable);
        EasyViewer.printTextLines(drawable, text);        
        EasyViewer.endOverlay(drawable);
        
    }
    
    GLU glu = new GLU();
    
    private BooleanParameter anisotropic = new BooleanParameter( "anisotropic filtering", false );
    
    private DoubleParameter scalex = new DoubleParameter( "scale x", 1, 0.1, 10 );
    private DoubleParameter scaley = new DoubleParameter( "scale y", 1, 0.1, 10 );
    
    private IntParameter minoption = new IntParameter( "minimization option", 0, 0, 5 );
    
    private BooleanParameter magoption = new BooleanParameter( "Linear Magnification", false );
    
    @Override
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( anisotropic.getControls() );
        vfp.add( scalex.getSliderControls( true ) );
        vfp.add( scaley.getSliderControls( true ) );
        vfp.add( magoption.getControls() );
        vfp.add( minoption.getSliderControls() );
        JTextArea ta = new JTextArea(
        		"1 - build checkerboard programmatically and with gluBuild2DMipmaps (doesn't work)\n"+
        				"2 - load checkerboard from file into mipmap\n"+
        				"3 - load cats from file into mipmap levels\n"+
        				"4 - load noise texture from file into mipmap levels\n"+
        				"RIGHT/LEFT - cycle through minification options\n"+
        				"SPACE/UP/DOWN - toggle minification option\n"+
        				"A - toggle anisotropic filtering");                  
        ta.setEditable(false);
        ta.setBorder( new TitledBorder("Keyboard controls") );
        vfp.add( ta );
        
        return vfp.getPanel();
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        initTexture(drawable);        
    }

    public TextureDemoApp() {
        // do nothing
    }
    
    public static void main(String[] args) {
        TextureDemoApp scene = new TextureDemoApp();
        EasyViewer ev = new EasyViewer("texture demo", scene, new Dimension(640,480), new Dimension(640,480) );
        ev.addInteractor( (Interactor) scene );      
    }

}
