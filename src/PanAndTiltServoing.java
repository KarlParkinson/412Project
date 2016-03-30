import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.Math;

import lejos.hardware.Button;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

public class PanAndTiltServoing extends JPanel {
	
	public static TrackerReader tracker = new TrackerReader();;
	RMIRegulatedMotor pan;
	RMIRegulatedMotor tilt1;
	RMIRegulatedMotor tilt2;
	
	private final double h = 120;
	private final double s = 180;
	
	double psi;
	double phi;
	double gammaForward;
	double ettaForward;
	
	boolean ESC;
	
	//private final double k1;
	private final double k2 = 0.1;
	
	public PanAndTiltServoing() {
		try {
			RemoteEV3 brick = new RemoteEV3("10.0.1.1");
			pan = brick.createRegulatedMotor("B", 'L');
			tilt1 = brick.createRegulatedMotor("C", 'L');
			tilt2 = brick.createRegulatedMotor("D", 'L');
			pan.setSpeed(50);
			tilt1.setSpeed(50);
			tilt2.setSpeed(50);
			ESC = false;
			MouseListener l = new MyMouseListener();
			addMouseListener(l);
			setFocusable(true);
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
	
	public void setInitialAngles() {
		tiltTo(-35);
		try {
			psi = (tilt1.getTachoCount() + tilt2.getTachoCount()) / 2.0;
			phi = pan.getTachoCount();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		}
	}
	
	public void approxFeedForwardTerms() {
		try {
			int panAngle2 = 5;
			double errorx = tracker.targetx - tracker.x;
			double gamma1 = Math.toRadians(phi) + Math.atan(errorx/5.4);
			pan.rotateTo((int) phi + panAngle2);
			errorx = tracker.targetx - tracker.x;
			double gamma2 = Math.toRadians(phi + panAngle2) + Math.atan(errorx/5.4);
			pan.rotateTo((int) phi);
			gammaForward = gamma2 - gamma1;
			
			double errory = tracker.targety - tracker.y;
			double tiltAngle2 = psi + 5;
			double etta1 = Math.toRadians(psi) + Math.atan(errory/5.4);
			tiltTo((int) tiltAngle2);
			errory = tracker.targety - tracker.y;
			double etta2 = Math.toRadians(tiltAngle2) + Math.atan(errory/5.4);
			tiltTo((int) psi);
			ettaForward = etta2 - etta1;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		}
		
	}
	
	public void calcVelocity() {
		System.out.println("phi is: " + phi);
		System.out.println("psi is: " + psi);
		double b = h / Math.tan(Math.toRadians(psi));
		double normESquared = Math.pow(b, 2) + Math.pow(s, 2) - 2*b*s*Math.cos(Math.toRadians(180 - phi));
		double alpha = Math.asin((b*Math.sin(Math.toRadians(180 - phi)))/Math.sqrt(normESquared));
		//double velocity = Math.sqrt(normESquared)*Math.cos(alpha)*k1;
		double angularVelocity = k2*alpha;
		System.out.println("Angular velocity is: " + angularVelocity);
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
	
	
	public void zeroy() {
		double MAX_ANGLE = 3;
		double errory;
		double kpsi = 0.01;
		//int tiltAngle2 = 5;

		//double psi = tilt1.getTachoCount();
		//double tiltAngle2 = psi + 5;
		//double etta1 = Math.toRadians(psi) + Math.atan(errory/5.4);
		//tiltTo((int) tiltAngle2);
		//errory = tracker.targety - tracker.y;
		//double etta2 = Math.toRadians(tiltAngle2) + Math.atan(errory/5.4);
		//tiltTo((int) psi);
		//double ettaForward = etta2 - etta1;
		errory = tracker.targety - tracker.y;
		while (Math.abs(errory) > 1) {
			double psiChange = -1*(ettaForward - kpsi*errory) % 360;
			if (Math.abs(psiChange) > MAX_ANGLE) {
				psiChange = MAX_ANGLE * Math.signum(psiChange);
			}
			//System.out.println(psiChange);
			psi += psiChange;
			tiltTo((int) psi);
			//Delay.msDelay(2000);
			errory = tracker.targety - tracker.y;
			//Delay.msDelay(500); 
		}
	}
	

	public void zerox() {
		double MAX_ANGLE = 3;
		double kphi = 0.1;
		try {
			//double phi = pan.getTachoCount();
			//double gamma1 = Math.toRadians(phi) + Math.atan(errorx/5.4);
			//pan.rotateTo(panAngle2);
			//errorx = tracker.targetx - tracker.x;
			//double gamma2 = Math.toRadians(panAngle2) + Math.atan(errorx/5.4);
			//pan.rotateTo((int) phi);
			//double gammaForward = gamma2 - gamma1;
			double errorx = tracker.targetx - tracker.x;
			while (Math.abs(errorx) > 1) {
				//phi += (gammaForward - kphi*errorx) % 360;
				double phiChange = -1*(gammaForward - kphi*errorx) % 360;
				if (Math.abs(phiChange) > MAX_ANGLE) {
					phiChange = MAX_ANGLE * Math.signum(phiChange);
				}
				//System.out.println(phiChange);
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
	
	public class MyMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent arg0) {
			ESC = true;
			System.out.println("Break");
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			
		}
	}

	
	public static void main(String[] args) {
		tracker.start();
		
		PanAndTiltServoing p = new PanAndTiltServoing();
		p.setInitialAngles();
		Button.waitForAnyPress();
		p.approxFeedForwardTerms();
		//System.out.println(p.gammaForward);
		//System.out.println(p.ettaForward);
		//p.zerox();
		//p.zeroy();
		double errorx = tracker.targetx - tracker.x;
		double errory = tracker.targety - tracker.y;
		double normTrackerError;
		
		JFrame frame = new JFrame("Click here to escape");
		frame.add(p);
		frame.setSize(250, 250);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (!p.ESC) {
			errorx = tracker.targetx - tracker.x;
			errory = tracker.targety - tracker.y;
			normTrackerError = Math.sqrt(Math.pow(errorx, 2) + Math.pow(errory, 2));
			if (normTrackerError > 30) {
				p.zerox();
				p.zeroy();
				p.calcVelocity();
			}
		}
		//Button.waitForAnyPress();
		//p.tiltTo(-45);
		//Button.waitForAnyPress();
		//while(true) {
		//	System.out.println(tracker.targetx - tracker.x);
		//}
		//p.zerox();
		//System.out.println("Done x");
		//Button.waitForAnyPress();
		//Delay.msDelay(2000);
		//p.zeroy();
		//System.out.println("Done");
		//Button.waitForAnyPress();
		p.closePorts();
		//System.out.println("Exiting");
	}

}
