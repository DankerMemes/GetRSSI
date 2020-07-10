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

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.Random;

public class FindItemActivity extends RobotActivity {
    private static final int REQUEST_DISCOVER_BT = 1;

    String Tag = "BluetoothRSSISearch";
    private ProgressBar spinner;
    private Button btnCancel, btnFindItem;
    private TextView devName, rssiVal;
    private BluetoothAdapter BTAdapter;
    private boolean discoveryStartedFlag = false;
    private int initialRssi;
    private String selectedDevName;
    private boolean isReceiverRegistered = false;
    private int previousStrength;
    private int followCommandSerialNumber;
    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public FindItemActivity(){
        super(robotCallback, robotListenCallback);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        spinner = (ProgressBar)findViewById(R.id.searching);
        spinner.setVisibility(View.INVISIBLE);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        BTDevice deviceObj = (BTDevice)intent.getSerializableExtra("deviceObj");
        selectedDevName = deviceObj.getName();
        devName = (TextView)findViewById(R.id.deviceName1);
        devName.setText(deviceObj.getName());
        rssiVal = (TextView)findViewById(R.id.rssiValue);
        String rssi = String.valueOf(deviceObj.getRssi());
        initialRssi = deviceObj.getRssi();
        previousStrength = initialRssi;
        rssiVal.setText(rssi + " DBM");

        btnCancel = (Button)findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                BTAdapter.cancelDiscovery();
                spinner.setVisibility(View.INVISIBLE);
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
                robotAPI.robot.speak("zenbo will follow you and attempt to detect this device");
//                followCommandSerialNumber = robotAPI.utility.followUser();
                scanDevices();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_DISCOVER_BT:
                if (resultCode == RESULT_OK) {
                    showToast("Scan started");
                    discoveryStartedFlag = true;
                }
                else {
                    // User declined to turn on Bluetooth
                    showToast("Could not start scan");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        if (!BTAdapter.isDiscovering()) {
            showToast("Making Your Device Discoverable");
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            if (!discoveryStartedFlag) {
                startActivityForResult(discoverableIntent, REQUEST_DISCOVER_BT);
            }
//            discoveryStatus = 1;
        }

        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);
            isReceiverRegistered = true;
        }
        spinner.setVisibility(View.VISIBLE);
        BTAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(Tag, "Discovery started");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(Tag, "Discovery finished");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                int updatedRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                if (name == null) {
                    BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    name = dev.getAddress();
                }

                if (name.equals(selectedDevName)) {
                    rssiVal.setText(updatedRSSI + " DBM");
                    if (updatedRSSI < -50) {
//                        robotAPI.robot.speak("rssi is less than -50");
                        // if (updatedRSSI - 10) < previousStrength
                        //      turn left
                        // while cannot move forward (detect object in front)
                        //      turn left
                        // move 1 metre forward
                        if ((updatedRSSI - 10) < previousStrength && updatedRSSI < previousStrength) {
//                            robotAPI.motion.moveBody(0, 0, (float) 1.57);
//                            int random = new Random().nextInt(2);
//                            switch (random) {
//                                case 0:
//                                    robotAPI.motion.moveBody(0, 0, (float) 1.57);
//                                    break;
//                                case 1:
//                                    robotAPI.motion.moveBody(0, 0, (float) -1.57);
//                                    break;
//                            }
                        } else {
//                            robotAPI.robot.speak("move 1m forward");
                            robotAPI.motion.moveBody(0, 1, 0);
                        }
                        previousStrength = updatedRSSI;
//                        scanDevices();
                        BTAdapter.cancelDiscovery();
                        Log.d("YoDawg", Integer.toString(BTAdapter.getState()));
                        BTAdapter.startDiscovery();
                    } else {
                        robotAPI.robot.speak("in 1m range");
                        spinner.setVisibility(View.INVISIBLE);
                        robotAPI.cancelCommandAll();
                        robotAPI.motion.stopMoving();
                        BTAdapter.cancelDiscovery();
                    }
                }

            }
//        }
        }
    };
}
