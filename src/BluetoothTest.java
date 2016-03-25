import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

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
		
		while(true){
			try{
				transmission = dataIn.readByte();
				System.out.println("Read byte");
			}catch(IOException e){
				System.out.println("Could not read bt connection");
			}
		}
		
		
	}
	
	public static void connect()
	 { 
	    System.out.println("Listening");
	    BTLink = (BTConnection) Bluetooth.getNXTCommConnector().waitForConnection(20000, NXTConnection.RAW);    
		dataOut = BTLink.openDataOutputStream();
		dataIn = BTLink.openDataInputStream();
	 }//End connect

}
