package comp557.a3;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

/**
 * Class implementing the Loop subdivision scheme     
 * 
 * @author Nicholas Colotouros, 260531370
 */
public class Loop {

    /**
     * Subdivides the provided half edge data structure
     * @param heds the mesh to subdivide
     * @return the subdivided mesh
     */
    public static HEDS subdivide( HEDS heds ) {
    	HEDS heds2 = new HEDS();
        List<HalfEdge> faces = heds.faces;
        
        setEvenVertices(faces); // Creates new vertices based on existing vertices
        setOddVertices(faces); // Creates vertices that go between the edges and connects them to their parents
        
        // Adds the additional edges to be made that make the triforce pattern and connects the faces
        heds2.faces = createInnerFaces(faces);
        heds2.computeVertexNormals();
        // TODO make sure normals are computed
        
        return heds2;        
    }
    
	// Goes through each vertex of each face and calculates it's child if it hasn't been set yet.
    private static void setEvenVertices(List<HalfEdge> halfEdges) {
    	for(HalfEdge startingFaceEdge : halfEdges) {
    		HalfEdge currentFace = startingFaceEdge;
    		do{
    			if(currentFace.head.child == null) setEvenVertex(currentFace);
    			currentFace = currentFace.next;
    		} while(startingFaceEdge != currentFace);
        }
    }
    
    
    // Computes the child vertex if it has not yet been computed
    private static void setEvenVertex(HalfEdge h) {
    	Point3d childPoint = new Point3d(0, 0, 0);
    	
    	// Calculate the center point's weight using 1 - nB
    	int numAdjacentVertices = getNumAdjacentVertices(h);
    	double beta = computeBeta(numAdjacentVertices);
    	addPointToPoint(childPoint, 1.0 - numAdjacentVertices * beta, h.head.p); // Even edge weighted addition
    	
    	// Now go through all adjacent points and add them with weight beta
    	HalfEdge currentEdge = h;
    	do {
    		currentEdge = currentEdge.next;
    		addPointToPoint(childPoint, beta, currentEdge.head.p);
    		currentEdge = currentEdge.twin;
    	} while(currentEdge != h);
    	
    	// Set the vertex
    	Vertex child = new Vertex();
    	child.p = childPoint;
    	h.head.child = child;
    }
    
    // Number of unique vertices that are connected to the vertex of the half edge
    // Needed to calculate beta
    private static int getNumAdjacentVertices(HalfEdge h)
    {
    	HalfEdge edge = h;
    	int n = 0;
    	do {
    		edge = edge.next.twin;
    		n++;
    	} while( edge != h );
    	return n;
    }
    
    // Computes the beta for the even vertex
    // using the rules proposed by Warren
    private static double computeBeta(double n) {
    	if(n > 3) return 3.0 / (8.0 * n);
    	else return 3.0/16.0;
    }
    
    // Set child1, child2 of each half edge with a new half edge.
    // The new half edges will have their parents and twin
    private static void setOddVertices(List<HalfEdge> halfEdges) {
    	for(HalfEdge startingFaceEdge : halfEdges) {
    		HalfEdge currentFace = startingFaceEdge;
    		do{
    			if(currentFace.child1 == null && currentFace.child2 == null) setOddVertex(currentFace);
    			currentFace = currentFace.next;
    		} while(startingFaceEdge != currentFace);
        }
    }
    
    private static void setOddVertex(HalfEdge h) {
    	Point3d midPoint = new Point3d(0, 0, 0);
    	
    	// Thanks to the half edge we have the line going across so let's add that immediately
    	addPointToPoint(midPoint, 3.0/8.0, h.head.p);
    	addPointToPoint(midPoint, 3.0/8.0, h.prev().head.p);
    	
    	// Now add in the other two points from the current face and the twin
    	addPointToPoint(midPoint, 1.0/8.0, h.next.head.p);
    	addPointToPoint(midPoint, 1.0/8.0, h.twin.next.head.p);
    	
    	// Create the vertex and half edges
    	Vertex midVertex = new Vertex();
    	midVertex.p = midPoint;
    	
    	HalfEdge leftEdge = new HalfEdge();
    	HalfEdge rightEdge = new HalfEdge();
    	
    	leftEdge.head = midVertex;
    	rightEdge.head = h.head.child;
    	
    	leftEdge.parent = h;
    	rightEdge.parent = h;
    	
    	h.child1 = leftEdge;
    	h.child2 = rightEdge;
    	
    	
    	// Create the twins of the two new half edges
    	HalfEdge twinParent = h.twin;
    	HalfEdge twinLeftEdge = new HalfEdge();
    	HalfEdge twinRightEdge = new HalfEdge();
    	
    	twinLeftEdge.head = midVertex;
    	twinRightEdge.head = twinParent.head.child;
    	
    	twinLeftEdge.parent = twinParent;
    	twinRightEdge.parent = twinParent;
    	
    	twinParent.child1 = twinLeftEdge;
    	twinParent.child2 = twinRightEdge;
    	
    	// Set the twins in the new 4 child edges
    	leftEdge.twin = twinRightEdge;
    	twinRightEdge.twin = leftEdge;
    	
    	rightEdge.twin = twinLeftEdge;
    	twinLeftEdge.twin = rightEdge;
    }
    
    // Go through each face and find the newly created vertexes
    // Returns the list of faces created by the subdivision
    private static ArrayList<HalfEdge> createInnerFaces(List<HalfEdge> halfEdges) {
    	ArrayList<HalfEdge> faces = new ArrayList<HalfEdge>();
    	
    	// Iterate over each edge in the parent face
    	for(HalfEdge faceEdge : halfEdges){
    		
    		// Now iterate over every edge in the face
    		// We look at edges two at a time so that we can create the innter edge
    		HalfEdge edge = faceEdge;
    		HalfEdge previousInnerEdge = null;
    		HalfEdge firstInnerEdge = null;
    		do{
        		HalfEdge nextEdge = edge.next;

    			Vertex edgeSubVertex = edge.child1.head;
    			Vertex nextEdgeSubVertex = nextEdge.child1.head;
    			
    			HalfEdge outerFaceCompletingEdge = new HalfEdge(); // the edge that will complete the subface
    			HalfEdge innerFaceEdge = new HalfEdge(); // the twin of the above
    			
    			outerFaceCompletingEdge.head = edgeSubVertex;
    			innerFaceEdge.head = nextEdgeSubVertex;
    			
    			outerFaceCompletingEdge.twin = innerFaceEdge;
    			innerFaceEdge.twin = outerFaceCompletingEdge;
    			
    			
    			
    			
    			// Complete the current face
    			outerFaceCompletingEdge.next = edge.child2;
    			edge.child2.next = nextEdge.child1;
    			nextEdge.child1.next = outerFaceCompletingEdge;
    			faces.add(edge.child2);
    			
    			
    			// Set the previous inner edge to be the new one
    			if(previousInnerEdge != null) {
    				previousInnerEdge.next = innerFaceEdge;
    			}
    			else {
    				firstInnerEdge = innerFaceEdge;
    			}
    			
    			// prepare for the next iteration
    			edge = nextEdge;
    			previousInnerEdge = innerFaceEdge;
    			
    			// Final iteration: complete the inner triangle
    			if(edge == faceEdge){
    				innerFaceEdge.next = firstInnerEdge;
    				faces.add(firstInnerEdge);
    			}
    		} while(edge != faceEdge);
    	}
    	return faces;
    }
            
    public static void addPointToPoint(Point3d pointToAddTo, double numToMultiplyBy, Point3d pointToAdd)
    {
    	pointToAddTo.x += numToMultiplyBy * pointToAdd.x;
    	pointToAddTo.y += numToMultiplyBy * pointToAdd.y;
    	pointToAddTo.z += numToMultiplyBy * pointToAdd.z;
    }
}
