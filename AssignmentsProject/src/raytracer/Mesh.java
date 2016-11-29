package raytracer;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import raytracer.PolygonSoup.Vertex;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	// Used for computing the bounding box for quick intersection
	private Box boundingBox;
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}
	
	public void initialoizeBoundingBox() {
		Point3d min = new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point3d max = new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for(Vertex v : soup.vertexList) {
			Point3d p = v.p;
			
			// Check for min
			if(p.x < min.x)
				min.x = p.x;
			if(p.y < min.y)
				min.y = p.y;
			if(p.z < min.z)
				min.z = p.z;
			
			// Check for max
			if(p.x > max.x)
				max.x = p.x;
			if(p.y > max.y)
				max.y = p.y;
			if(p.z > max.z)
				max.z = p.z;
		}
		
		boundingBox = new Box(min, max);
	}
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// First check the bounding box to see if it's worth computing
		IntersectResult tempResult = new IntersectResult(result);
		boundingBox.intersect(ray, tempResult);
		if(Double.isInfinite(tempResult.t) || tempResult.t > result.t)
			return;
		
		
		// Now check the actual triangles
		for(int[] vertices : soup.faceList)
		{
			// Don't want the program to crash if it's not a triangular mesh
			if(vertices.length != 3)
				continue;
			
			final Point3d p1 = soup.vertexList.get(vertices[0]).p;
			final Point3d p2 = soup.vertexList.get(vertices[1]).p;
			final Point3d p3 = soup.vertexList.get(vertices[2]).p;
			
			final Vector3d v1 = new Vector3d();
			v1.sub(p2, p1);
			final Vector3d v2 = new Vector3d();
			v2.sub(p3, p1);

			// Figure out which normal is pointing towards the eye
			Vector3d normal1 = new Vector3d();
			Vector3d normal2 = new Vector3d();
			normal1.cross(v1, v2);
			normal2.cross(v2, v1);
			
			Vector3d planeNormal;
			if( normal1.dot(ray.viewDirection) < normal2.dot(ray.viewDirection))
				planeNormal = normal1;
			else
				planeNormal = normal2;

			// Find t and the point it corresponds to on the plane
			Vector3d p1MinusEye = new Vector3d();
			p1MinusEye.sub(p1, ray.eyePoint);
			double t = p1MinusEye.dot(planeNormal) / ray.viewDirection.dot(planeNormal);
			
			// stop if what we intersected is further than our closest result
			if (t >= result.t || t <= 0)
				continue;
			
			Point3d p = new Point3d();
			ray.getPoint(t, p);
			
			// https://www.cs.princeton.edu/courses/archive/fall00/cs426/lectures/raycast/sld018.htm
			// Check if the point is in the triangle
			// If it is, we have a closer intersection
			Vector3d w1 = new Vector3d();
			w1.sub(p2, p1);
			Vector3d w2 = new Vector3d();
			w2.sub(p3, p2);
			Vector3d w3 = new Vector3d();
			w3.sub(p1, p3);
			
			Vector3d u1 = new Vector3d();
			u1.sub(p, p1);
			Vector3d u2 = new Vector3d();
			u2.sub(p, p2);
			Vector3d u3 = new Vector3d();
			u3.sub(p, p3);
			
			Vector3d x1 = new Vector3d();
			x1.cross(w1, u1);
			Vector3d x2 = new Vector3d();
			x2.cross(w2, u2);
			Vector3d x3 = new Vector3d();
			x3.cross(w3, u3);
			
			if (x1.dot(planeNormal) > 0 && x2.dot(planeNormal) > 0 && x3.dot(planeNormal) > 0) {
				result.t = t;
				result.n = planeNormal;
				result.p = p;
				result.n.normalize();
				result.material = material;
			}
		}
	}
}
