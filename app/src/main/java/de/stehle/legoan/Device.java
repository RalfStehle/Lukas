package de.stehle.legoan;

import java.util.ArrayList;
import java.util.List;

public abstract class Device {
    private final List<ChangeListener> listeners = new ArrayList<>();
    private int battery;
    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    protected void setIsConnected(boolean value) {
        if (isConnected != value) {
            isConnected = value;
            notifyChanged();
        }
    }

    public int getBattery() {
        return battery;
    }

    protected void setBattery(int value) {
        if (battery != value) {
            battery = value;
            notifyChanged();
        }
    }

    public void subscribe(ChangeListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(ChangeListener listener) {
        listeners.remove(listener);
    }

    protected void notifyChanged() {
        for (ChangeListener listener : listeners) {
            listener.notifyChanged();
        }
    }

    public abstract String getName();

    public abstract String getAddress();

    public abstract void disconnect();
}
