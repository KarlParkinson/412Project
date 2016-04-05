
import java.lang.Math;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;



public class Robot{
	//public static TrackerReader tracker = new TrackerReader();
	EV3LargeRegulatedMotor pan;
	EV3LargeRegulatedMotor tilt1;
	EV3LargeRegulatedMotor tilt2;
	EV3LargeRegulatedMotor grabber;
	EV3UltrasonicSensor dSensor;
	SampleProvider sp;
	float sample[] = {0};
	int[] tracker = {0,0,0,0};
	double distanceThres = 0.04;
	
	private final double h = 120;
	private final double s = 180;
	
	double psi;
	double phi;
	double gammaForward;
	double ettaForward;
	
	boolean ESC;
	
	//private final double k1;
	private final double k2 = 1.45;
	
	double radiansPerTick;
	
	public Robot() {

		//RemoteEV3 brick = new RemoteEV3("10.0.1.1");
		pan = new EV3LargeRegulatedMotor(MotorPort.B);
		tilt1 = new EV3LargeRegulatedMotor(MotorPort.C);
		tilt2 = new EV3LargeRegulatedMotor(MotorPort.D);
		grabber = new EV3LargeRegulatedMotor(MotorPort.A);
		pan.setSpeed(50);
		tilt1.setSpeed(50);
		tilt2.setSpeed(50);
		dSensor = new EV3UltrasonicSensor(SensorPort.S1);
		sp = dSensor.getDistanceMode();
		sample = new float[1];
		
		ESC = false;
		
		double distancePerTick = (Math.PI*0.056)/360;
		double ticksPerRotation = (2*Math.PI*0.05875)/ distancePerTick;
		radiansPerTick = (2*Math.PI)/ ticksPerRotation;
		
	}
	
	public void setInitialAngles() {
		tiltTo(-65);

		psi = (tilt1.getTachoCount() + tilt2.getTachoCount()) / 2.0;
		phi = pan.getTachoCount();
	}
	
	public void approxFeedForwardTerms() {
		try {
			int panAngle2 = 5;
			double errorx = trackerTargetX() - trackerX();
			double gamma1 = Math.toRadians(phi) + Math.atan(errorx/2.79);
			pan.rotateTo((int) phi + panAngle2);
			errorx = trackerTargetX() - trackerX();
			double gamma2 = Math.toRadians(phi + panAngle2) + Math.atan(errorx/2.79);
			pan.rotateTo((int) phi);
			gammaForward = gamma2 - gamma1;
			
			double errory = trackerTargetY() - trackerY();
			double tiltAngle2 = psi + 5;
			double etta1 = Math.toRadians(psi) + Math.atan(errory/2.79);
			tiltTo((int) tiltAngle2);
			errory = trackerTargetY() - trackerY();
			double etta2 = Math.toRadians(tiltAngle2) + Math.atan(errory/2.79);
			tiltTo((int) psi);
			ettaForward = etta2 - etta1;
			
			System.out.println("gammaForward is " + gammaForward);
			System.out.println("ettaForward is " + ettaForward);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.closePorts();
		}
		
	}
	
	public double calcVelocity() {
		double b = h / Math.tan(Math.toRadians(psi));
		double normESquared = Math.pow(b, 2) + Math.pow(s, 2) - 2*b*s*Math.cos(Math.toRadians(180 - phi));
		double alpha = Math.asin((b*Math.sin(Math.toRadians(180 - phi)))/Math.sqrt(normESquared));
		//double velocity = Math.sqrt(normESquared)*Math.cos(alpha)*k1;
		double angularVelocity = k2*alpha;
		System.out.println("Angular velocity is: " + angularVelocity);
		return angularVelocity;
	}
	
	private double calcTachoCount(double angularVelocity) {
		double tachoCount = (2*angularVelocity) / radiansPerTick;
		System.out.println("Tacho Count is: " + tachoCount);
		return tachoCount;
		
	}
	
	public void closePorts(){
		tilt1.close();
		tilt2.close();
		pan.close();
		dSensor.close();
		grabber.close();
	}
	
	public void zeroy() {
		double MAX_ANGLE = 3;
		double errory;
		double kpsi = 0.01;
		errory = trackerTargetY() - trackerY();
		while (Math.abs(errory) > 5 && !this.ESC) {
			sp.fetchSample(sample,0);
			if(sample[0] <= distanceThres){
				break;
			}
			double psiChange = -1*(ettaForward - kpsi*errory) % 360;
			if (Math.abs(psiChange) > MAX_ANGLE) {
				psiChange = MAX_ANGLE * Math.signum(psiChange);
			}
			psi += psiChange;
			tiltTo((int) psi);
			errory = trackerTargetY() - trackerY();
		}
	}
	

	public void zerox() {
		double MAX_ANGLE = 3;
		double kphi = 0.1;
		double errorx = trackerTargetX() - trackerX();
		while (Math.abs(errorx) > 5 && !this.ESC) {
			sp.fetchSample(sample,0);
			if(sample[0] <= distanceThres){
				break;
			}
			double phiChange = -1*(gammaForward - kphi*errorx) % 360;
			if (Math.abs(phiChange) > MAX_ANGLE) {
				phiChange = MAX_ANGLE * Math.signum(phiChange);
			}
			phi += phiChange;
			panTo((int) phi);
			errorx = trackerTargetX() - trackerX();
		}
		
	}
	
	private void tiltTo(int i) {
		tilt1.rotateTo(i, true);//, true);
		tilt2.rotateTo(i);
	}
	
	public void grab() {
		grabber.rotate(-2200);
	}
	
	public void release() {
		grabber.rotate(2200);
	}
	
	public int trackerTargetX(){
		int coord = BluetoothClient.readCoordinates(3);
		System.out.println("");
		return coord;
	}
	
	public int trackerTargetY(){
		int coord = BluetoothClient.readCoordinates(4);
		System.out.println("");
		return coord;
	}
	
	public int trackerX(){
		int coord = BluetoothClient.readCoordinates(1);
		System.out.println("");
		return coord;
	}
	
	public int trackerY(){
		int coord = BluetoothClient.readCoordinates(2);
		System.out.println("");
		return coord;
	}
	
	public void panTo(int degree){
		pan.rotateTo(degree);
	}
	
	public void target() {
		BluetoothClient.sendCommand(6);
		boolean done = false;
		while(!done){
			int command = BluetoothClient.readCommand();
			switch(command) {
			
			case(-1):
				phi -= 3;
				panTo((int)phi);
				break;
			case(1):
				phi += 3;
				panTo((int) phi);
				break;
			case(-2):
				psi -= 3;
				tiltTo((int) psi);
				break;
			case(2):
				psi += 3;
				tiltTo((int) psi);
				break;
			default:
				done = true;
				break;
			}
		}
	}
	
	public void drive() {
		double errorx = trackerTargetX() - trackerX();
		double errory = trackerTargetY() - trackerY();
		double normTrackerError;
		double angularV;
		double tachoCount;
		
		while (!ESC) {
			sp.fetchSample(sample,0);
			System.out.println("Sensor distance measure " + sample[0]);
			if(sample[0] <= distanceThres){
				break;
			}
			errorx = trackerTargetX() - trackerX();
			errory = trackerTargetY() - trackerY();
	
			normTrackerError = Math.sqrt(Math.pow(errorx, 2) + Math.pow(errory, 2));
			if (normTrackerError > 60) {
				BluetoothServer.send(Integer.MAX_VALUE);
				zerox();
				zeroy();
				angularV = calcVelocity();
				tachoCount = calcTachoCount(angularV);
				BluetoothServer.send((int) Math.round(tachoCount));
			} else {
				BluetoothServer.send(Integer.MIN_VALUE);
			}
		}
	}
	
	public static void main(String[] args) {
		//tracker.start();
		
		BluetoothServer.connect();
		BluetoothClient.connect();
		System.out.println("Done connecting to Tablet");
		Robot p = new Robot();
		p.setInitialAngles();
		//double errorx = p.trackerTargetX() - p.trackerX();
		//double errory = p.trackerTargetY() - p.trackerY();
		//double normTrackerError;
		//double angularV;
		//double tachoCount;
		
		//p.zerox();
		//p.zeroy();
		
		p.target();
		p.approxFeedForwardTerms();
		p.drive();
		
		/*
		while (!p.ESC) {
			p.sp.fetchSample(p.sample,0);
			System.out.println("Sensor distance measure " + p.sample[0]);
			if(p.sample[0] <= p.distanceThres){
				break;
			}
			errorx = p.trackerTargetX() - p.trackerX();
			errory = p.trackerTargetY() - p.trackerY();
	
			normTrackerError = Math.sqrt(Math.pow(errorx, 2) + Math.pow(errory, 2));
			if (normTrackerError > 60) {
				BluetoothServer.send(Integer.MAX_VALUE);
				p.zerox();
				p.zeroy();
				angularV = p.calcVelocity();
				tachoCount = p.calcTachoCount(angularV);
				BluetoothServer.send((int) Math.round(tachoCount));
			} else {
				BluetoothServer.send(Integer.MIN_VALUE);
			}
		}
		*/
		
		BluetoothServer.send(Integer.MAX_VALUE);
		p.grab();
		p.target();
		p.approxFeedForwardTerms();
		//Button.waitForAnyPress();
		p.drive();
		BluetoothServer.send(Integer.MAX_VALUE);
		p.release();
		p.closePorts();
		BluetoothServer.disconnect();
		BluetoothClient.disconnect();
		
	}



}
