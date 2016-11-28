package com.ramida.a3dlaserscaner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

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

    Button button;
    Button button1;
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


        hTextView = (TextView) findViewById(R.id.textView);



        button = (Button) findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button2);
        button2 = (Button) findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                btAdapter = BluetoothAdapter.getDefaultAdapter();


                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

                    Toast.makeText(getApplicationContext(), "Laczy", Toast.LENGTH_SHORT)
                            .show();
                    connect = new ConnectThread(device);
                    connect.start();
                } else {
                    Toast.makeText(getApplicationContext(), "device is not paired", 0)
                            .show();
                }


                // zadanie uaktualniania textView

                try {
                    mHandl.postDelayed(hMyTimeTask, 1000);
                } catch (Exception e) {

                }
            }
        });



        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String message= "1";

                byte[] msgBuffer = message.getBytes();
                connectedThread.write(msgBuffer);

                //tu cos bedzie
            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "2";

                byte[] msgBuffer = message.getBytes();
                connectedThread.write(msgBuffer);
            }
        });


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
                    Log.i(tag, "Otrzymano wiadomosci do zapisu do pliku");

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



                        Log.i(tag, "mam wiadomosci: " + buffer.length + "_"+
                                new String(buffer, "US-ASCII")+ "zapisano do tablicy  ");
                        // Send the obtained bytes to the UI activity


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








}

