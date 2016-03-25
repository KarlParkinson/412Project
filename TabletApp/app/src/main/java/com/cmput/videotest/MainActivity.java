package com.cmput.videotest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    DownloadImageTask d;
    ImageView ipCamera;
    Bitmap mIcon11 = null;
    Button connectIpButton;
    Button connectEV3;
    EditText enterIpText;
    boolean ipconnected;
    boolean btconnected;
    BT_Comm btComm;
    String macAddress1 = "00:16:53:44:9B:36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipCamera = (ImageView) findViewById(R.id.capturedimage);
        connectIpButton = (Button) findViewById(R.id.connectIpCamera);
        connectEV3 = (Button) findViewById(R.id.connectEV3);
        enterIpText = (EditText) findViewById(R.id.enterIpAddress);
        enterIpText.setText("172.28.88.195:8080");
        ipconnected = false;
        btconnected = false;
        btComm = new BT_Comm();
    }

    public void onStart(){
        super.onStart();
        d = new DownloadImageTask((ImageView) findViewById(R.id.capturedimage));

    }

    public void onPause(){
        super.onPause();
        d.cancel(false);
    }

    public void clickConnectEV3(View view) {
        if(!btconnected) {
            Log.d("MainActivity", "Connect to EV3");
            btComm.enableBT();
            boolean connected = btComm.connectToEV3(this.macAddress1);

            if (connected) {
                btconnected = true;
                try {
                    btComm.writeMessage((byte) 42);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    Log.d("MainActivity", "Did not sent byte");
                }
            }
        }

    }

    public void clickConnectIpCamera(View view){
        String ip = enterIpText.getText().toString();
        if(!ipconnected){
            d.execute("http://"+ip+"/shot.jpg");
            ipconnected = true;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Bitmap, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            while(!this.isCancelled()){
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    publishProgress(mIcon11);
                    Log.d("MainActivity", "Picture read");
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    Log.d("MainActivity", "Picture not read");
                }
            }
            return mIcon11;
        }

        protected void onProgressUpdate(Bitmap... result) {
            super.onProgressUpdate(result);
            bmImage.setImageBitmap(result[0]);
            Log.d("MainActivity", "Async task on progress update");
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);

        }


    }





}
