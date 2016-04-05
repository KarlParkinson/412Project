package com.cmput.videotest;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    static {
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = "OCVTabletApp::Activity";

    private DownloadImageTask downloadImageAsyncTask;
    private ConnectedThread bluetoothThread;

    ImageView ipCamera;
    Bitmap mIcon11 = null;
    Button connectIpButton;
    Button connectEV3;
    EditText enterIpText;
    TextView blobXText;
    TextView blobYText;
    TextView centerXText;
    TextView centerYText;

    boolean ipconnected;
    boolean btconnected;
    BT_Comm btComm;
    String macAddress1 = "00:16:53:44:9B:36";
    //String macAddress1 = "00:16:53:44:C1:4A";
    int[] goalCoords = {0, 0};
    int[] centerCoords = {0,0};
    int[] blobCoords = {0,0};

    private boolean mIsColorSelected = false;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipCamera = (ImageView) findViewById(R.id.capturedimage);
        connectIpButton = (Button) findViewById(R.id.connectIpCamera);
        connectEV3 = (Button) findViewById(R.id.connectEV3);
        blobXText = (TextView) findViewById(R.id.blobX);
        blobXText.setText("");
        blobYText = (TextView) findViewById(R.id.blobY);
        blobYText.setText("");
        centerXText = (TextView) findViewById(R.id.centerX);
        centerXText.setText("");
        centerYText = (TextView) findViewById(R.id.centerY);
        centerYText.setText("");
        enterIpText = (EditText) findViewById(R.id.enterIpAddress);
        enterIpText.setText("172.28.90.37:8080");
        ipconnected = false;
        btconnected = false;
        btComm = new BT_Comm();
        downloadImageAsyncTask = new DownloadImageTask((ImageView) findViewById(R.id.capturedimage));
    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        btconnected = false;
        downloadImageAsyncTask.cancel(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        btconnected = false;
        downloadImageAsyncTask.cancel(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    /**
     * Button action to Connect to EV3 brick
     * @param view
     */
    public void clickConnectEV3(View view) {
        if (!btconnected) {
            Log.d("Bluetooth", "Trying to connect to EV3");

            if (!btComm.initBT()) {
                Log.d("Bluetooth", "Could not init bluetooth");
            } else {
                boolean connected = btComm.connectToEV3(macAddress1);
                if (connected) {
                    Log.d("Bluetooth", "Connected to brick");
                    btconnected = true;
                    bluetoothThread = new ConnectedThread(btComm.socket_ev3_1);
                    bluetoothThread.start();
                } else {
                    Toast.makeText(this,"Could not connect to EV3",Toast.LENGTH_SHORT);
                    Log.d("Bluetooth", "Did not connect");
                }
            }
        }
    }

    /**
     * Button action to connect to Ip camera
     *
     * @param view
     */
    public void clickConnectIpCamera(View view) {
        String ip = enterIpText.getText().toString();
        Log.d("Connect to camera","Trying to connect to: " + "http://" + ip + "/shot.jpg");

        if (!ipconnected) {
            //Initialize opencv variables
            mDetector = new ColorBlobDetector();
            mSpectrum = new Mat();
            mBlobColorRgba = new Scalar(255);
            mBlobColorHsv = new Scalar(255);
            SPECTRUM_SIZE = new Size(200, 64);
            CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
            downloadImageAsyncTask.execute("http://" + ip + "/shot.jpg");
            ipconnected = true;
        }else{
            Toast.makeText(this,"Could not connect to ip camera",Toast.LENGTH_SHORT);
        }
    }
    public void clickStopEV3(View view) {
        Log.d("main activity", "Stopping EV3");
        bluetoothThread.write(5);
        btconnected =false;
        try{
            btComm.socket_ev3_1.close();
        }catch(IOException e){
            Log.d("Bluetooth", "Could not close socket");
        }
    }
    /**
     * Thread task for pulling images from ipCamera, and applying opencv object detection
     */
    private class DownloadImageTask extends AsyncTask<String, Bitmap, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            while (!this.isCancelled()) {
                try {
                    InputStream in = new URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    publishProgress(mIcon11);
                    Log.d("MainActivity", "Picture read");
                } catch (Exception e) {
                    //Log.e("Error", e.getMessage());
                    Log.d("MainActivity", "Picture not read");
                }
            }
            return mIcon11;
        }

        //Allows UI changes
        protected void onProgressUpdate(Bitmap... result) {
            super.onProgressUpdate(result);
            Bitmap bm = result[0];

            //If tracking color has not been selected then just display bm
            if (!mIsColorSelected) {
                bmImage.setImageBitmap(result[0]);
                mRgba = new Mat(ipCamera.getHeight(), ipCamera.getWidth(), CvType.CV_8UC4);
                Imgproc.circle(mRgba, new Point(mRgba.cols() / 2, mRgba.rows() / 2), 20, new Scalar(0, 255, 0), 1);
                Imgproc.circle(mRgba, new Point(mRgba.cols()/2, mRgba.rows()/2), 2, new Scalar(0, 255, 0), 1);
                Utils.bitmapToMat(result[0],mRgba);
            } else {
                //convert bm from ipcamera to opencv Mat
                mRgba = new Mat(ipCamera.getHeight(), ipCamera.getWidth(), CvType.CV_8UC4);
                Utils.bitmapToMat(result[0], mRgba);

                //Call blob detector process and get contours, and draw contours
                mDetector.process(mRgba);
                List<MatOfPoint> contours = mDetector.getContours();
                Log.e(TAG, "Contours count: " + contours.size());
                Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

                //Display selected color on image top corner
                Mat colorLabel = mRgba.submat(4, 68, 4, 68);
                colorLabel.setTo(mBlobColorRgba);
                Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());


                centerCoords[0] = mRgba.cols() / 2;
                centerCoords[1] = mRgba.rows() /2;
                //if Bluetooth connected then get contour center and send to EV3
                if(contours.size() > 0){
                    MatOfPoint2f cnt =new MatOfPoint2f(contours.get(0).toArray());
                    Point center = new Point();
                    float[] radius = {0};
                    Imgproc.minEnclosingCircle(cnt, center, radius);
                    blobCoords[0] = (int) Math.round(center.x);
                    blobCoords[1] = (int) Math.round(center.y);
                }else{
                    blobCoords[0] = mRgba.cols() / 2;
                    blobCoords[1] = mRgba.rows() /2;
                }

                blobXText.setText(" blob X: " + blobCoords[0]);
                blobYText.setText(" blob Y: " + blobCoords[1]);

                centerXText.setText(" center X: " + (mRgba.cols() / 2));
                centerYText.setText(" center Y: " + (mRgba.rows() / 2));

                //Draw center target
                Imgproc.circle(mRgba, new Point(mRgba.cols()/2, mRgba.rows()/2), 20, new Scalar(0, 255, 0), 1);
                Imgproc.circle(mRgba, new Point(mRgba.cols()/2, mRgba.rows()/2), 2, new Scalar(0, 255, 0), 1);
                Utils.matToBitmap(mRgba, bm);
                ipCamera.setImageBitmap(bm);
            }
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //mOpenCvCameraView.enableView();
                    ipCamera.setOnTouchListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /**
     * OnTouch Action for ImageView that displays ipCamera frames
     *
     * Used OpenCV Android SDK Samples:
     * retrieved from https://github.com/Itseez/opencv/blob/master/samples/android/color-blob-detection/src/org/opencv/samples/colorblobdetect/ColorBlobDetectionActivity.java
     * on 2016-03-27
     *
     * @param v
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (ipCamera.getWidth() - cols) / 2;
        int yOffset = (ipCamera.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


    /**
     * Bluetooth connection thread, Handles reading and writing from bluetooth socket
     * Uses BT_comm class for writing
     */
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int n;
            int bytes;

            // Keep looping to listen for received messages
            while (btconnected) {
                try {
                    n = mmInStream.read();

                    /*
                    Right here write some code
                    if(n == some number){
                        enter teleoperation mode
                        Show left and right buttons
                        On button clicks send commands to Ev3
                        When stop button is pressed do normal stuff

                    }
                     */
                    if(n > 0) {
                        Log.d("BT Thread num received ",String.valueOf(n));
                        write(n);
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(int n) {
            try {
                switch (n) {
                    case 1:
                        btComm.writeData(blobCoords[0]);
                        Log.d("BT Blob X", String.valueOf(blobCoords[0]));
                        break;
                    case 2:
                        btComm.writeData(blobCoords[1]);
                        Log.d("BT Blob Y", String.valueOf(blobCoords[1]));
                        break;
                    case 3:
                        btComm.writeData(centerCoords[0]);
                        Log.d("BT center X", String.valueOf(centerCoords[0]));
                        break;
                    case 4:
                        btComm.writeData(centerCoords[1]);
                        Log.d("BT center Y", String.valueOf(centerCoords[0]));
                        break;
                    case 5:
                        btComm.writeData(-1);
                        Log.d("BT stop", String.valueOf(-1));
                        break;
                }
            } catch (InterruptedException e) {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}




