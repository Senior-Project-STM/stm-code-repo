package com.uiuc.stmbluetoothdemo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    View v;
    Button connectButton;
    Button resetButton;
    boolean connected = false;
    boolean scanning = false;
    boolean pictureAvailable = false;
    Button scanButton;
    Button saveButton;
    ImageView iv;
    ListView deviceList;
    Dialog deviceListDialog;
    int notificationID = 1;
    boolean dialogOpen = false;
    ArrayAdapter<String> deviceListAdapter;
    ArrayList<Byte> bufferMain = new ArrayList<Byte>();
    int totalCountRead = 0;
    Handler imageHandler;
    Handler buttonHandler;
    Handler titleHandler;
    Bitmap bm;
    String connectedTo = "";
    CommThread thread;
    static boolean readyToSend = false; //A Flag that shows whether we are ready to send messages or not
    BluetoothAdapter myBluetoothAdapter;
    public static final int ENABLE_BLUETOOTH = 1;
    public static final UUID MY_UUID = UUID.fromString("37407000-8cf0-11bd-b23e-10b75c30d20a");
    public final String SERVICE_NAME = "STM";
    // Create a BroadcastReceiver for ACTION_FOUND
    BroadcastReceiver mReceiver;

    public MainActivityFragment() {
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // retain this fragment
//        setRetainInstance(true);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, container, false);
        iv = (ImageView) v.findViewById(R.id.imageView);
        iv.setImageBitmap(bm);
        iv.setOnClickListener(new View.OnClickListener() {           // A click listener for the image
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();         //Create an image Dialog to display the scan image as a large image
                ImageDialogFragment frag = ImageDialogFragment.newInstance(bm);
                frag.show(fm, "image");

            }
        });
        connectButton = (Button) v.findViewById(R.id.connect);
        scanButton = (Button) v.findViewById(R.id.scan);
        resetButton = (Button) v.findViewById(R.id.reset);
        saveButton = (Button) v.findViewById(R.id.save);
        deviceListAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_view);
        titleHandler = new Handler() {                  //Handler to set the title to be displayed
            public void handleMessage(Message msg) {
                if (!connected) {
                    ((MainActivity) getActivity()).setTitle("Disconnected");
                } else {
                    ((MainActivity) getActivity()).setTitle("Connected To: " + connectedTo);
                }
            }};
        imageHandler = new Handler() {               //Handler to set the image to be the received bitmap
            @Override
            public void handleMessage(Message msg){
                iv.setImageBitmap(bm);
            }};
        buttonHandler = new Handler() {      //Handler to toggle being able to click on any of the buttons
            @Override
            public void handleMessage(Message msg) {
                if(connected) {
                    //connectButton.setText("Microscope Connected");
                    connectButton.setVisibility(View.GONE);
                    if(scanning) {
                        scanButton.setVisibility(View.GONE);
                    }
                    else {
                        scanButton.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    connectButton.setText("Connect to Microscope");
                    connectButton.setVisibility(View.VISIBLE);
                    scanButton.setVisibility(View.GONE);
                }
                if(pictureAvailable) {
                    saveButton.setVisibility(View.VISIBLE);
                }
                else{
                    saveButton.setVisibility(View.GONE);
                }
            }
        };
        buttonHandler.sendEmptyMessage(0);
        titleHandler.sendEmptyMessage(0);
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

    public void reset() {
        cancelNotification();
        scanning = false;
        pictureAvailable = false;
        iv.setImageBitmap(null);
        totalCountRead = 0;
        bufferMain.clear();
        thread.write("Reset Scan");
        buttonHandler.sendEmptyMessage(0);
    }

    public void save() {
        if(pictureAvailable) {
            openSaveDialog();
        }
    }

    public void startScan() {
        if(connected) {
            sendNotification("STM Scan is occuring");
            buttonHandler.sendEmptyMessage(0);
            thread.write("Start Scan");
            scanning = true;
        }
    }

    public String savePicture(String name) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        File directory = getContext().getExternalFilesDir(null); //Access the external storage folder
        File path = new File(directory, name + ".jpg");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(path);
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path.getAbsolutePath();
    }

    public void saveToDb(String scanName, String extraNotes, String imagePath, Long time) {
        ScanResultDbHelper dbHelper = new ScanResultDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //These are the values you will insert into the database
        ContentValues values = new ContentValues();
        values.put(ScanResultContract.FeedEntry.TIME, time);
        values.put(ScanResultContract.FeedEntry.SCAN_NAME, scanName);
        values.put(ScanResultContract.FeedEntry.FILE_PATH, imagePath);
        values.put(ScanResultContract.FeedEntry.EXTRA_NOTES, extraNotes);

        db.insert(ScanResultContract.FeedEntry.TABLE_NAME, "null", values);
        Log.v("Inserted", scanName + imagePath);
    }

    /*
    Creates a dialog that allows you to save the results of the current scan, once they are received
     */
    public void openSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Choose a name for the saved scan:");
        final EditText scanName = new EditText(this.getActivity());
        scanName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final EditText extra_notes = new EditText(this.getActivity());
        extra_notes.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final TextView notes = new TextView(getActivity());
        notes.setText("Any Other Notes:");
        LinearLayout l = new LinearLayout(this.getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);
        l.setLayoutParams(params);
        scanName.setLayoutParams(params);
        extra_notes.setLayoutParams(params);
        notes.setLayoutParams(params);
        l.addView(scanName);
        l.addView(notes);
        l.addView(extra_notes);

        builder.setView(l);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Long time = System.currentTimeMillis();
                String path = savePicture(scanName.getText().toString() + Long.toString(time));
                Log.v("Path", path);
                saveToDb(scanName.getText().toString(), extra_notes.getText().toString(), path, time);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialogOpen = false;
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void openDevicePicker() {
        dialogOpen = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Choose Bluetooth Device");

        deviceList = new ListView(this.getActivity());
        deviceList.setPadding(4, 4, 4, 4);
        deviceList.setAdapter(deviceListAdapter);

        //Get all of the already bonded devices and add them to the list of devices to connect to
        Set<BluetoothDevice> devices = myBluetoothAdapter.getBondedDevices();
        if(devices != null) {
            for (BluetoothDevice device : devices) {
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                deviceListDialog.dismiss();
                dialogOpen = false;
                String t = deviceListAdapter.getItem(position);
                String lines[] = t.split("\\r?\\n");
                Toast.makeText(getActivity(), lines[1], Toast.LENGTH_SHORT).show();
                deviceListAdapter.clear();
                deviceListAdapter.notifyDataSetChanged();
                BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(lines[1]);
                ConnectThread connectThread = new ConnectThread(lines[0], device);
                connectThread.start();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setView(deviceList);
        deviceListDialog = builder.create();
        deviceListDialog.show();
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
                    Log.d("Device", device.getName() + "\n" + device.getAddress());
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
        if(!dialogOpen) {
            openDevicePicker();
        }
    }

    /**
     * This class is used for communicating over bluetooth, once the connect thread sets up a connection. There are two threads, a read thread, and a write thread.
     */
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
            } catch (IOException e) {
                cancel();
                e.printStackTrace();
            }
        }

        //Listens on the passed in bluetooth socket for any incoming images, and replaces the image in the imageView with the received image
        public void run() {
            byte[] buffer = new byte[4000];

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    int count = inStream.read(buffer);
                    byte[] picDoneResp = new byte[4];
                    byte[] scanDoneResp = new byte[13];
                    for(int i = 0; i < 13; i ++) {
                        if (i < 4) {
                            picDoneResp[i] = buffer[i];
                        }
                        scanDoneResp[i] = buffer[i];
                    }
                    if(count == 4 && (new String(picDoneResp)).equals("Done")) {
                        sendNotification("STM Scan is occuring");
                        Log.i("Image", "Image Received");
                        write("Received");  //Acknowledge that the image has been received.
                        byte[] arr = new byte[totalCountRead];
                        for(int i = 0; i < totalCountRead; i ++) {
                            arr[i] = ((Byte) bufferMain.get(i));
                        }
                        bm = BitmapFactory.decodeByteArray(arr, 0, totalCountRead);
                        imageHandler.sendEmptyMessage(0);   //Displays the newly received image
                        totalCountRead = 0;
                        bufferMain.clear();
                    }
                    else if(count == 13 && (new String(scanDoneResp)).equals("Scan Finished")) {        //If the scan has finished
                        Log.i("Scan Finished", "Scan Finished");
                        sendNotification("STM Scan has finished");
                        scanning = false;
                        pictureAvailable = true;
                        cancelNotification();
                        Snackbar.make(v, "Scan has finished", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else {
                        for(int i = 0; i < count; i ++) {
                            bufferMain.add(buffer[i]);
                        }
                        totalCountRead += count;
                    }
                    buttonHandler.sendEmptyMessage(0);
                } catch (IOException e) {
                    cancel();
                    break;
                }
            }
            cancel();
        }

        /*
        Write to the output stream to send commands to the Raspberry Pi
         */
        public void write(String command) {
            Log.i("Sending Message", command);
            try {
                outStream.write(command.getBytes());
            } catch (IOException e) {
                cancel();
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                cancelNotification();
                socket.close();
                connected = false;  //Disables the sending of messages, and renables the connect button
                connectedTo = "";
                buttonHandler.sendEmptyMessage(0);
                titleHandler.sendEmptyMessage(0);
                Snackbar.make(v, "Connection to microscope has been terminated", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (IOException e) { }
        }
    }

    /**
     * This thread sets up the connection to the server, and then hands of the final socket to the Comm thread.
     */
    private class ConnectThread extends Thread {
        BluetoothSocket socket = null;
        String device_name;

        public ConnectThread(String device_name, BluetoothDevice device) {
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                Log.v("Try", "Attempting to create bluetooth socket");
                // MY_UUID is the app's UUID string, also used by the server code
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.v("Created", "Created Socket");
                this.device_name = device_name;
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
                    connectedTo = device_name;
                    thread.start();
                    connected = true; //Enables the sending of messages, and disables connecting to microscope
                    titleHandler.sendEmptyMessage(0);
                    buttonHandler.sendEmptyMessage(0);
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

    /**
     * Create a persistent notification when scanning starts
     */
    public void sendNotification(String title) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(getActivity());
        builder.setContentTitle(title);
        builder.setContentText("Connected to: " + connectedTo);

        builder.setTicker("Fancy Notification");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(bm);
        builder.setContentIntent(pIntent);
        Notification notification = builder.build();
        NotificationManager notificationManger =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(notificationID, notification);
    }

    public void cancelNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            Log.v("Receiver", "Not registered");
        }
    }
}
