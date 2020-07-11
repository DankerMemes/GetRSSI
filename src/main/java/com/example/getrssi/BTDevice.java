package com.example.getrssi;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class BTDevice implements Serializable {
    public String name;
    public String registeredName;
    public int rssi;

    @Override
    @NonNull
    public String toString() {
        return (this.registeredName != null ? this.registeredName : this.name) /*+ " => " + this.rssi + "dBm"*/;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTDevice device = (BTDevice) o;
        return this.name.equals(device.name);
    }
}
