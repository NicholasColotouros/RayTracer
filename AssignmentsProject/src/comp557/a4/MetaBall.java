package comp557.a4;

import java.util.List;

import javax.vecmath.Point3d;

public class MetaBall extends Intersectable{

	private List<Sphere> spheres;
	private double scalingFactor; // a
	private double maxContributingDistance; // b
	
	public MetaBall(Sphere s) {
		Sphere s2 = new Sphere();
		s2.material = s.material;
		s2.center = new Point3d(0, 2, 0);
		s2.radius =s.radius;
		
		Sphere s3 = new Sphere();
		s3.material = s.material;
		s3.center = new Point3d(2, 0, 0);
		s3.radius =s.radius;
		
		spheres.add(s);
		spheres.add(s2);
		spheres.add(s3);
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO Auto-generated method stub
		
	}

}
