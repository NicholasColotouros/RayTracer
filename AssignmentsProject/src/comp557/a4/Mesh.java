package comp557.a4;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 7: Bonus: finish this class as a bonus objective
		// Based on https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
		double t = Double.NEGATIVE_INFINITY;
		Vector3d n;
		
		for(int[] face : soup.faceList) {
			Point3d vertex1 = soup.vertexList.get(face[0]).p;
			Point3d vertex2 = soup.vertexList.get(face[1]).p;
			Point3d vertex3 = soup.vertexList.get(face[2]).p;
			
			// TODO am i subbing the right way?
			Vector3d edge1 = new Vector3d(
										vertex1.x - vertex2.x,
										vertex1.y - vertex2.y,
										vertex1.z - vertex2.z 
									);
			Vector3d edge2 = new Vector3d(
										vertex1.x - vertex3.x,
										vertex1.y - vertex3.y,
										vertex1.z - vertex3.z 
									);
			
			Vector3d p = new Vector3d();
			p.cross(ray.viewDirection, edge2);
			
			double determinant = edge1.dot(p);
			
			// Don't care if it's the back face
			if(determinant > -EPSILON && determinant < EPSILON) 
				continue;
			double invDeterminant = 1.0/determinant;
			Vector3d asdf = new Vector3d();
			asdf.sub(ray.eyePoint, vertex1);
			
			double u = asdf.dot(p) * invDeterminant;
			
			// The intersection lies outside of the triangle
			if(u < 0.f || u > 1.f) 
				continue;
			Vector3d q = new Vector3d();
			q.cross(asdf, edge1);
			double v = ray.viewDirection.dot(q) * invDeterminant;
			
			//The intersection lies outside of the triangle
			if(v < 0.f || u + v  > 1.f) return;
			
			double potentialT = edge2.dot(q) * invDeterminant;
			if (potentialT > EPSILON && potentialT < t) {
				t = potentialT;
			}
		}
		
		if (t != Double.NEGATIVE_INFINITY ) {
			result.t = t;
			result.p = new Point3d();
			ray.getPoint(t, result.p);
			result.material = material;
			result.n = new Vector3d();
		}
	}

}
