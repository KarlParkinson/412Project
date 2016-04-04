
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;
import lejos.utility.Delay;

public class BluetoothTest {
	
	private static BTConnection BTLink;
	private static DataInputStream dataIn;
	private static DataOutputStream dataOut;
	private static int transmission;
	
	
	public BluetoothTest() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		connect();
		int count = 0;
		while(true){
			try{
				dataOut.writeInt(1);
				dataOut.flush();
				transmission = dataIn.readInt();
				System.out.println(transmission);
				dataOut.writeInt(2);
				dataOut.flush();
				transmission = dataIn.readInt();
				System.out.println(transmission);
				dataOut.writeInt(3);
				dataOut.flush();
				transmission = dataIn.readInt();
				System.out.println(transmission);
				dataOut.writeInt(4);
				dataOut.flush();
				transmission = dataIn.readInt();
				System.out.println(transmission);
				//Delay.msDelay();
				System.out.println("Now reading");
				
				Delay.msDelay(2000);
			
			}catch(IOException e){
				System.out.println("Could not read bt connection");
			}
		}
		
		
	}
	
	public static void connect()
	 { 
	    System.out.println("Listening");
	    BTLink = (BTConnection) Bluetooth.getNXTCommConnector().waitForConnection(8000, NXTConnection.RAW);    
		dataOut = BTLink.openDataOutputStream();
		dataIn = BTLink.openDataInputStream();
		System.out.println("Connected");
	 }//End connect

}