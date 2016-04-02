import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.hardware.BrickFinder;
import lejos.hardware.BrickInfo;
import lejos.hardware.Button;
import lejos.remote.ev3.RemoteEV3;

import lejos.remote.nxt.NXTConnection;
import lejos.utility.Delay;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;

public class BluetoothServer {

	public static DataOutputStream outData1;
	public static BTConnector link1;

	public static DataOutputStream outData2;
	public static BTConnector link2;

	public static void connect() {
		link1 = new BTConnector();
		//link2 = new BTConnector();
		BTConnection btc1 = link1.connect("00:16:53:44:9B:36", NXTConnection.RAW);
		//BTConnection btc2 = link2.connect("00:16:53:44:C1:4A", NXTConnection.RAW);
		outData1 = btc1.openDataOutputStream();
		//outData2 = btc2.openDataOutputStream();
		System.out.println("\nEV3s are Connected");   
	}

	public static void disconnect() {
		try{
			outData1.close();
			//outData2.close();
			//link.close();
		}
		catch (IOException ioe) {
			System.out.println("\nIO Exception writing bytes");
		}
		System.out.println("\nClosed data streams");

	}//End disconnect
	
	public static void send(int data) {
		try {
			System.out.println("Sending: " + data);
			outData1.writeInt(data);
			outData1.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	/*
	public static void main(String[] args) {
		connect();
		//Button.waitForAnyPress();
		for (int i = 0; i < 100; i++) {
			try {
				outData1.writeInt(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		Delay.msDelay(10000);
		disconnect();
	}
	*/
	

}
