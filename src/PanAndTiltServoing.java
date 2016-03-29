import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.lang.Math;

import lejos.hardware.Button;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

public class PanAndTiltServoing {
	
	public static TrackerReader tracker;
	RMIRegulatedMotor pan;
	RMIRegulatedMotor tilt1;
	RMIRegulatedMotor tilt2;
	
	public PanAndTiltServoing() {
		try {
			RemoteEV3 brick = new RemoteEV3("10.0.1.1");
			pan = brick.createRegulatedMotor("B", 'L');
			tilt1 = brick.createRegulatedMotor("C", 'L');
			tilt2 = brick.createRegulatedMotor("D", 'L');
			pan.setSpeed(50);
			tilt1.setSpeed(50);
			tilt2.setSpeed(50);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		}
	}
	
	public void calcVelocity() {
		double b = h / Math.tan(psi);
		double normESquared = Math.pow(b, 2) + Math.pow(s, 2) - 2*b*s*Math.cos(180 - phi);
		double alpha = Math.asin((b*Math.sin(180 - phi))/Math.sqrt(normESquared));
		double velocity = Math.sqrt(normESquared)*Math.cos(alpha)*k1;
		double angularVelocity = k2*alpha;
		
	}
	
	public void closePorts(){
		try {
			tilt1.close();
			tilt2.close();
			pan.close();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		}
	}
	
	
	public void zeroy(TrackerReader tracker) {
		double MAX_ANGLE = 3;
		double errory = tracker.targety - tracker.y;
		double kpsi = 0.01;
		//int tiltAngle2 = 5;
		
		try {
			double psi = tilt1.getTachoCount();
			double tiltAngle2 = psi + 5;
			double etta1 = Math.toRadians(psi) + Math.atan(errory/5.4);
			tiltTo((int) tiltAngle2);
			errory = tracker.targety - tracker.y;
			double etta2 = Math.toRadians(tiltAngle2) + Math.atan(errory/5.4);
			tiltTo((int) psi);
			double ettaForward = etta2 - etta1;
			errory = tracker.targety - tracker.y;
			while (Math.abs(errory) > 1) {
				double psiChange = (ettaForward - kpsi*errory) % 360;
				if (Math.abs(psiChange) > MAX_ANGLE) {
					psiChange = MAX_ANGLE * Math.signum(psiChange);
				}
				System.out.println(psiChange);
				psi += psiChange;
				tiltTo((int) psi);
				//Delay.msDelay(2000);
				errory = tracker.targety - tracker.y;
				//Delay.msDelay(500);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	

	public void zerox(TrackerReader tracker) {
		double MAX_ANGLE = 3;
		double errorx = tracker.targetx - tracker.x;
		double kphi = 0.1;
		int panAngle2 = 5;
		try {
			double phi = pan.getTachoCount();
			double gamma1 = Math.toRadians(phi) + Math.atan(errorx/5.4);
			pan.rotateTo(panAngle2);
			errorx = tracker.targetx - tracker.x;
			double gamma2 = Math.toRadians(panAngle2) + Math.atan(errorx/5.4);
			pan.rotateTo((int) phi);
			double gammaForward = gamma2 - gamma1;
			errorx = tracker.targetx - tracker.x;
			while (Math.abs(errorx) > 1) {
				//phi += (gammaForward - kphi*errorx) % 360;
				double phiChange = -1*(gammaForward - kphi*errorx) % 360;
				if (Math.abs(phiChange) > MAX_ANGLE) {
					phiChange = MAX_ANGLE * Math.signum(phiChange);
				}
				System.out.println(phiChange);
				phi += phiChange;
				pan.rotateTo((int) phi);
				Delay.msDelay(100);
				errorx = tracker.targetx - tracker.x;
				//Delay.msDelay(2000);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void tiltTo(int i) {
		try {
			tilt1.rotateTo(i, true);//, true);
			tilt2.rotateTo(i);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closePorts();
		}
		
		
	}
	
	public static void main(String[] args) {
		TrackerReader tracker = new TrackerReader();
		tracker.start();
		
		PanAndTiltServoing p = new PanAndTiltServoing();
		//Button.waitForAnyPress();
		p.tiltTo(-45);
		Button.waitForAnyPress();
		//while(true) {
		//	System.out.println(tracker.targetx - tracker.x);
		//}
		p.zerox(tracker);
		System.out.println("Done x");
		//Button.waitForAnyPress();
		Delay.msDelay(2000);
		p.zeroy(tracker);
		System.out.println("Done");
		Button.waitForAnyPress();
		p.closePorts();
		System.out.println("Exiting");
	}

}
