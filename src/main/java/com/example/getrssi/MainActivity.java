package com.example.getrssi;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private static final String TAG = "BluetoothRSSI";

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    private BluetoothAdapter BTAdapter;
    private List<BTDevice> deviceList = new ArrayList<>();
    ProgressBar spinner;
    private Button btnEnableBT, btnStartDiscovery;
    private ListView listViewDevices;
    private ArrayAdapter<BTDevice> arrayAdapter;
    private int discoveryStatus = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner =(ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        // Set BT adapter
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BTAdapter == null) {
            showToast("Bluetooth is not available");
        } else {
            showToast("Bluetooth is available");
        }



        btnEnableBT = (Button) findViewById(R.id.btn_turn_on_bluetooth);
        btnEnableBT.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                // Turn on Bluetooth if not enabled
                if (!BTAdapter.isEnabled()) {
                    showToast("Turning on Bluetooth...");

                    // Intent to turn on Bluetooth
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    showToast("Bluetooth is on");
                }
            }
        });

        btnStartDiscovery = findViewById(R.id.btn_start_discovery);
        btnStartDiscovery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevices();
            }
        });

        listViewDevices = findViewById(R.id.listview_devices);
        arrayAdapter = new DeviceListAdapter(this, R.layout.device_item, deviceList);
        listViewDevices.setAdapter(arrayAdapter);
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object selectedDevice =  listViewDevices.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), FindItemActivity.class);
                intent.putExtra("deviceObj", (Serializable) selectedDevice);
                BTAdapter.cancelDiscovery();
                startActivity(intent);
            }
        });
        if (getIntent().getAction() == "com.example.getrssi.CANCEL"){
            discoveryStatus = 1;
            btnStartDiscovery.callOnClick();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    showToast("Bluetooth is on");
                }
                else {
                    // User declined to turn on Bluetooth
                    showToast("Could not turn on bluetooth");
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void scanDevices(){
        // Make device discoverable
        if (!BTAdapter.isDiscovering() && discoveryStatus == 0){
            showToast("Making Your Device Discoverable");
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 5);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVER_BT);
            discoveryStatus = 1;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBTPermissions();
        }
        BTAdapter.startDiscovery();
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery started");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setVisibility(View.VISIBLE);
                    }
                });
                deviceList.clear();
                arrayAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setVisibility(View.GONE);
                    }
                });
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                BTDevice device = new BTDevice();
                device.name = name;
                device.rssi = rssi;

                if (!deviceList.contains(device)) {
                            deviceList.add(device);
                            Log.d(TAG, deviceList.toString());
                            arrayAdapter.notifyDataSetChanged();
                        }
                else{
                    deviceList.set(deviceList.indexOf(device), device);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

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
