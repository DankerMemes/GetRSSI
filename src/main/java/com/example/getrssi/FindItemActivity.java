package com.example.getrssi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class FindItemActivity extends Activity {
    String Tag = "BluetoothRSSISearch";

    private ProgressBar spinner;
    private Button btnCancel, btnFindItem;
    private TextView devName, rssiVal;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        spinner = (ProgressBar)findViewById(R.id.searching);
        spinner.setVisibility(View.GONE);

        Intent intent = getIntent();
        BTDevice deviceObj = (BTDevice)intent.getSerializableExtra("deviceObj");
        devName = (TextView)findViewById(R.id.deviceName1);
        devName.setText(deviceObj.getName());
        rssiVal = (TextView)findViewById(R.id.rssiValue);
        String rssi = String.valueOf(deviceObj.getRssi());
        rssiVal.setText(rssi + " DBM");

        btnCancel = (Button)findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(Tag, "Cancel Selection");
                Intent cancelSearch = new Intent(getApplicationContext(), MainActivity.class);
                cancelSearch.setAction("com.example.getrssi.CANCEL");
                startActivity(cancelSearch);
            }
        });

        btnFindItem = (Button)findViewById(R.id.select);
        btnFindItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Tag, "Finding Item");
            }
        });
    }

    private void startSearch(){

    }
}
