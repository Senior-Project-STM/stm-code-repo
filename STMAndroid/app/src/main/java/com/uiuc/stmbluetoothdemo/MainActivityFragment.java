package com.uiuc.stmbluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    View v;
    TextView textView;
    ImageView iv;
    String received;
    ArrayList<Byte> bufferMain = new ArrayList<Byte>();
    Handler handler;
    Bitmap bm;
    BluetoothAdapter myBluetoothAdapter;
    public static final int ENABLE_BLUETOOTH = 1;
    public static final int MAKE_DISCOVERABLE = 1;
    public static final UUID MY_UUID = UUID.fromString("37407000-8cf0-11bd-b23e-10b75c30d20a");
    public final String SERVICE_NAME = "STM";

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, container, false);
        textView = (TextView) v.findViewById(R.id.textView);
        iv = (ImageView) v.findViewById(R.id.imageView);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg){
                iv.setImageBitmap(bm);
            }};
        return v;
    }

    /**
     * Starts bluetooth on the phone and opens up a bluetooth receive socket
     */
    public void startBluetooth() {
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {        //Check to see if bluetooth is supported on the device
            Snackbar.make(v, "Bluetooth is not supported on this device", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if(myBluetoothAdapter.isEnabled()) {   //Turn off and turn on bluetooth
            myBluetoothAdapter.disable();
        }

        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(v, "Bluetooth has been enabled", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                new AcceptConnectionThread().start();
            }
        }
    }

    private class ReadThread extends Thread {
        private BluetoothSocket socket;
        private InputStream inStream;

        public ReadThread(BluetoothSocket socket) {
            this.socket = socket;
            try {
                inStream = socket.getInputStream();
                Log.w("Stream", "Input Stream Set Up");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Listens on the passed in bluetooth socket for any incoming text, and replaces the text in the textView with the recieved text
        public void run() {
            int totalCount = 0;
            byte[] buffer = new byte[4000];

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    int count = inStream.read(buffer);
                    Log.i("Received", "Received: " + Integer.toString(count));
                    byte[] resp = new byte[4];
                    for(int i = 0; i < 4; i ++) {
                        resp[i] = buffer[i];
                    }
                    Log.i("Rec", new String(resp));
                    if(count == 4 && (new String(resp)).equals("Done")) {
                        Log.i("Image", "Image Received");
                        byte[] arr = new byte[totalCount];
                        for(int i = 0; i < totalCount; i ++) {
                            arr[i] = ((Byte) bufferMain.get(i));
                        }
                        bm = BitmapFactory.decodeByteArray(arr, 0, totalCount);
                        handler.sendEmptyMessage(0);
                        totalCount = 0;
                        bufferMain.clear();
                    }
                    else {
                        for(int i = 0; i < count; i ++) {
                            bufferMain.add(buffer[i]);
                        }
                        totalCount += count;
                    }
                } catch (IOException e) {
                    break;
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }

    private class AcceptConnectionThread extends Thread {
        private BluetoothServerSocket serverSocket = null;

        public AcceptConnectionThread() {
            serverSocket = null;
            Intent makeDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(makeDiscoverable, MAKE_DISCOVERABLE);
            try {
                // NAME is the name of the bluetooth service, which the system will write to a new SDP db entry on the device
                // UUID is a unique string id that is used to uniquely identify the bluetooth service
                serverSocket = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
                Snackbar.make(v, "Server socket has been opened", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.w("Socket", "Server socket has been opened");
            } catch (IOException e) {

            }
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    Snackbar.make(v, "Bluetooth connection has been established", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.w("Socket", "Bluetooth connection has been established");
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    new ReadThread(socket).start();
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) { }
        }
    }
}
