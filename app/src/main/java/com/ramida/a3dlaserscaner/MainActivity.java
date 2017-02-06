package com.ramida.a3dlaserscaner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;

import static android.R.attr.value;


public class MainActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private Button capture;//, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private ImageView ImgView;
    private boolean cameraFront = false;
    private Bitmap bitmapClone;
    private ProgressDialog progressDialog;



    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    HashSet<BluetoothDevice> devices;
    BluetoothAdapter btAdapter;
    ThreeDDColector skaner;

    Button button;
    Button button1ROTACJA;
    Button button2;
    TextView hTextView;
    TextView hTextInfo;
    EditText etStep;
    EditText etAngle;

    private int nCounter=0;

    public String TextBoxDataFromThread = "Dupa !!";
    public static String folderDate="brak";
    public  static String picName = "brak2";

    // Insert your bluetooth devices MAC address
    private static String address ="98:D3:32:30:39:99";// "98:D3:33:80:70:01";

   // private static String address ="24:DF:6A:0C:2D:7F";

    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    protected static final int MESSAGE_TO_TEXT_BOX = 2;
    protected static final int MESSAGE_TAKE_PHOTO = 3;
    protected static final int  MESSAGE_CLOSE_BT =4;



    String tag = "debugging";
    public final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");


    // watek wykorzystany do polaczenia z urzadzeniem
    ConnectThread connect;
    ConnectedThread connectedThread;


    UpdateTextInfoAsyncTask mUpdateTextInfoAsyncTask = null;

    //private Handler mHandl= new Handler();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();



        hTextView = (TextView) findViewById(R.id.textView);
        hTextInfo= (TextView) findViewById(R.id.tInfoBox);


        button = (Button) findViewById(R.id.button);
        button1ROTACJA = (Button) findViewById(R.id.Brot);
        button2 = (Button) findViewById(R.id.button3);
        etAngle= (EditText) findViewById(R.id.tAngle);
        etStep = (EditText) findViewById(R.id.tStep);





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

              /*  try {
                    mHandl.postDelayed(hMyTimeTask, 1000);
                } catch (Exception e) {

                }*/
            }
        });



        button1ROTACJA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {



                connectedThread.resetConection();
                finish();
                System.exit(0);
                //tu cos bedzie

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
  /*              String message = "50\n";

                byte[] msgBuffer = message.getBytes();
                connectedThread.write(msgBuffer);
*/

                String message= "haslo\n";

               // hTextView.setText("Send pass\n");
                byte[] msgBuffer = message.getBytes();
                connectedThread.write(msgBuffer);




                hTextView.setText("Start pomiarow\n");

                folderDate =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                skaner = new ThreeDDColector(Float.valueOf(etStep.getText().toString()) , Float.valueOf(etAngle.getText().toString()));
                skaner.start();



                if (mUpdateTextInfoAsyncTask == null )
                {
                    mUpdateTextInfoAsyncTask = new UpdateTextInfoAsyncTask();
                    mUpdateTextInfoAsyncTask.execute();
                }


            }
        });



    }

// myContext.getMainLooper()

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler( Looper.getMainLooper()) {
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
                case MESSAGE_TO_TEXT_BOX:
                {
                  //  hTextInfo.setText((String)msg.obj);
                }
                break;

                case MESSAGE_TAKE_PHOTO:
                      //  mCamera.takePicture(null, null, mPicture);
                    break;

                case   MESSAGE_CLOSE_BT:
                        connectedThread.resetConection();
                    break;
            }
        }
    };


    public void takePhoto() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                mCamera.takePicture(null, null, mPicture);
                skaner.setPhotoReady();
            }
        });

    }



    public void receiveMyMessage() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                hTextInfo.setText(TextBoxDataFromThread);
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


        if (mUpdateTextInfoAsyncTask == null )
            {
                mUpdateTextInfoAsyncTask = new UpdateTextInfoAsyncTask();
                mUpdateTextInfoAsyncTask.execute();
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


    public static byte[][] cloneArray(byte[][] src) {
        int length = src.length;
        byte[][] target = new byte[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }


    private  class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        //ArrayList<byte[]> daneDD;



        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;



            // Write to SD Card
            try {

                File outFile= getOutputMediaFile();



                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

               // Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                //refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

    }

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //new SaveImageTask().execute(data);
            new SaveImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data);
            resetCam();
          //  Log.d(TAG, "onPictureTaken - jpeg");
        }
    };


    private void resetCam() {
        mCamera.startPreview();
        mPreview.setCamera(mCamera);
    }



    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public synchronized void onPictureTaken(byte[] data, Camera camera) {

                long startTime = System.currentTimeMillis();



            ///moje


            /*   Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0 , data.length);
               bitmapClone = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
                        mBitmap.getConfig());
                bitmapClone.copy(mBitmap.getConfig(), true);


                ImgView.setImageBitmap(bitmapClone);
              ImgView.invalidate();
*/
                /*
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

                  //  Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName()+ " in "+ String.format("%d", TimeUnit.MILLISECONDS.toSeconds(difference)) + " s", Toast.LENGTH_SHORT);
                   // toast.show();

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



            //Intent myIntent = new Intent(myContext, STLViewActivity.class);
            //myContext.startActivity(myIntent);


            //new PostTask().execute("dupa");

           // cameraPreview.removeView(mPreview);
           // mCamera.startPreview();


           /// to zablokowane ostatnio
            new LoadViewTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                        // mCamera.takePicture(null, null, mPicture);


            //startActivity();

            //ImgView.setImageBitmap();
            //ImgView.invalidate();

        }
    };

    //make picture and save to a folder
    private static File getOutputMediaFile() {

        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir1 = new File("/sdcard/3DScannerData/");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir1.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir1.mkdirs()) {
                return null;
            }
        }


        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/3DScannerData/"+folderDate);

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }



        //take the current timeStamp
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:

        mediaFile = new File(mediaStorageDir.getPath() + File.separator  + picName + ".jpg");

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



/*
    private Runnable hMyTimeTask = new Runnable() {
        @Override
        public void run() {
            nCounter++;
            hTextView.setText("Nowy tekst "+ nCounter);
        }
    };*/






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

        public void resetConection()
        {

            if (mmInStream != null) {
                try {mmInStream.close();} catch (Exception e) {}
               // mmInStream = null;
            }

            if (mmOutStream != null) {
                try {mmOutStream.close();
                    //mmOutStream = null;
                } catch (Exception e) {}

            }

            if (mmSocket != null) {
                try {mmSocket.close();} catch (Exception e) {}
               // mmSocket = null;
            }
                RunThread=false;

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
                            //hTextView.setText(new String(buffer, "US-ASCII"));
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




    private class  OneMeasurment extends Thread {

    }

    private class  ThreeDDColector extends Thread {
        boolean RunThread;
        float stepAngle, AngleMax;
        boolean canTakephoto;
        boolean canRotateTable;
        int nr_of_pic=0;


        public ThreeDDColector (float step,float angle) {
            stepAngle=step;
            RunThread=true;
            AngleMax=angle;

            canRotateTable=true;
               canTakephoto=true;

        }

        public synchronized void setRotationReady()
        {
            canRotateTable=true;

        }

        public synchronized void setPhotoReady()
        {
            canTakephoto= true;

        }

       private synchronized void RotateTable(float Angle)
        {
            String message =  Integer.toString((int)Angle)+'\n';

            byte[] msgBuffer = message.getBytes();

            connectedThread.write(msgBuffer);


            canRotateTable=false;


        }


        private  synchronized void UpdateTBox(float Angle)
        {
            TextBoxDataFromThread = "Aktualny kat " + Angle+"\nWykonano "+ ((Angle/AngleMax)*100.0)+" %"+"\nWykonano " +nr_of_pic+"zdjęć";

            if(Angle == (float)-1.0)
                TextBoxDataFromThread= "Wykonano "+nr_of_pic+"zdjęć\nKoniec pomiarow!!!";


          //  mHandler.obtainMessage(MESSAGE_TO_TEXT_BOX, msg2).sendToTarget();


        }


         public synchronized void run() {

             try {


                /* String message = "haslo\n";

                 byte[] msgBuffer = message.getBytes();

                 connectedThread.write(msgBuffer);
*/


                 for (float a=0;a<=AngleMax;a+=stepAngle)
                 {
                     synchronized(this) {
                         while (!canTakephoto & !canRotateTable) {
                             Log.i("Obroty", "czekam w whileu ");

                         }


                         if (canTakephoto) {
                             nr_of_pic++;
                             //  mCamera.takePicture(null, null, mPicture);
                             int tmp = (int) a;

                             picName = Integer.toString(nr_of_pic) + "_" + String.valueOf(tmp);


                             takePhoto();


                             // mHandler.obtainMessage(MESSAGE_TAKE_PHOTO, "brak").sendToTarget();

                             Log.i("Obroty", "zdjecie zrobione wewnatrz ifa ");

                             canTakephoto = false;


                             UpdateTBox(a);
                             Log.i("Obroty", "po  udate textu");

                         }

                         Log.i("Obroty", "zdjecie zrobione ");
                         RotateTable(stepAngle);
                         Log.i("Obroty", "stol obrucony");
                         //    Thread.
                         sleep(3000);
                         Log.i("Obroty", "koniec przerwy");
                         //setRotationReady();
                         //   Log.i("Obroty", "mozesz robic nastepna fotke");
                     }
                 }
                 UpdateTBox((float)-1.0);


            } catch (InterruptedException e) {
                 e.printStackTrace();
             }


        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            RunThread = false;
        }

    }


    /****
     * Watek asynchroniczny do aktualizacji text boxow
     */



    public class UpdateTextInfoAsyncTask extends AsyncTask<Void, Integer, Void> {

        int myProgress;
        boolean running;


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            myProgress = 0;
            running = false;

        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);


        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub

            running = false;
            //	mUpdateTextInfoAsyncTask = null;
        }


        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            //while (myProgress < 100)
            running = true;

           /* while (AutoUpdate.isChecked())
            {
                //RefreshListOfChanels();

                myProgress++;
                publishProgress(myProgress);
                SystemClock.sleep(500);
            }*/
            while (running) {
                publishProgress(myProgress);
                SystemClock.sleep(500);
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            //cProgress.setProgress(values[0]);
            //mainactivity context = (mainactivity) getActivity();
            //context.setListOfFiesToTransferSSHServer("nazwa");

            if (myContext != null) {

                receiveMyMessage();
               // hTextInfo.setText(TextBoxDataFromThread);

                //ut dodam wyswietlanie aktualnego stringa z maina :)


            }

        }

    }



    private class PostTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // displayProgressBar("Downloading...");

            //Create a new progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle("Loading...");
            //Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage("Loading application View, please wait...");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number of items is 100
            progressDialog.setMax(100);
            //Set the current progress to zero
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            //String url=params[0];

            try
            {
                //Get the current thread's token
                synchronized (this)
                {
                    //Initialize an integer (that will act as a counter) to zero
                    int counter = 0;
                    //While the counter is smaller than four
                    while(counter <= 10)
                    {
                        //Wait 850 milliseconds
                        this.wait(850);
                        //Increment the counter
                        counter++;
                        //Set the current progress.
                        //This value is going to be passed to the onProgressUpdate() method.
                        publishProgress(counter*10);
                    }
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }


            return "All Done!";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);


            //  updateProgressBar(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View
            setContentView(R.layout.activity_main);

            //  dismissProgressBar();
        }
    }


    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
        //Before running code in separate thread
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //Create a new progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle("Loading...");
            //Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage("Loading application View, please wait...");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number of items is 100
            progressDialog.setMax(100);
            //Set the current progress to zero
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();
            Log.i("Obroty", "koniec proces Showed");
        }

        //The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params)
        {
            /* This is just a code that delays the thread execution 4 times,
             * during 850 milliseconds and updates the current progress. This
             * is where the code that is going to be executed on a background
             * thread must be placed.
             */
            Log.i("Obroty", "przed try");
            try
            {
                //Get the current thread's token
                synchronized (this)
                {
                    folderDate =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                    Log.i("Obroty", "w synchronized");
                    //Initialize an integer (that will act as a counter) to zero
                    int counter = 0;
                    //While the counter is smaller than four

                    while(counter < 100)
                    {
                        picName = Integer.toString(counter);
                        //takePhoto();
                        //mCamera.takePicture(null, null, mPicture);
                        mCamera.takePicture(null, null, jpegCallback);

                        Log.i("Obroty", "w while ");
                        //Wait 850 milliseconds
                        this.wait(2000);

                        //Increment the counter
                        counter+=5;
                        Log.i("Obroty", "po waicie");
                        //Set the current progress.
                        //This value is going to be passed to the onProgressUpdate() method.
                        publishProgress(counter);
                        Log.i("Obroty", "po publikacji");



                    }
                }
            }
            catch (InterruptedException e)
            {
                Log.i("Obroty", "w kaczu");
                e.printStackTrace();
            }
            Log.i("Obroty", "returny 0000");
            return null;
        }

        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            //set the current progress of the progress dialog
            Log.i("Obroty", "w publikacji");
            progressDialog.setProgress(values[0]);
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(Void result)
        {
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View
            setContentView(R.layout.activity_main);
        }
    }




}



