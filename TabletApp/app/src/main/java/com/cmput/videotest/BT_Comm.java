package com.cmput.videotest;

/**
 * Retreived from https://github.com/awhittle3/EV3AndroidRC on March 24 2016
 * and http://stackoverflow.com/questions/4969053/bluetooth-connection-between-android-and-lego-mindstorm-nxt on March 24 2016
 *
 */
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.util.Log;
import android.widget.Toast;

public class BT_Comm {
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    //Target NXTs for communication
    //final String nxt2 = "00:16:53:04:52:3A";
    //final String nxt1 = "00:16:53:07:AA:F6";

    BluetoothAdapter localAdapter;
    BluetoothSocket socket_ev3_1, socket_nxt2;
    boolean success=false;


    public boolean initBT(){
        localAdapter=BluetoothAdapter.getDefaultAdapter();
        return localAdapter.isEnabled();
    }

    public void enableBT(){
        localAdapter = BluetoothAdapter.getDefaultAdapter();
        //If Bluetooth not enable then do it
        if(localAdapter.isEnabled()==false){
            localAdapter.enable();
            while(!(localAdapter.isEnabled())){

            }
        }

    }

    public  boolean connectToEV3(String macAdd){
        BluetoothDevice ev3_1 = localAdapter.getRemoteDevice("00:16:53:44:9B:36");
        //try to connect to the ev3
        try {
            socket_ev3_1 = ev3_1.createRfcommSocketToServiceRecord(UUID
                    .fromString(SPP_UUID));
            socket_ev3_1.connect();
            success = true;
        } catch (IOException e) {
            Log.d("Bluetooth","Err: Device not found or cannot connect " + macAdd);
            success=false;
        }
        return success;
    }


    public void writeMessage(byte msg) throws InterruptedException{
        BluetoothSocket connSock;

        //Swith nxt socket
        //if(nxt.equals("nxt2")){
        //    connSock=socket_nxt2;
        //}else
        //if(nxt.equals("nxt1")){
            connSock= socket_ev3_1;
        //}else{
        //    connSock=null;
        //}

        if(connSock!=null){
            try {
                OutputStreamWriter out=new OutputStreamWriter(connSock.getOutputStream());
                out.write(msg);
                out.flush();

                Thread.sleep(1000);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            //Error
        }
    }


    public void writeData(int msg) throws InterruptedException{
        BluetoothSocket connSock;

        //Swith nxt socket
        //if(nxt.equals("nxt2")){
        //    connSock=socket_nxt2;
        //}else
        //if(nxt.equals("nxt1")){
        connSock= socket_ev3_1;
        //}else{
        //    connSock=null;
        //}

        if(connSock!=null){
            try {
                DataOutputStream dout = new DataOutputStream(connSock.getOutputStream());
                //OutputStreamWriter out=new OutputStreamWriter(connSock.getOutputStream());
                dout.writeInt(msg);
                dout.flush();
                //Thread.sleep(40);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            //Error
        }
    }

    // Note: Not needed for the current program
    public int readMessage(String nxt){
        BluetoothSocket connSock;
        int n;
        //Swith nxt socket
        if(nxt.equals("nxt2")){
            connSock=socket_nxt2;
        }else if(nxt.equals("nxt1")){
            connSock= socket_ev3_1;
        }else{
            connSock=null;
        }

        if(connSock!=null){
            try {

                InputStreamReader in=new InputStreamReader(connSock.getInputStream());
                n=in.read();

                return n;


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return -1;
            }
        }else{
            //Error
            return -1;
        }

    }

}