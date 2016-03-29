import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.hardware.Bluetooth;
import lejos.hardware.Button;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.NXTConnection;

public class BluetoothClient {
	
	public static DataOutputStream dataOut;
	public static DataInputStream dataIn;
	public static NXTConnection BTLink;
	public static BTConnection btLink;
	
	public static void connect() { 
	    System.out.println("Listening");
	    BTLink = Bluetooth.getNXTCommConnector().waitForConnection(8000, NXTConnection.RAW);    
		dataOut = BTLink.openDataOutputStream();
		dataIn = BTLink.openDataInputStream();
	 }//End connect

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		connect();
		while (true) {
			try {
				int transmitReceived = dataIn.readInt();
				System.out.printf("Received: %d\n", transmitReceived);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}

	}

}
