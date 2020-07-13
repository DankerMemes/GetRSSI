package com.example.getrssi;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.getrssi.util.BTDevice;
import com.example.getrssi.util.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DialogRegisterNew extends DialogFragment {
    private static final String TAG = "DialogRegisterNew";

    private EditText editTextRegisteredName;
    private TextView textViewDeviceName;
    private Button btnSaveItem, btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_register_new, container, false);

        final BTDevice item = (BTDevice) getArguments().getSerializable("itemObj");
        Log.d(TAG, item.deviceName + item.rssi);

        textViewDeviceName = view.findViewById(R.id.textview_found_item_name);
        textViewDeviceName.setText(item.deviceName);
        editTextRegisteredName = view.findViewById(R.id.textview_location_found);

        btnSaveItem = view.findViewById(R.id.btn_save_location);
        btnCancel = view.findViewById(R.id.btn_cancel_save);

        btnSaveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSaveItem.setEnabled(false);
                Log.d(TAG, "onClick: capturing input");
                String input = editTextRegisteredName.getText().toString().trim();
                if (!input.equals("")) {
                    Log.d(TAG, "Name set to " + input);
                    item.registeredName = input;
                }

                StringEntity itemEntity = null;
                try {
                    itemEntity = new StringEntity(item.toJSON().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                HttpUtils.post(getContext(), "addItem", itemEntity, "application/json", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, response.toString());
                        getDialog().dismiss();
                        getActivity().finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        getDialog().dismiss();
                        getActivity().finish();
                    }
                });
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCancel.setEnabled(false);
                btnSaveItem.setEnabled(false);
                Log.d(TAG, "onClick: closing dialog");

                getDialog().dismiss();
            }
        });

        return view;
//        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
