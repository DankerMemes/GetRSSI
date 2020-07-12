package com.example.getrssi;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.getrssi.util.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class RegisteredItemsActivity extends AppCompatActivity {
    private static final String TAG = "RegisteredItemsActivity";
    private static String alertMsg;

    private List<BTDevice> itemList = new ArrayList<>();
    private ArrayAdapter<BTDevice> arrayAdapter;

    private ProgressBar progressItemList;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        HttpUtils.get("/items", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        BTDevice item = new BTDevice();
                        item.assignedName = obj.getString("deviceName");
                        item.registeredName = obj.getString("registeredName");
                        itemList.add(item);
                    }
                    progressItemList = findViewById(R.id.progress_item_list);
                    progressItemList.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    alertMsg = errorResponse != null
                            ? errorResponse.get("message").toString()
                            : "Connection timeout occurred";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createAlertDialog();
            }
        });


        final ListView listViewItemList = findViewById(R.id.listview_registered_items);
        arrayAdapter = new DeviceListAdapter(this, R.layout.device_item, itemList);
        listViewItemList.setAdapter(arrayAdapter);
        listViewItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BTDevice selectedItem = (BTDevice) listViewItemList.getItemAtPosition(position);
//                Log.d(TAG, "onClick: opening dialog");
            }
        });
    }




    // HELPER FUNCTIONS
    private void createAlertDialog() {
        // Set up alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisteredItemsActivity.this);
        builder.setTitle("Error Occurred");
        builder.setMessage(alertMsg);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the alert dialog
                dialog.cancel();
            }
        });

        // Create and show the alert dialog
        alertDialog = builder.create();
        alertDialog.show();
    }

}
