package comp557.a1;

import comp557.a1.joints.*;
import comp557.a1.shapes.*;

public class CharacterCreator {

	static public String name = "Pikachu - Nicholas Colotouros 260531370";
	
	/** 
	 * Creates a character.
	 * @return root DAGNode
	 */
	static public DAGNode create() {
		FreeJoint bodyBase = new FreeJoint();
		DAGNode head = createHead(0, 5, 0);
		DAGNode body = createBody();
		
		BallJoint upperBodyJoint = new BallJoint("upper body", 0, 0, 0, 0, 0, 0);
		upperBodyJoint.setMinimumAngle(-15, -30, -10);
		upperBodyJoint.setMaximumAngle(30, 30, 10);
		
		DAGNode legs = createLowerBody(0,-2.5,0);

		bodyBase.add(upperBodyJoint);
		bodyBase.add(legs);
		upperBodyJoint.add(body);
		body.add(head);
		
		return bodyBase;
	}
	
	static private DAGNode createLeg(double x, double y, double z, boolean isRightLeg){
		String legName = "right leg";
		if(!isRightLeg) legName = "left leg";
		
		BallJoint buttCheek = new BallJoint(legName, x, y, z, 0,0,0);
		
		Cylinder thigh = new Cylinder(0.65, 2);
		thigh.SetRotation(90, 0, 0);
		thigh.SetColor(1, 1, 0);
		
		HingeJoint knee = new HingeJoint("knee", 0, 0, 2, 1,0,0, 0,0,120);
		Ellipse kneeCap = new Ellipse(0.7, 0.7, 0,0,0, 0.7f, 0.7f, 0f, false);

		Cylinder shin = new Cylinder(0.6, 2);
		shin.SetColor(1, 1, 0);

		BallJoint heel = new BallJoint("heel", 0,0,2, 0,0,0);
		Ellipse foot = new Ellipse(0.8,1.5, 0,0.5,0.5, 0.7f, 0.7f, 0f, false);
		heel.setMinimumAngle(-30, 0, -20);
		heel.setMaximumAngle(60, 0, 20);			
		
		// set the joint maximums, the branch is because of the direction in which the rotation
		// happens is different depending on the left or right leg
		if(isRightLeg){
			buttCheek.setMinimumAngle(-90, 0, 0);
			buttCheek.setMaximumAngle(120, 0, 90);
		}
		else{
			buttCheek.setMinimumAngle(-90, 0, -90);
			buttCheek.setMaximumAngle(120, 0, 15);
		}
		
		// Construct leg
		buttCheek.add(thigh);
		thigh.add(knee);
		knee.add(kneeCap);
		knee.add(shin);
		shin.add(heel);
		heel.add(foot);
		return buttCheek;
	}
	
	static private DAGNode createLowerBody(double x, double y, double z){
		Ellipse pelvis = new Ellipse(3,3, x,y,z, 1f,1f,0f, false);
		DAGNode leftLeg = createLeg(-1.25,-2.5,0, false);
		DAGNode rightLeg = createLeg(1.25,-2.5,0, true);
		
		pelvis.add(leftLeg);
		pelvis.add(rightLeg);
		return pelvis;
	}
	
	// returns an arm drawn at x, y, z without any min/max angles set
	static private DAGNode createArm(double x, double y, double z, boolean isRightArm){
		String armName = "right arm";
		if(!isRightArm) armName = "left arm";
		
		BallJoint shoulder = new BallJoint(armName,x, y, z, 0,0,0);
		Ellipse shoulderBall = new Ellipse(0.7, 0.7, 0,0,0, 0.7f, 0.7f, 0f, false);
		
		Cylinder bicep = new Cylinder(0.65, 2);
		bicep.SetColor(1, 1, 0);
		
		HingeJoint elbow = new HingeJoint("elbow",0, 0, 2, 1,0,0, 0,-120,0);
		Ellipse elbowBall = new Ellipse(0.7, 0.7, 0,0,0, 0.7f, 0.7f, 0f, false);
		
		Cylinder forarm = new Cylinder(0.6, 2);
		forarm.SetColor(1, 1, 0);
		
		BallJoint wrist = new BallJoint("wrist",0,0,2, 0,0,0);
		Ellipse hand = new Ellipse(0.5,1, 0,0,0.5, 0.7f, 0.7f, 0f, false);
		hand.SetRotation(90, 0, 0);
		
		// set the joint maximums, the branch is because of the direction in which the rotation
		// happens is different depending on the left or right arm
		if(isRightArm){
			shoulder.setMinimumAngle(-90, 0, 0);
			shoulder.setMaximumAngle(120, 90, 120);
			
			wrist.setMinimumAngle(-90, 0, 0);
			wrist.setMaximumAngle(90, 90, 90);			
		}
		else{
			shoulder.setMinimumAngle(-90, -90, -120);
			shoulder.setMaximumAngle(120, 0, 0);

			wrist.setMinimumAngle(-90, -90, -90);
			wrist.setMaximumAngle(90, 0, 0);
		}
		
		// Construct arm
		shoulder.add(bicep);
		shoulder.add(shoulderBall);
		bicep.add(elbow);
		elbow.add(elbowBall);
		elbow.add(forarm);
		forarm.add(wrist);
		wrist.add(hand);
		
		return shoulder;
	}
	
	static private DAGNode createBody(){
		Ellipse body = new Ellipse(3,5,0,0,0,1,1,0, false);
		Ellipse topStripe = new Ellipse(2.2,0.7, 0,1.25,-1, 0.5f,0.2f,0.2f, false);
		Ellipse bottomStripe = new Ellipse(2.2,0.7, 0,-1.25,-1, 0.5f,0.2f,0.2f, false);
		DAGNode tail = createTail(0, -3, -2);
		DAGNode leftShoulder = createArm(-2.5, 1, 1, false);
		DAGNode rightShoulder = createArm(2.5,1,1, true);
		
		body.add(topStripe);
		body.add(bottomStripe);
		body.add(tail);
		body.add(leftShoulder);
		body.add(rightShoulder);
		
		return body;
	}
	
	static private DAGNode createTail(double x, double y, double z){
		BallJoint tailBase = new BallJoint("tail",x, y, z, 230, 0, 0);
		tailBase.setMinimumAngle(175, -45, 0);
		tailBase.setMaximumAngle(250, 90, 0);
		
		Cylinder tailPiece1 = new Cylinder(0.5, 1, 0,0,0, 0.5f,0.2f,0.2f, false);
		tailPiece1.SetRotation(0, -45, 0);
		Cylinder tailPiece2 = new Cylinder(0.5, 1.5, 0,0,1, 0.5f,0.2f,0.2f, false);
		tailPiece2.SetRotation(0, 45, 0);
		Cylinder tailPiece3 = new Cylinder(0.5, 2, 0,0,1.5, 0.5f,0.2f,0.2f, false);
		tailPiece3.SetRotation(0, -45, 0);
		Cylinder tailPiece4 = new Cylinder(0.5, 2.5, 0,0,1.75, 1,1,0, false);
		tailPiece4.SetRotation(0, 45, 0);
		Cylinder tailPiece5 = new Cylinder(0.5, 3, 0,0,2, 1,1,0, false);
		tailPiece5.SetRotation(0, -45, 0);
		
		tailBase.add(tailPiece1);
		tailPiece1.add(tailPiece2);
		tailPiece2.add(tailPiece3);
		tailPiece3.add(tailPiece4);
		tailPiece4.add(tailPiece5);
		
		return tailBase;
	}
	
	static private DAGNode createHead(double x, double y, double z){
		
		// Create head, cheeks and mouth
		Ellipse head = new Ellipse(3, 3, x, y, z, 1, 1, 0, false);
		
		Ellipse leftcheek = new Ellipse(0.75,0.75, 2,-0.75,1.5, 1,0,0, false);
		Ellipse rightcheek = new Ellipse(0.75,0.75, -2,-0.75,1.5, 1,0,0, false);
		Ellipse mouth = new Ellipse(0.75, 0.75, 0, -0.75, 2.5, 0,0,0, true);
		
		head.add(leftcheek);
		head.add(rightcheek);
		head.add(mouth);
		
		// Add eyes with pupils that can be articulated
		double eyeXMin = -25;
		double eyeXMax = 30;
		
		double eyeYMin = -15;
		double eyeYMax = 30;
				
		Ellipse lefteye = new Ellipse(0.75,0.75, 1.25,0.5,2.25, 1,1,1, false);
		BallJoint leftEyeSocket = new BallJoint("left eye",0, 0, 0, 0, 0, 0);
		leftEyeSocket.setMinimumAngle(eyeXMin, eyeYMin, z);
		leftEyeSocket.setMaximumAngle(eyeXMax, eyeYMax, z);
		
		Ellipse leftPupil = new Ellipse(0.15, 0.15, 0,0,0.7, 0,0,0, true);
		head.add(lefteye);
		lefteye.add(leftEyeSocket);
		leftEyeSocket.add(leftPupil);
		
		Ellipse rightEye = new Ellipse(0.75,0.75, -1.25,0.5,2.25, 1,1,1, false);
		BallJoint rightEyeSocket = new BallJoint("right eye",0, 0, 0, 0, 0, 0);
		rightEyeSocket.setMinimumAngle(eyeXMin, eyeYMax * -1, z);
		rightEyeSocket.setMaximumAngle(eyeXMax, eyeYMin * -1, z);
		Ellipse rightPupil = new Ellipse(0.15, 0.15, 0,0,0.7, 0,0,0, true);
		head.add(rightEye);
		rightEye.add(rightEyeSocket);
		rightEyeSocket.add(rightPupil);		
		
		// Add the ears
		BallJoint leftEarBase = new BallJoint("left ear",1.25, 2, 0, 270,30,0);
		leftEarBase.xRot.setMinimum(150.0);
		leftEarBase.xRot.setMaximum(315.0);
		leftEarBase.yRot.setMinimum(0.0);
		leftEarBase.yRot.setMaximum(130.0);
		leftEarBase.zRot.setMinimum(0.0);
		leftEarBase.zRot.setMaximum(0.0);
		
		
		Cone leftEarYellow = new Cone(0.75,5,0,0,0,1,1,0, false);
		Cone leftEarBlack = new Cone(0.28, 2.5, 0,0, 3, 0,0,0, false);
		head.add(leftEarBase);
		leftEarBase.add(leftEarYellow);
		leftEarBase.add(leftEarBlack);
		
		
		BallJoint rightEarBase = new BallJoint("right ear",-1.25, 2, 0, 270,330,0);
		rightEarBase.xRot.setMinimum(150.0);
		rightEarBase.xRot.setMaximum(315.0);
		rightEarBase.yRot.setMinimum(230.0);
		rightEarBase.yRot.setMaximum(360.0);
		rightEarBase.zRot.setMinimum(0.0);
		rightEarBase.zRot.setMaximum(0.0);
		
		
		Cone rightEarYellow = new Cone(0.75,5,0,0,0,1,1,0, false);
		Cone rightEarBlack = new Cone(0.28, 2.5, 0,0, 3, 0,0,0, false);
		head.add(rightEarBase);
		rightEarBase.add(rightEarYellow);
		rightEarBase.add(rightEarBlack);
		
		// Now create the neck joint
		BallJoint neck = new BallJoint("neck",0, 0, 0, 0, 0, 0);
		neck.setMaximumAngle(45, 45, 15);
		neck.setMinimumAngle(-15, -45, -15);
		
		neck.add(head);
		return neck;
	}	
}
