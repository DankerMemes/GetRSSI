package com.example.getrssi;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogRegisterNew extends DialogFragment {
    private static final String TAG = "DialogRegisterNew";

    private EditText editTextItemName;
    private Button btnSaveItem, btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_register_new, container, false);

        BTDevice item = (BTDevice) getArguments().getSerializable("itemObj");
        Log.d(TAG, item.assignedName + item.rssi);

        editTextItemName = view.findViewById(R.id.edittext_personal_item_name);
        btnSaveItem = view.findViewById(R.id.btn_save_item);
        btnCancel = view.findViewById(R.id.btn_cancel_save);

        btnSaveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input");

                String input = editTextItemName.getText().toString();
                if (!input.equals("")) {
                    Log.d(TAG, "Name set to " + input);
                }
                getDialog().dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }
        });

        return view;
//        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
