import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;
import lejos.utility.Delay;

public class BluetoothClient {

	public static DataInputStream dataIn;
	//public static DataOutputStream dataOut;
	public static BTConnection BTLink;

	
	public static void connect() {
		  System.out.println("Listening for Tablet");
		  BTLink = (BTConnection) Bluetooth.getNXTCommConnector().waitForConnection(8000, NXTConnection.RAW);    
		  //dataOut = BTLink.openDataOutputStream();
		  dataIn = BTLink.openDataInputStream();
		  System.out.println("Connected");
	}

	public static void disconnect() {
		try{
			dataIn.close();
		}
		catch (IOException ioe) {
			System.out.println("\nIO Exception writing bytes");
		}
		System.out.println("\nClosed data streams");

	}//End disconnect
	
	public static int[] read(){
		
		int[] values = {-1,-1,-1,-1};
		int transmission;
		
		while(true){
			try{
				transmission = dataIn.readInt();
				System.out.println("Read int: " + transmission);
				if(transmission == -1){
					Delay.msDelay(100);
					for(int i = 0; i < 4; i++){
						transmission = dataIn.readInt();
						values[i] = transmission;
					}
					break;
				}
			}catch(IOException e){
				System.out.println("Could not read bt connection");
			}
		}
		
		return values;
		
	}


}
