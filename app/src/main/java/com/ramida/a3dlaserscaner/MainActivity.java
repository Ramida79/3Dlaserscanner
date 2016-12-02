package com.ramida.a3dlaserscaner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Camera;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private Button capture;//, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private ImageView ImgView;
    private boolean cameraFront = false;
    private Bitmap bitmapClone;


  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
*/


    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    HashSet<BluetoothDevice> devices;
    BluetoothAdapter btAdapter;
    ThreeDDColector skaner;

    Button button;
    Button button1ROTACJA;
    Button button2;
    TextView hTextView;
    private int nCounter=0;




    // Insert your bluetooth devices MAC address
    private static String address ="98:D3:32:30:39:99";// "98:D3:33:80:70:01";



    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;

    String tag = "debugging";
    public final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");


    // watek wykorzystany do polaczenia z urzadzeniem
    ConnectThread connect;
    ConnectedThread connectedThread;


    private Handler mHandl= new Handler();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();



        hTextView = (TextView) findViewById(R.id.textView);



        button = (Button) findViewById(R.id.button);
        button1ROTACJA = (Button) findViewById(R.id.Brot);
        button2 = (Button) findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                hTextView.setText("Connecting...\n");

                btAdapter = BluetoothAdapter.getDefaultAdapter();


                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

                    Toast.makeText(getApplicationContext(), "Laczy", Toast.LENGTH_SHORT)
                            .show();
                    connect = new ConnectThread(device);
                    connect.start();
                } else {
                    Toast.makeText(getApplicationContext(), "device is not paired",Toast.LENGTH_LONG).show();
                }


                // zadanie uaktualniania textView

                try {
                    mHandl.postDelayed(hMyTimeTask, 1000);
                } catch (Exception e) {

                }
            }
        });



        button1ROTACJA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String message= "haslo\n";

                hTextView.setText("Send pass\n");
                byte[] msgBuffer = message.getBytes();
                connectedThread.write(msgBuffer);
                //tu cos bedzie

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
  /*              String message = "50\n";

                byte[] msgBuffer = message.getBytes();
                connectedThread.write(msgBuffer);
*/

                hTextView.setText("Start pomiarow\n");
                skaner = new ThreeDDColector((float)100.0 , (float)360.0);
                skaner.start();
            }
        });


    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }



    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
               // switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }
    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                long startTime = System.currentTimeMillis();





                ///moje


                Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0 , data.length);
                bitmapClone = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
                        mBitmap.getConfig());
                bitmapClone.copy(mBitmap.getConfig(), true);

                int iY = 0;
                int iX = 0;
                int iPixel = 0;
                int iRed = 0;
                int iGreen = 0;
                int iBlue = 0;
                int iRGBAvg = 0;
                // Gray of image processing.


                // The height of the image
/*                for ( iY = 0; iY < bitmapClone.getHeight(); iY++ )
                {
                    // The width of the image
                    for ( iX = 0; iX < bitmapClone.getWidth(); iX++ )
                    {
                        // To get pixel.
                        iPixel = mBitmap.getPixel(iX, iY);
                        // To get value of the red channel.
                        iRed = Color.red(iPixel);
                        // To get value of the green channel.
                        iGreen = Color.green(iPixel);
                        // To get value of the blue channel.
                        iBlue = Color.blue(iPixel);
                        // Compute value of gray.
                        iRGBAvg = ( iRed + iGreen + iBlue ) / 3;
                        // Set pixel of gray.
                        bitmapClone.setPixel(iX, iY, Color.rgb(iRGBAvg, iRGBAvg, iRGBAvg));
                    }
                }
*/
                ImgView.setImageBitmap(bitmapClone);


                ///moje koniec




                //make a new picture file
                File pictureFile = getOutputMediaFile();

                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();

                    long difference = System.currentTimeMillis() - startTime;

                    Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName()+ " in "+ String.format("%d", TimeUnit.MILLISECONDS.toSeconds(difference)) + " s", Toast.LENGTH_LONG);
                    toast.show();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }

                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }


    OnClickListener captrureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null, mPicture);

        }
    };

    //make picture and save to a folder
    private static File getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "3DScannerData");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
        ImgView = (ImageView)this.findViewById(R.id.imgView);


        capture = (Button) findViewById(R.id.button_capture);
        capture.setOnClickListener(captrureListener);

        //switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
        //switchCamera.setOnClickListener(switchCameraListener);
    }


    private Runnable hMyTimeTask = new Runnable() {
        @Override
        public void run() {
            nCounter++;
            hTextView.setText("Nowy tekst "+ nCounter);
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
     /*   if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }




    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT: {

                    connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                    connectedThread.start();
                    Log.i(tag, "jestem polaczony- connected");
                }
                break;
                case MESSAGE_READ: {
                    byte[] readBuf = (byte[]) msg.obj;



                    Log.i(tag, "NOWEwiadomosci: " + readBuf.length + "____ "+ readBuf.toString());//new String(readBuf.to, "US-ASCII"));



                }
                break;
            }
        }
    };






    private class ConnectThread extends Thread {

        @SuppressWarnings("unused")
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(tag, "construct");
            // Get a BluetoothSocket to connect with the given
            // BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server
                // code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(tag, "get socket failed");
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();
            Log.i(tag, "connect - run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i(tag, "connect - succeeded");
            } catch (IOException connectException) {
                Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();

        }
    }



    private class ConnectedThread extends Thread {
        private boolean RunThread;

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //new ChanelsAndMeasurments();;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            RunThread = false;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            } finally {
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
                RunThread = true;
                //     dataBuffer = new CircularBuffer(2*1024*save_file_size);
            }
        }




        public void run() {
            byte[] buffer; // buffer store for the stream

            // Keep listening to the InputStream until an exception occurs
            while (RunThread) {
                try {
                    // Read from the InputStream
                    int size = mmInStream.available();
                    if (size > 0) {

                        buffer = new byte[size];
                        mmInStream.read(buffer, 0, size);



                        Log.i(tag, "mam wiadomosci: " + buffer.length + "____ "+ new String(buffer, "US-ASCII"));
                        // Send the obtained bytes to the UI activity


                      /*  Toast toast = Toast.makeText(myContext, " otzymano dane " +  new String(buffer, "US-ASCII"), Toast.LENGTH_LONG);
                        toast.show();
*/



                        if(     new String(buffer, "US-ASCII") == "R")
                        {
                            hTextView.setText(new String(buffer, "US-ASCII"));
                                skaner.setRotationReady();
                        }

                        Thread.sleep(100);

                    }
                }


                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Log.i(tag, "Watek czytajacy zakonczyl dzialanie:");
        }

        /*
         * Call this from the main activity to send data to the remote device
         */
        public void write(byte[] bytes) {
            try {

                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            RunThread = false;
        }
    }







    private class ThreeDDColector extends Thread {
        boolean RunThread;
        float stepAngle, AngleMax;
        boolean canTakephoto;


        public ThreeDDColector (float step,float angle) {
            stepAngle=step;
            RunThread=true;
            AngleMax=angle;
            canTakephoto=true;

        }

        public void setRotationReady()
        {
            canTakephoto=true;

        }

       private  void RotateTable(float Angle)
        {
            String message =  Integer.toString((int)Angle)+'\n';

            byte[] msgBuffer = message.getBytes();

            connectedThread.write(msgBuffer);


        }


         public void run() {

             try {


                /* String message = "haslo\n";

                 byte[] msgBuffer = message.getBytes();

                 connectedThread.write(msgBuffer);
*/


                 for (float a=0;a<=AngleMax;a+=stepAngle)
                 {

                     if(canTakephoto)
                     {

                        // mCamera.takePicture(null, null, mPicture);

                         canTakephoto=false;
                     }

                     RotateTable(stepAngle);
                     Thread.sleep(2000);

                 }

             } catch (InterruptedException e) {
                 e.printStackTrace();
             }


        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            RunThread = false;
        }

    }


}




