package org.opencv.samples.ENB329_SoccerRobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Raph on 27/09/15.
 */


public class BT_helper extends Activity {
    private static final String TAG = "BT_helper";
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    Thread workerThread;
    byte[] readBuffer;
    volatile Boolean stopWorker;
    int readBufferPosition;
    int counter;

    private ListView lv;
    private BluetoothDevice mDevice;

    private void findDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            Log.i(TAG, "BT: no bluetooth support");
            //error case here, no bluetooth support
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDevice = device;
                Log.i(TAG, "BT:" + device.getName());
                //to only find our arduino module
//                if(device.getName().equals("HB-05")) {
//                    mDevice = device;
//                    break;
//                }
            }
        }
        else{
            Log.i(TAG, "BT: No devices found");
        }
    }

    public void openCommunication() throws IOException{
        findDevice();
        Log.i(TAG, "BT: just found device");
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        mSocket.connect();
        mOutputStream = mSocket.getOutputStream();
        mInputStream = mSocket.getInputStream();
        //sendData();
        //beginListenForData();
    }

    void sendData(char[] data) throws IOException{
        mOutputStream.write(data[0]);
        mOutputStream.write(data[1]);
        mOutputStream.write(data[2]);
        //mOutputStream.write('l');
        Log.i(TAG, "bluetooth sent: ");
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {

                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    Log.i(TAG, "BT: thread is running");
                    try {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                Log.i(TAG, "BT:" + b);
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.i(TAG, "BT:" + data);
//                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }

                    catch (IOException ex) {
                        Log.i(TAG, "BT: IOexception found");
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
}