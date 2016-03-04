package com.uiuc.stmbluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    View v;
    ImageView iv;
    ListView deviceList;
    ArrayAdapter<String> deviceListAdapter;
    String received;
    ArrayList<Byte> bufferMain = new ArrayList<Byte>();
    Handler handler;
    Bitmap bm;
    CommThread thread;
    static boolean readyToSend = false; //A Flag that shows whether we are ready to send messages or not
    BluetoothAdapter myBluetoothAdapter;
    public static final int ENABLE_BLUETOOTH = 1;
    public static final int MAKE_DISCOVERABLE = 1;
    public static final UUID MY_UUID = UUID.fromString("37407000-8cf0-11bd-b23e-10b75c30d20a");
    public final String SERVICE_NAME = "STM";
    // Create a BroadcastReceiver for ACTION_FOUND
    BroadcastReceiver mReceiver;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, container, false);
        iv = (ImageView) v.findViewById(R.id.imageView);
        deviceList = (ListView) v.findViewById(R.id.list_view);
        deviceListAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_view);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String t = deviceListAdapter.getItem(position);
                String lines[] = t.split("\\r?\\n");
                Toast.makeText(getActivity(), lines[1], Toast.LENGTH_SHORT).show();
                BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(lines[1]);
                ConnectThread connectThread = new ConnectThread(device);
                connectThread.start();
            }
        });
        handler = new Handler() {               //Handler to set the image to be the received bitmap
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
        if (myBluetoothAdapter == null) {        //Check to see if bluetooth is supported on the device
            Snackbar.make(v, "Bluetooth is not supported on this device", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (myBluetoothAdapter.isEnabled()) {    //Search for available bluetooth devices, until you find the microscope
            findDevices();
        } else {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
        }
    }

    public void startScan() {
        if(readyToSend) {
            thread.write("Start Scan");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(v, "Bluetooth has been enabled", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                findDevices();     //Search for available bluetooth devices, until you find the microscope
            }
        }
    }

    /*
    Attempts to discover any Bluetooth Devices that are available, and displays them as a list
     */
    public void findDevices() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
                    Log.v("Device", device.getName() + "\n" + device.getAddress());
                    deviceListAdapter.notifyDataSetChanged();
                    Snackbar.make(v, "Device has been added to list", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        myBluetoothAdapter.startDiscovery();
        Snackbar.make(v, "Discovery has started", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        deviceListAdapter.clear();      //Clear out all the values that are currently there
        deviceListAdapter.notifyDataSetChanged();
        deviceList.setAdapter(deviceListAdapter);
    }

    private class CommThread extends Thread {
        private BluetoothSocket socket;
        private InputStream inStream;
        private OutputStream outStream;

        public CommThread(BluetoothSocket socket) {
            this.socket = socket;
            try {
                inStream = socket.getInputStream();
                Log.w("Stream", "Input Stream Set Up");
                outStream = socket.getOutputStream();
                Log.w("Stream", "Input Stream Set Up");
                readyToSend = true; //Enables the sending of messages
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Listens on the passed in bluetooth socket for any incoming images, and replaces the image in the imageView with the received image
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
                readyToSend = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
        Write to the output stream to send commands to the Raspberry Pi
         */
        public void write(String command) {
            try {
                outStream.write(command.getBytes());
            } catch (IOException e) {}
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                readyToSend = false;
                socket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        BluetoothSocket socket = null;

        public ConnectThread(BluetoothDevice device) {
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                Log.v("Try", "Attempting to create bluetooth socket");
                // MY_UUID is the app's UUID string, also used by the server code
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.v("Created", "Created");
            } catch (IOException e) { }

        }


        public void run() {
            // Cancel discovery because it will slow down the connection
            myBluetoothAdapter.cancelDiscovery();
            while (true) {
                try {
                    // Connect the device through the socket. This will block
                    // until it succeeds or throws an exception
                    socket.connect();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and get out
                    try {

                        socket.close();
                    } catch (IOException closeException) {
                    }
                    return;
                }
                // If a connection was created
                if (socket != null) {
                    Snackbar.make(v, "Bluetooth connection has been established", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.w("Socket", "Bluetooth connection has been established");
                    thread = new CommThread(socket);
                    thread.start();
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
    }
}
