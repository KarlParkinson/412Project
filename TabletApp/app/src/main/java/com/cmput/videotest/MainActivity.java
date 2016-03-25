package com.cmput.videotest;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    DownloadImageTask d;
    ImageView ipCamera;
    Bitmap mIcon11 = null;
    Button connectIpButton;
    Button connectEV3;
    EditText enterIpText;
    boolean IPCONNECTED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipCamera = (ImageView) findViewById(R.id.capturedimage);
        connectIpButton = (Button) findViewById(R.id.connectIpCamera);
        connectEV3 = (Button) findViewById(R.id.connectEV3);
        enterIpText = (EditText) findViewById(R.id.enterIpAddress);
        enterIpText.setText("172.28.88.195:8080");
        IPCONNECTED = false;
    }

    public void onStart(){
        super.onStart();
        d = new DownloadImageTask((ImageView) findViewById(R.id.capturedimage));

    }

    public void onPause(){
        super.onPause();
        d.cancel(false);
    }

    public void clickConnectEV3(View view){
       Log.d("MainActivity", "Connect to EV3");
    }

    public void clickConnectIpCamera(View view){
        String ip = enterIpText.getText().toString();
        if(!IPCONNECTED){
            d.execute("http://"+ip+"/shot.jpg");
            IPCONNECTED = true;
        }
    }

    public void updateImageView(Bitmap bm){
        this.ipCamera.setImageBitmap(bm);
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
