import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.hardware.Bluetooth;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.NXTConnection;

public class MotorController {
	
	public static DataOutputStream dataOut;
	public static DataInputStream dataIn;
	public static NXTConnection BTLink;
	public static BTConnection btLink;
	
	public static void turn(EV3LargeRegulatedMotor motor, int tachoCountChange) {
		System.out.println("Turning by " + tachoCountChange + " tachos");
		int target = motor.getTachoCount() + tachoCountChange;
		while (motor.getTachoCount() < target) {
			motor.forward();
		}
		motor.stop();
	}
	
	public static void connect() { 
	    System.out.println("Listening");
	    BTLink = Bluetooth.getNXTCommConnector().waitForConnection(8000, NXTConnection.RAW);    
		dataOut = BTLink.openDataOutputStream();
		dataIn = BTLink.openDataInputStream();
	 }//End connect
	
	public static int decode(int received) {
		int motor = received % 10;
		return (int) ((received - motor*Math.signum(motor)) / 10);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.A);
		EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.D);
		
		left.setSpeed(50);
		right.setSpeed(50);
		
		EV3LargeRegulatedMotor motor;
		
		connect();
		while (true) {
			try {
				int transmitReceived = dataIn.readInt();
				System.out.printf("Received: " +transmitReceived);
				if (transmitReceived == Integer.MAX_VALUE) {
					left.stop();
					right.stop();
				} else if (transmitReceived == Integer.MIN_VALUE) {
					left.forward();
					right.forward();
				} else {
					motor = (transmitReceived >= 0) ? left : right;
					turn(motor, Math.abs(transmitReceived));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}

	}

}
