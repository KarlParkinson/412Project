import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.SampleProvider;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;

public class Vehicle {
	
	RMIRegulatedMotor left;
	RMIRegulatedMotor right;
	
	EV3GyroSensor gyro;
	SampleProvider sp;
	
	public Vehicle() {
		RemoteEV3 brick;
		try {
			brick = new RemoteEV3("10.0.1.1");
			left = brick.createRegulatedMotor("A", 'L');
			right = brick.createRegulatedMotor("D", 'L');
			
			//gyro = new EV3GyroSensor(brick.getPort(SensorPort.S3.getName()));
			//sp = gyro.getAngleMode();
			
			left.setSpeed(90);
			right.setSpeed(90);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			closePorts();
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			closePorts();
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			closePorts();
			e.printStackTrace();
		}
	}
	
	public void closePorts() {
		try {
			left.close();
			right.close();
			//gyro.close();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void turnLeft(int tachoCount) {
		try {
			float sample[] = {0};
			System.out.println("here");
			//sp.fetchSample(sample, 0);
			//System.out.println(sample[0]);
			int current = left.getTachoCount();
			int target = current + tachoCount;
			while (left.getTachoCount() < target) {
				System.out.println("in loop");
				left.forward();
			}
			left.stop(false);
			//sp.fetchSample(sample, 0);
			//System.out.println(sample[0]);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			closePorts();
			e.printStackTrace();
		}
	}
	
	public void turnRight(int tachoCount) {
		try {
			float sample[] = {0};
			System.out.println("here");
			//sp.fetchSample(sample, 0);
			//System.out.println(sample[0]);
			int current = left.getTachoCount();
			int target = current + tachoCount;
			while (right.getTachoCount() < target) {
				System.out.println("in loop");
				right.forward();
			}
			right.stop(false);
			//sp.fetchSample(sample, 0);
			//System.out.println(sample[0]);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			closePorts();
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Vehicle v = new Vehicle();
		v.turnRight(25);
		v.closePorts();
	}

}
