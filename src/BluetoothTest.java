<<<<<<< 585dfc2503d349f05f72973be0f4d14d095edfcb
=======

<<<<<<< 1fe3b0d7b737135c5bf2bec21b62faa85c103c3e
>>>>>>> communication working, swapped bricks
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
<<<<<<< 585dfc2503d349f05f72973be0f4d14d095edfcb
				transmission = dataIn.readInt();
				System.out.println("Read int: " + transmission);
				if(transmission == -1){
					Delay.msDelay(500);
				}
=======
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
			
>>>>>>> communication working, swapped bricks
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
<<<<<<< 585dfc2503d349f05f72973be0f4d14d095edfcb
	 }//End connect

}
=======
		System.out.println("Connected");
	 }//End connect

}
>>>>>>> communication working, swapped bricks
=======
>>>>>>> bluetooth connection test
