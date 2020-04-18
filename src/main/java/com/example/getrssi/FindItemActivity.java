package com.example.getrssi;

import android.Manifest;
import android.app.Activity;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class FindItemActivity extends Activity {
    private static final int REQUEST_DISCOVER_BT = 1;

    String Tag = "BluetoothRSSISearch";
    private ProgressBar spinner;
    private Button btnCancel, btnFindItem;
    private TextView devName, rssiVal;
    private BluetoothAdapter BTAdapter;
    private int discoveryStatus = 1;
    private int initialRssi;

    private boolean isReceiverRegistered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        spinner = (ProgressBar)findViewById(R.id.searching);
        spinner.setVisibility(View.GONE);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        BTDevice deviceObj = (BTDevice)intent.getSerializableExtra("deviceObj");
        devName = (TextView)findViewById(R.id.deviceName1);
        devName.setText(deviceObj.getName());
        rssiVal = (TextView)findViewById(R.id.rssiValue);
        String rssi = String.valueOf(deviceObj.getRssi());
        initialRssi = deviceObj.getRssi();
        rssiVal.setText(rssi + " DBM");

        btnCancel = (Button)findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(Tag, "Cancel Selection");
                Intent cancelSearch = new Intent(FindItemActivity.this, MainActivity.class);
                setResult(RESULT_CANCELED, cancelSearch);
                FindItemActivity.this.finish();
            }
        });

        btnFindItem = (Button)findViewById(R.id.select);
        btnFindItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Tag, "Finding Item");
                scanDevices();
            }
        });
    }

//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(receiver);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isReceiverRegistered) {
            unregisterReceiver(receiver);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(Tag, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
        isReceiverRegistered = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBTPermissions();
        }
        BTAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Tag, "received");
        }
    };
}
