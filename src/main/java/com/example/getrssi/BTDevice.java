package com.example.getrssi;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class BTDevice implements Serializable {
    public int id;
    public String deviceName;
    public String registeredName;
    public int rssi = -999;

    public BTDevice() {}

    public BTDevice(JSONObject deviceJson) {
        try {
            this.id = deviceJson.getInt("id");
            this.deviceName = deviceJson.getString("deviceName");
            this.registeredName = deviceJson.getString("registeredName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson() {
        JSONObject deviceObj = new JSONObject();

        try {
           deviceObj.put("id", this.id);
           deviceObj.put("deviceName", this.deviceName);
           deviceObj.put("registeredName", this.registeredName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return deviceObj;
    }

    @Override
    @NonNull
    public String toString() {
        return (this.registeredName != null ? this.registeredName : this.deviceName) /*+ " => " + this.rssi + "dBm"*/;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTDevice device = (BTDevice) o;
        return this.deviceName.equals(device.deviceName);
    }
}
