package comp557.a3;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import comp557.a3.HalfEdge;

/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow for easy display of geometry.
 * 
 * @author TODO: YOUR NAME AND STUDENT NUMBER HERE
 */
public class HEDS {

    /**
     * List of faces where each face is defined implicity by a half edge.
     */
    List<HalfEdge> faces = new ArrayList<HalfEdge>();
    
    /**
     * Constructs an empty mesh (used when building a mesh with subdivision)
     */
    public HEDS() {
        // do nothing
    }
        
    /**
     * Builds a half edge data structure from the polygon soup   
     * @param soup
     */
    public HEDS( PolygonSoup soup ) {
        
        
        
        
        // TODO: Objective 1: create the half edge data structure from a polygon soup
        
        
        
        
    }
    
    /** Should delete these properly on finalize! */
    private int[] displayListID = null;
    
    /**
     * Draws the half edge data structure by drawing each of its faces.
     * Per vertex normals are used to draw the smooth surface when available,
     * otherwise a face normal is computed. 
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        if ( displayListID != null ) {
        	gl.glCallList( displayListID[0] );
        } else {
        	displayListID = new int[] { gl.glGenLists(1) };
        	gl.glNewList( displayListID[0], GL2.GL_COMPILE_AND_EXECUTE );

	        // assume triangular faces (we're doing loop after all!
	        Vector3d v1 = new Vector3d();
	        Vector3d v2 = new Vector3d();
	        Vector3d n = new Vector3d();
	        gl.glBegin( GL.GL_TRIANGLES );
	        for ( HalfEdge he: faces ) {
	            Point3d p0 = he.head.p;
	            Point3d p1 = he.next.head.p;
	            Point3d p2 = he.next.next.head.p;
	            if ( he.head.n == null ) {
	                v1.sub( p1,p0 );
	                v2.sub( p2,p1 );
	                n.cross( v1, v2 );            
	                gl.glNormal3d( n.x, n.y, n.z );
	                gl.glVertex3d( p0.x, p0.y, p0.z );
	                gl.glVertex3d( p1.x, p1.y, p1.z );
	                gl.glVertex3d( p2.x, p2.y, p2.z );
	            } else {
	                Vector3d n0 = he.head.n;
	                Vector3d n1 = he.next.head.n;
	                Vector3d n2 = he.next.next.head.n;
	                gl.glNormal3d( n0.x, n0.y, n0.z );
	                gl.glVertex3d( p0.x, p0.y, p0.z );
	                gl.glNormal3d( n1.x, n1.y, n1.z );
	                gl.glVertex3d( p1.x, p1.y, p1.z );
	                gl.glNormal3d( n2.x, n2.y, n2.z );
	                gl.glVertex3d( p2.x, p2.y, p2.z );
	            }
	        }
	        gl.glEnd();
	     
		    gl.glEndList();
        }
    }
    
    /** 
     * Draws all child vertices to help with debugging and evaluation.
     * (this will draw each points multiple times)
     * @param drawable
     */
    public void drawChildVertices( GLAutoDrawable drawable ) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glDisable( GL2.GL_LIGHTING );
        gl.glPointSize(3);
        gl.glBegin( GL.GL_POINTS );
        for ( HalfEdge he: faces ) {
            HalfEdge loop = he;
            do {
                if ( loop.head.child != null ) {
                    Point3d p = loop.head.child.p;
                    gl.glColor3f(1,0,0);
                    gl.glVertex3d( p.x, p.y, p.z );
                }
                if ( loop.child1 != null && loop.child1.head != null ) {
                    Point3d p = loop.child1.head.p;
                    gl.glColor3f(0,1,0);
                    gl.glVertex3d( p.x, p.y, p.z );
                }
                loop = loop.next;
            } while ( loop != he );
        }
        gl.glEnd();
        gl.glEnable( GL2.GL_LIGHTING );
    }
}
