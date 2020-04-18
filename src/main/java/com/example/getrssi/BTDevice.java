package com.example.getrssi;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class BTDevice implements Serializable {
    public String name;
    public int rssi;

    @Override
    public String toString() {
        return this.name + " => " + this.rssi + "dBm";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTDevice device = (BTDevice) o;
        return this.name.equals(device.name);
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }
}
