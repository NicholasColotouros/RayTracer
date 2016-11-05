package comp557.a3;

import java.util.ArrayList;
import java.util.HashMap;
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
 * @author Nicholas Colotouros, 260531370
 */
public class HEDS {

    /**
     * List of faces where each face is defined implicity by a half edge.
     */
    public List<HalfEdge> faces = new ArrayList<HalfEdge>();
    
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
    	// Objective 1: create the half edge data structure from a polygon soup
        
    	// Loop through each face
    	HashMap<String, HalfEdge> twinEdgeMap = new HashMap<String, HalfEdge>();
    	for(int[] face : soup.faceList)
    	{
    		// Loop through each edge in the face to create the twin edges
    		List<HalfEdge> faceEdges = new ArrayList<>();
    		for(int i = 0; i < face.length; i++)
    		{
    			int fromVertexIndex = face[i];
    			int toVertexIndex;
    			
    			// We loop around for the next vertex if there is none
    			if(i != face.length - 1)
    			{
    				toVertexIndex = face[i + 1];
    			} else
    			{
    				toVertexIndex = face[0];
    			}
    			
    			// Create the half edge
    			HalfEdge halfEdge = new HalfEdge();
    			halfEdge.head = soup.vertexList.get(toVertexIndex);
    			faceEdges.add(halfEdge);
    			
    			// Check if we have a corresponding twin. If so set references accordingly
    			// Otherwise add it in to our hash map.
    			String twinKey = "" + toVertexIndex + "," + fromVertexIndex;
    			if(twinEdgeMap.containsKey(twinKey))
    			{
    				HalfEdge twinEdge = twinEdgeMap.get(twinKey);
    				twinEdge.twin = halfEdge;
    				halfEdge.twin = twinEdge;
    			}
    			else
    			{
        			String key = "" + fromVertexIndex + "," + toVertexIndex;
        			twinEdgeMap.put(key, halfEdge);    				
    			}
    		}
    		
    		// Now from our created edges set the next edge so that it loops around 
    		for(int i = 0; i < faceEdges.size(); i++)
    		{
    			int nextHalfEdgeIndex;
    			if(i != faceEdges.size() - 1)
    			{
    				nextHalfEdgeIndex = i + 1;
    			} else
    			{
    				nextHalfEdgeIndex = 0;    				
    			}
				faceEdges.get(i).next = faceEdges.get(nextHalfEdgeIndex);
    		}
    		
    		// Now that we have a face, use one vertex to add it to our list of faces
    		faces.add(faceEdges.get(0));
    	}
    	computeVertexNormals();
    }
    
    // Objective 5: compute surface normal
    public void computeVertexNormals(){
    	// Iterate over every face
    	for(HalfEdge face : faces) {
    		HalfEdge faceEdge = face;
    		
    		// Now check if the normal has been computed in every vertex in that face
    		do{
    			if(faceEdge.head.n == null) calculateNormal(faceEdge);
    			faceEdge = faceEdge.next;
    		} while(faceEdge != face);
        }
    }
    
    // calculates the normal for the head of a given Halfedge
    private void calculateNormal(HalfEdge h){
    	double twoPi = Math.PI * 2.0;
    	ArrayList<Vertex> adjVertices = getAdjacentVertices(h);
    	double numAdjacentVertices = adjVertices.size();
    	Vector3d tangent1 = new Vector3d(0,0,0);
    	Vector3d tangent2 = new Vector3d(0,0,0);
    	
    	// Apply tangent equation as seen in SIGGRAPH 2000 notes equation 4.1 page 70:
    	// Sum from i = 0 to #AdjacentPoints-1: (sin or cos) of (2 pi * i /#AdjacentPoints) * point i,1
    	for(int i = 0; i < numAdjacentVertices; i++) {
    		double iDouble = i;
    		double pointWeight = twoPi * iDouble / numAdjacentVertices;
    		// TODO figure out what the point order is
    		addPointToVector(tangent1, Math.cos(pointWeight), adjVertices.get(i).p);
    		addPointToVector(tangent2, Math.sin(pointWeight), adjVertices.get(i).p);
    	}
    	
    	// Normal = t1 cross t2
    	// Compute it and set it
    	Vector3d normal = new Vector3d();
    	normal.cross(tangent1, tangent2);
    	h.head.n = normal;
    }
    
    private static void addPointToVector(Vector3d pointToAddTo, double numToMultiplyBy, Point3d pointToAdd)
    {
    	pointToAddTo.x += numToMultiplyBy * pointToAdd.x;
    	pointToAddTo.y += numToMultiplyBy * pointToAdd.y;
    	pointToAddTo.z += numToMultiplyBy * pointToAdd.z;
    }
    
    // The order returned is the order needed for computing the Tangents
    private ArrayList<Vertex> getAdjacentVertices(HalfEdge h) {
    	ArrayList<Vertex> ret = new ArrayList<>();
    	HalfEdge edge = h;
    	do {
    		ret.add(edge.next.head);
    		edge = edge.next.twin;
    	} while( edge != h ); // TODO make sure it sorts correctly
    	return ret;
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
