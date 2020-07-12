package com.example.getrssi;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RegisterNewItemActivity extends AppCompatActivity {
    private static final String TAG = "RegisterNewItemActivity";

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    private static final int FIND_ITEM_ACTIVITY = 2;

    private BluetoothAdapter BTAdapter;
    private List<BTDevice> deviceList = new ArrayList<>();
    private ProgressBar progressDiscovery;
    private ArrayAdapter<BTDevice> arrayAdapter;

    private boolean isReceiverRegistered = false;
    private boolean isRegisterActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_item);
        isRegisterActivityActive = true;

        progressDiscovery = findViewById(R.id.progress_discovery);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        Button btnStartDiscovery = findViewById(R.id.btn_start_discovery);
        btnStartDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevices();
            }
        });

        final ListView listViewDiscoveredItems = findViewById(R.id.listview_registered_items);
        arrayAdapter = new DeviceListAdapter(this, R.layout.device_item, deviceList);
        listViewDiscoveredItems.setAdapter(arrayAdapter);
        listViewDiscoveredItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BTDevice selectedItem = (BTDevice) listViewDiscoveredItems.getItemAtPosition(position);
                Log.d(TAG, "onClick: opening dialog");

                DialogRegisterNew dialog = new DialogRegisterNew();
                Bundle args = new Bundle();
                args.putSerializable("itemObj", (Serializable) selectedItem);
                dialog.setArguments(args);
                getFragmentManager();

                dialog.show(getSupportFragmentManager(), "DialogRegisterNew");
//                Intent intent = new Intent(getApplicationContext(), FindItemActivity.class);
//                intent.putExtra("deviceObj", (Serializable) selectedDevice);
//                BTAdapter.cancelDiscovery();
//                startActivityForResult(intent, FIND_ITEM_ACTIVITY);
            }
        });

        if (BTAdapter == null) {
            showToast("Bluetooth is not available");
        } else {
            showToast("Bluetooth is available");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (isReceiverRegistered && !isRegisterActivityActive) {
            unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    showToast("Bluetooth is on");
                    scanDevices();
                }
                else {
                    // User declined to turn on Bluetooth
                    showToast("Could not turn on bluetooth");
                }
                break;

            case FIND_ITEM_ACTIVITY:
                isRegisterActivityActive = true;
                if (resultCode == RESULT_OK) {
                    // Do something
                } else if (resultCode == RESULT_CANCELED) {
                    scanDevices();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isReceiverRegistered) {
            unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }


    public void scanDevices(){
        // Turn on Bluetooth if not enabled
        if (!BTAdapter.isEnabled()) {
            showToast("Turning on Bluetooth...");

            // Intent to turn on Bluetooth
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // Make device discoverable
            if (!BTAdapter.isDiscovering()){
                showToast("Making Your Device Discoverable");
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 15);
                startActivityForResult(discoverableIntent, REQUEST_DISCOVER_BT);
            }

            if (!isReceiverRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(receiver, filter);
                isReceiverRegistered = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBTPermissions();
                }
            }
            BTAdapter.startDiscovery();
        }
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery started");
                progressDiscovery.setVisibility(View.VISIBLE);
                deviceList.clear();
                arrayAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
                progressDiscovery.setVisibility(View.INVISIBLE);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                if(name == null){
                    BluetoothDevice dev =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    name = dev.getAddress();
                }
                BTDevice device = new BTDevice();
                device.deviceName = name;
                device.rssi = rssi;

                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                    Log.d(TAG, deviceList.toString());
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };




    // HELPER FUNCTIONS
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}