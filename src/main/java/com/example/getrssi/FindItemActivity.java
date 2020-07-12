package com.example.getrssi;

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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.example.getrssi.util.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class FindItemActivity extends RobotActivity {
    private static final String TAG = "FindItemActivity";
    private static final int REQUEST_DISCOVER_BT = 1;

    private ProgressBar progressBarSpinner;
    private Button btnCancel, btnFindItem;
    private TextView textViewDeviceName, textViewRSSIValue, textViewLastLocation;
    private BluetoothAdapter BTAdapter;

    private boolean discoveryStartedFlag = false, isReceiverRegistered = false;;
    private int initialRSSI, previousStrength, followCommandSerialNumber;
    private String selectedDevName;
    private BTDevice deviceObj;

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
        setContentView(R.layout.activity_find_item);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        deviceObj = (BTDevice)intent.getSerializableExtra("deviceObj");

        progressBarSpinner = (ProgressBar) findViewById(R.id.progress_find_item);
        selectedDevName = deviceObj.registeredName != null ? deviceObj.registeredName : deviceObj.deviceName;
        textViewDeviceName = findViewById(R.id.textview_device_name);
        textViewDeviceName.setText(selectedDevName);
        textViewRSSIValue = findViewById(R.id.textview_rssi_value);
        String rssi = String.valueOf(deviceObj.rssi);
        initialRSSI = deviceObj.rssi;
        previousStrength = initialRSSI;
        textViewRSSIValue.setText(rssi + " DBM");

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                BTAdapter.cancelDiscovery();
                progressBarSpinner.setVisibility(View.INVISIBLE);
                Log.d(TAG, "Cancel Selection");
                Intent cancelSearch = new Intent(FindItemActivity.this, MainActivity.class);
                setResult(RESULT_CANCELED, cancelSearch);
                FindItemActivity.this.finish();
            }
        });

        btnFindItem = (Button)findViewById(R.id.btn_find_item);
        btnFindItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Finding Item");
                robotAPI.robot.speak("zenbo will follow you and attempt to detect this device");
//                followCommandSerialNumber = robotAPI.utility.followUser();
                scanDevices();
            }
        });

        // Get last location
        HttpUtils.get(String.format("%d/getLastLocation", deviceObj.id),  new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, response.toString());
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
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
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
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
        progressBarSpinner.setVisibility(View.VISIBLE);
        BTAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery started");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                int updatedRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                if (name == null) {
                    BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    name = dev.getAddress();
                }

                if (name.equals(selectedDevName)) {
                    textViewRSSIValue.setText(updatedRSSI + " DBM");
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
                        progressBarSpinner.setVisibility(View.INVISIBLE);
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
